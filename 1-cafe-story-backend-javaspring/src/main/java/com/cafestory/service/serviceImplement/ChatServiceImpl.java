package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.CreateDirectConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateCafePageConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateGroupConversationRequestDTO;
import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.dto.requestDTO.UpdateGroupInfoRequestDTO;
import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.ConversationResponseDTO;
import com.cafestory.dto.responseDTO.SocketEventResponseDTO;
import com.cafestory.entity.ChatMember;
import com.cafestory.entity.ChatMessage;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Conversation;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ConversationType;
import com.cafestory.entity.enums.MemberRole;
import com.cafestory.entity.enums.MessageStatus;
import com.cafestory.entity.enums.MessageType;
import com.cafestory.mapper.ChatMapper;
import com.cafestory.repository.ChatMemberRepository;
import com.cafestory.repository.ChatMessageRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ConversationRepository;
import com.cafestory.service.serviceInterface.ChatService;
import com.cafestory.service.serviceInterface.FirebaseChatService;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.validation.UserValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CONVERSATION_TOPIC_PREFIX = "/topic/conversations/";

    private final ConversationRepository conversationRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CafePageRepository cafePageRepository;
    private final UserValidator userValidator;
    private final ChatMapper chatMapper;
    private final FirebaseChatService firebaseChatService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(
            ConversationRepository conversationRepository,
            ChatMemberRepository chatMemberRepository,
            ChatMessageRepository chatMessageRepository,
            CafePageRepository cafePageRepository,
            UserValidator userValidator,
            ChatMapper chatMapper,
            FirebaseChatService firebaseChatService,
            NotificationService notificationService,
            SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.cafePageRepository = cafePageRepository;
        this.userValidator = userValidator;
        this.chatMapper = chatMapper;
        this.firebaseChatService = firebaseChatService;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public ConversationResponseDTO createOrGetDirectConversation(CreateDirectConversationRequestDTO request) {
        validateDifferentUsers(request.getFirstUserId(), request.getSecondUserId());
        User firstUser = userValidator.validateUserExists(request.getFirstUserId());
        User secondUser = userValidator.validateUserExists(request.getSecondUserId());

        return conversationRepository.findDirectConversation(firstUser.getUserId(), secondUser.getUserId())
                .map(conversation -> toConversationResponse(conversation, firstUser.getUserId()))
                .orElseGet(() -> createDirectConversation(firstUser, secondUser));
    }

    @Override
    @Transactional
    public ConversationResponseDTO createOrGetCafePageConversation(CreateCafePageConversationRequestDTO request) {
        CafePage cafePage = cafePageRepository.findById(request.getCafePageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page not found"));
        User user = userValidator.validateUserExists(request.getUserId());
        User owner = cafePage.getOwner();

        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cafe page owner is missing");
        }
        validateDifferentUsers(user.getUserId(), owner.getUserId());

        ConversationResponseDTO response = conversationRepository
                .findDirectConversation(user.getUserId(), owner.getUserId())
                .map(conversation -> toConversationResponse(conversation, user.getUserId()))
                .orElseGet(() -> createDirectConversation(user, owner));

        response.setChatName(cafePage.getName());
        response.setUserName(cafePage.getName());
        response.setChatAvatar(cafePage.getAvatarUrl());
        return response;
    }

    @Override
    @Transactional
    public ConversationResponseDTO createGroupConversation(CreateGroupConversationRequestDTO request) {
        User creator = userValidator.validateUserExists(request.getCreatorUserId());
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.GROUP);
        conversation.setGroupName(request.getGroupName().trim());
        conversation.setGroupAvatar(blankToNull(request.getGroupAvatar()));
        Conversation savedConversation = conversationRepository.save(conversation);

        addMemberEntity(savedConversation, creator, MemberRole.OWNER);
        for (UUID memberId : distinctMemberIds(request.getMemberIds())) {
            if (!memberId.equals(creator.getUserId())) {
                addMemberEntity(savedConversation, userValidator.validateUserExists(memberId), MemberRole.MEMBER);
            }
        }

        ConversationResponseDTO response = toConversationResponse(savedConversation, creator.getUserId());
        firebaseChatService.saveConversation(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponseDTO> getUserConversations(UUID userId) {
        userValidator.validateUserExists(userId);
        return conversationRepository.findUserConversationsOrderByLatestActivity(userId)
                .stream()
                .map(conversation -> toConversationResponse(conversation, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getMessagesByConversationId(UUID conversationId, UUID userId, int page, int size) {
        validateConversationExists(conversationId);
        validateSenderIsMember(conversationId, userId);
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        PageRequest pageRequest = PageRequest.of(
                sanitizedPage,
                sanitizedSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return chatMessageRepository.findByConversationId(conversationId, pageRequest)
                .map(chatMapper::toChatMessageResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ChatMessageResponseDTO sendMessage(UUID conversationId, SendMessageRequestDTO request) {
        Conversation conversation = validateConversationExists(conversationId);
        User sender = userValidator.validateUserExists(request.getSenderId());
        validateSenderIsMember(conversationId, sender.getUserId());
        validateMessagePayload(request);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setType(request.getType());
        message.setText(blankToNull(request.getText()));
        message.setImageUrls(request.getImageUrls() == null ? List.of() : request.getImageUrls());
        message.setStickerUrl(blankToNull(request.getStickerUrl()));
        message.setStickerId(blankToNull(request.getStickerId()));
        message.setStatus(MessageStatus.SENT);
        message.setRead(false);

        ChatMessage savedMessage = chatMessageRepository.save(message);
        conversation.setLatestMessageId(savedMessage.getId());
        conversation.setLatestMessagePreview(buildLatestMessagePreview(savedMessage));
        Conversation savedConversation = conversationRepository.save(conversation);

        ChatMessageResponseDTO messageResponse = chatMapper.toChatMessageResponseDTO(savedMessage);
        ConversationResponseDTO conversationResponse = toConversationResponse(
                savedConversation,
                sender.getUserId(),
                savedMessage);
        firebaseChatService.saveMessage(messageResponse);
        firebaseChatService.updateLatestMessage(conversationResponse, messageResponse);
        publishMessage(conversationId, sender.getUserId(), messageResponse);
        createMessageNotifications(conversationId, sender.getUserId(), savedMessage.getId());
        return messageResponse;
    }

    private void publishMessage(UUID conversationId, UUID senderId, ChatMessageResponseDTO messageResponse) {
        messagingTemplate.convertAndSend(
                CONVERSATION_TOPIC_PREFIX + conversationId,
                new SocketEventResponseDTO("receive_message", conversationId, senderId, messageResponse));
    }

    @Override
    @Transactional
    public ConversationResponseDTO addMember(UUID conversationId, UUID actorUserId, UUID memberUserId) {
        Conversation conversation = validateGroupConversation(conversationId);
        validateManagePermission(conversationId, actorUserId);
        if (!chatMemberRepository.existsByConversationIdAndUserUserId(conversationId, memberUserId)) {
            addMemberEntity(conversation, userValidator.validateUserExists(memberUserId), MemberRole.MEMBER);
        }
        ConversationResponseDTO response = toConversationResponse(conversation, actorUserId);
        firebaseChatService.saveConversation(response);
        return response;
    }

    @Override
    @Transactional
    public ConversationResponseDTO removeMember(UUID conversationId, UUID actorUserId, UUID memberUserId) {
        Conversation conversation = validateGroupConversation(conversationId);
        validateManagePermission(conversationId, actorUserId);
        ChatMember member = validateMember(conversationId, memberUserId);
        if (member.getRole() == MemberRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot remove group owner");
        }
        chatMemberRepository.delete(member);
        ConversationResponseDTO response = toConversationResponse(conversation, actorUserId);
        firebaseChatService.saveConversation(response);
        return response;
    }

    @Override
    @Transactional
    public void leaveGroup(UUID conversationId, UUID actorUserId) {
        Conversation conversation = validateGroupConversation(conversationId);
        ChatMember member = validateMember(conversationId, actorUserId);
        if (member.getRole() == MemberRole.OWNER && chatMemberRepository.countByConversation(conversation) > 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner must transfer ownership before leaving");
        }
        chatMemberRepository.delete(member);
        firebaseChatService.saveConversation(toConversationResponse(conversation, actorUserId));
    }

    @Override
    @Transactional
    public ConversationResponseDTO updateGroupInfo(UUID conversationId, UpdateGroupInfoRequestDTO request) {
        Conversation conversation = validateGroupConversation(conversationId);
        validateManagePermission(conversationId, request.getActorUserId());
        if (request.getGroupName() != null) {
            if (request.getGroupName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name cannot be blank");
            }
            conversation.setGroupName(request.getGroupName().trim());
        }
        if (request.getGroupAvatar() != null) {
            conversation.setGroupAvatar(blankToNull(request.getGroupAvatar()));
        }
        Conversation savedConversation = conversationRepository.save(conversation);
        ConversationResponseDTO response = toConversationResponse(savedConversation, request.getActorUserId());
        firebaseChatService.saveConversation(response);
        return response;
    }

    private ConversationResponseDTO createDirectConversation(User firstUser, User secondUser) {
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.DIRECT);
        Conversation savedConversation = conversationRepository.save(conversation);
        addMemberEntity(savedConversation, firstUser, MemberRole.MEMBER);
        addMemberEntity(savedConversation, secondUser, MemberRole.MEMBER);
        ConversationResponseDTO response = toConversationResponse(savedConversation, firstUser.getUserId());
        firebaseChatService.saveConversation(response);
        return response;
    }

    private ChatMember addMemberEntity(Conversation conversation, User user, MemberRole role) {
        ChatMember member = new ChatMember();
        member.setConversation(conversation);
        member.setUser(user);
        member.setRole(role);
        return chatMemberRepository.save(member);
    }

    private Conversation validateConversationExists(UUID conversationId) {
        if (conversationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation id is required");
        }
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private Conversation validateGroupConversation(UUID conversationId) {
        Conversation conversation = validateConversationExists(conversationId);
        if (conversation.getType() != ConversationType.GROUP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation is not a group");
        }
        return conversation;
    }

    private ChatMember validateMember(UUID conversationId, UUID userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }
        return chatMemberRepository.findByConversationIdAndUserUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User not in conversation"));
    }

    private void validateSenderIsMember(UUID conversationId, UUID senderId) {
        validateMember(conversationId, senderId);
    }

    private void validateManagePermission(UUID conversationId, UUID actorUserId) {
        ChatMember actor = validateMember(conversationId, actorUserId);
        if (actor.getRole() != MemberRole.OWNER && actor.getRole() != MemberRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }
    }

    private void validateDifferentUsers(UUID firstUserId, UUID secondUserId) {
        if (firstUserId != null && firstUserId.equals(secondUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Direct conversation requires two different users");
        }
    }

    private void validateMessagePayload(SendMessageRequestDTO request) {
        boolean hasText = request.getText() != null && !request.getText().isBlank();
        boolean hasImages = request.getImageUrls() != null && !request.getImageUrls().isEmpty();
        boolean hasSticker = (request.getStickerUrl() != null && !request.getStickerUrl().isBlank())
                || (request.getStickerId() != null && !request.getStickerId().isBlank());

        if (request.getType() == null || !List.of(MessageType.TEXT, MessageType.IMAGE, MessageType.STICKER, MessageType.MIXED)
                .contains(request.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid message type");
        }
        if (request.getImageUrls() != null && request.getImageUrls().stream().anyMatch(url -> url == null || url.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageUrls must contain non-empty strings");
        }

        switch (request.getType()) {
            case TEXT -> require(hasText, "Text message cannot be empty");
            case IMAGE -> require(hasImages, "Image message requires imageUrls");
            case STICKER -> require(hasSticker, "Sticker message requires stickerUrl or stickerId");
            case MIXED -> require(hasText || hasImages || hasSticker, "Message cannot be empty");
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid message type");
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private String buildLatestMessagePreview(ChatMessage message) {
        if (message.getText() != null && !message.getText().isBlank()) {
            return message.getText();
        }
        if (message.getType() == MessageType.IMAGE) {
            return "[image]";
        }
        if (message.getType() == MessageType.STICKER) {
            return "[sticker]";
        }
        return "[message]";
    }

    private ConversationResponseDTO toConversationResponse(Conversation conversation, UUID viewerUserId) {
        ChatMessage latestMessage = conversation.getLatestMessageId() == null
                ? null
                : chatMessageRepository.findById(conversation.getLatestMessageId()).orElse(null);
        return toConversationResponse(conversation, viewerUserId, latestMessage);
    }

    private ConversationResponseDTO toConversationResponse(
            Conversation conversation,
            UUID viewerUserId,
            ChatMessage latestMessage) {
        long unreadCount = latestMessage != null
                && viewerUserId != null
                && !viewerUserId.equals(latestMessage.getSender().getUserId())
                && !latestMessage.isRead()
                ? 1
                : 0;
        return chatMapper.toConversationResponseDTO(
                conversation,
                chatMemberRepository.findByConversationId(conversation.getId()),
                viewerUserId,
                latestMessage,
                unreadCount);
    }

    private Set<UUID> distinctMemberIds(List<UUID> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member ids are mandatory");
        }
        return new LinkedHashSet<>(memberIds);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void createMessageNotifications(UUID conversationId, UUID senderId, UUID messageId) {
        chatMemberRepository.findByConversationId(conversationId)
                .stream()
                .map(ChatMember::getUser)
                .filter(user -> !user.getUserId().equals(senderId))
                .forEach(user -> {
                    try {
                        notificationService.createMessageNotification(user.getUserId(), senderId, conversationId, messageId);
                    } catch (RuntimeException ignored) {
                        // Message delivery should not fail if notification delivery fails.
                    }
                });
    }
}
