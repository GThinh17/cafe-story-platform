// package com.cafestory.service;

// import com.cafestory.dto.requestDTO.chat.CreateDirectConversationRequest;
// import com.cafestory.dto.requestDTO.chat.CreateGroupConversationRequest;
// import com.cafestory.dto.requestDTO.chat.SendMessageRequest;
// import com.cafestory.dto.requestDTO.chat.UpdateGroupInfoRequest;
// import com.cafestory.dto.responseDTO.chat.ChatMessageResponseDTO;
// import com.cafestory.dto.responseDTO.chat.SocketEventResponseDTO;
// import com.cafestory.entity.ChatMember;
// import com.cafestory.entity.ChatMessage;
// import com.cafestory.entity.Conversation;
// import com.cafestory.entity.User;
// import com.cafestory.entity.enums.ConversationType;
// import com.cafestory.entity.enums.MemberRole;
// import com.cafestory.entity.enums.MessageType;
// import com.cafestory.mapper.ChatMapper;
// import com.cafestory.repository.ChatMemberRepository;
// import com.cafestory.repository.ChatMessageRepository;
// import com.cafestory.repository.ConversationRepository;
// import com.cafestory.service.serviceImplement.ChatServiceImpl;
// import com.cafestory.service.serviceInterface.FirebaseChatService;
// import com.cafestory.service.serviceInterface.NotificationService;
// import com.cafestory.validation.UserValidator;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.HttpStatus;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.web.server.ResponseStatusException;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.doAnswer;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// class ChatServiceImplTest {

// @Mock
// private ConversationRepository conversationRepository;

// @Mock
// private ChatMemberRepository chatMemberRepository;

// @Mock
// private ChatMessageRepository chatMessageRepository;

// @Mock
// private UserValidator userValidator;

// @Mock
// private FirebaseChatService firebaseChatService;

// @Mock
// private NotificationService notificationService;

// @Mock
// private SimpMessagingTemplate messagingTemplate;

// private ChatServiceImpl chatService;

// @BeforeEach
// void setUp() {
// chatService = new ChatServiceImpl(
// conversationRepository,
// chatMemberRepository,
// chatMessageRepository,
// userValidator,
// new ChatMapper(),
// firebaseChatService,
// notificationService,
// messagingTemplate);
// }

// @Test
// void createOrGetDirectConversation_success_createsWhenMissing_TC001() {
// User firstUser = user(UUID.randomUUID());
// User secondUser = user(UUID.randomUUID());
// CreateDirectConversationRequest request = new
// CreateDirectConversationRequest();
// request.setFirstUserId(firstUser.getUserId());
// request.setSecondUserId(secondUser.getUserId());
// mockConversationSave();
// mockMemberSave();

// when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
// when(userValidator.validateUserExists(secondUser.getUserId())).thenReturn(secondUser);
// when(conversationRepository.findDirectConversation(firstUser.getUserId(),
// secondUser.getUserId()))
// .thenReturn(Optional.empty());
// when(chatMemberRepository.findByConversationId(any(UUID.class)))
// .thenReturn(List.of(member(firstUser, MemberRole.MEMBER), member(secondUser,
// MemberRole.MEMBER)));

// var result = chatService.createOrGetDirectConversation(request);

// assertThat(result.getType()).isEqualTo(ConversationType.DIRECT);
// assertThat(result.getMembers()).hasSize(2);
// verify(firebaseChatService).saveConversation(result);
// }

// @Test
// void createGroupConversation_success_TC002() {
// User owner = user(UUID.randomUUID());
// User member = user(UUID.randomUUID());
// CreateGroupConversationRequest request = new
// CreateGroupConversationRequest();
// request.setCreatorUserId(owner.getUserId());
// request.setGroupName("Cafe team");
// request.setGroupAvatar("https://example.com/group.png");
// request.setMemberIds(List.of(owner.getUserId(), member.getUserId()));
// mockConversationSave();
// mockMemberSave();

// when(userValidator.validateUserExists(owner.getUserId())).thenReturn(owner);
// when(userValidator.validateUserExists(member.getUserId())).thenReturn(member);
// when(chatMemberRepository.findByConversationId(any(UUID.class)))
// .thenReturn(List.of(member(owner, MemberRole.OWNER), member(member,
// MemberRole.MEMBER)));

// var result = chatService.createGroupConversation(request);

// assertThat(result.getType()).isEqualTo(ConversationType.GROUP);
// assertThat(result.getGroupName()).isEqualTo("Cafe team");
// assertThat(result.getMembers()).hasSize(2);
// verify(firebaseChatService).saveConversation(result);
// }

// @Test
// void
// getUserConversations_success_returnsRepositorySortedConversations_TC011() {
// User currentUser = user(UUID.randomUUID());
// User firstParticipant = user(UUID.randomUUID());
// User secondParticipant = user(UUID.randomUUID());
// Conversation newestConversation = conversation(ConversationType.DIRECT);
// Conversation olderConversation = conversation(ConversationType.DIRECT);

// newestConversation.setUpdatedAt(LocalDateTime.now());
// olderConversation.setUpdatedAt(newestConversation.getUpdatedAt().minusDays(1));

// when(userValidator.validateUserExists(currentUser.getUserId())).thenReturn(currentUser);
// when(conversationRepository.findUserConversationsOrderByLatestActivity(currentUser.getUserId()))
// .thenReturn(List.of(newestConversation, olderConversation));
// when(chatMemberRepository.findByConversationId(newestConversation.getId()))
// .thenReturn(List.of(
// member(currentUser, MemberRole.MEMBER),
// member(firstParticipant, MemberRole.MEMBER)));
// when(chatMemberRepository.findByConversationId(olderConversation.getId()))
// .thenReturn(List.of(
// member(currentUser, MemberRole.MEMBER),
// member(secondParticipant, MemberRole.MEMBER)));

// var result = chatService.getUserConversations(currentUser.getUserId());

// assertThat(result)
// .extracting("id")
// .containsExactly(newestConversation.getId(), olderConversation.getId());
// }

// @Test
// void sendMessage_success_text_updatesFirebaseAndLatestMessage_TC003() {
// Conversation conversation = conversation(ConversationType.DIRECT);
// User sender = user(UUID.randomUUID());
// SendMessageRequest request = sendRequest(sender.getUserId(),
// MessageType.TEXT);
// request.setText("hello");
// mockMessageSending(conversation, sender);

// ChatMessageResponseDTO result = chatService.sendMessage(conversation.getId(),
// request);

// assertThat(result.getType()).isEqualTo(MessageType.TEXT);
// assertThat(result.getText()).isEqualTo("hello");
// verify(firebaseChatService).saveMessage(result);
// verify(firebaseChatService).updateLatestMessage(any(), any());
// ArgumentCaptor<SocketEventResponseDTO> eventCaptor =
// ArgumentCaptor.forClass(SocketEventResponseDTO.class);
// verify(messagingTemplate).convertAndSend(eq("/topic/conversations/" +
// conversation.getId()), eventCaptor.capture());
// assertThat(eventCaptor.getValue().getEvent()).isEqualTo("receive_message");
// assertThat(eventCaptor.getValue().getConversationId()).isEqualTo(conversation.getId());
// assertThat(eventCaptor.getValue().getUserId()).isEqualTo(sender.getUserId());
// assertThat(eventCaptor.getValue().getPayload()).isEqualTo(result);
// verify(notificationService).createMessageNotification(any(UUID.class),
// any(UUID.class), any(UUID.class), any(UUID.class));
// verify(conversationRepository).save(conversation);
// assertThat(conversation.getLatestMessagePreview()).isEqualTo("hello");
// }

// @Test
// void sendMessage_success_multipleImageUrls_TC004() {
// Conversation conversation = conversation(ConversationType.GROUP);
// User sender = user(UUID.randomUUID());
// SendMessageRequest request = sendRequest(sender.getUserId(),
// MessageType.IMAGE);
// request.setImageUrls(List.of("https://example.com/a.png",
// "https://example.com/b.png"));
// mockMessageSending(conversation, sender);

// ChatMessageResponseDTO result = chatService.sendMessage(conversation.getId(),
// request);

// assertThat(result.getType()).isEqualTo(MessageType.IMAGE);
// assertThat(result.getImageUrls()).containsExactly("https://example.com/a.png",
// "https://example.com/b.png");
// }

// @Test
// void sendMessage_success_sticker_TC005() {
// Conversation conversation = conversation(ConversationType.DIRECT);
// User sender = user(UUID.randomUUID());
// SendMessageRequest request = sendRequest(sender.getUserId(),
// MessageType.STICKER);
// request.setStickerId("latte-smile");
// mockMessageSending(conversation, sender);

// ChatMessageResponseDTO result = chatService.sendMessage(conversation.getId(),
// request);

// assertThat(result.getType()).isEqualTo(MessageType.STICKER);
// assertThat(result.getStickerId()).isEqualTo("latte-smile");
// }

// @Test
// void sendMessage_fail_userNotInConversation_TC006() {
// Conversation conversation = conversation(ConversationType.DIRECT);
// User sender = user(UUID.randomUUID());
// SendMessageRequest request = sendRequest(sender.getUserId(),
// MessageType.TEXT);
// request.setText("blocked");

// when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
// when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
// when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(),
// sender.getUserId()))
// .thenReturn(Optional.empty());

// assertThatThrownBy(() -> chatService.sendMessage(conversation.getId(),
// request))
// .isInstanceOf(ResponseStatusException.class)
// .satisfies(error -> assertThat(((ResponseStatusException)
// error).getStatusCode())
// .isEqualTo(HttpStatus.FORBIDDEN));

// verify(chatMessageRepository, never()).save(any(ChatMessage.class));
// }

// @Test
// void addAndRemoveMember_success_ownerCanManage_TC007() {
// Conversation conversation = conversation(ConversationType.GROUP);
// User owner = user(UUID.randomUUID());
// User newMember = user(UUID.randomUUID());
// ChatMember ownerMember = member(owner, MemberRole.OWNER);
// ownerMember.setConversation(conversation);
// ChatMember removableMember = member(newMember, MemberRole.MEMBER);
// removableMember.setConversation(conversation);
// mockMemberSave();

// when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
// when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(),
// owner.getUserId()))
// .thenReturn(Optional.of(ownerMember));
// when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(),
// newMember.getUserId()))
// .thenReturn(false);
// when(userValidator.validateUserExists(newMember.getUserId())).thenReturn(newMember);
// when(chatMemberRepository.findByConversationId(conversation.getId()))
// .thenReturn(List.of(ownerMember, removableMember));

// var added = chatService.addMember(conversation.getId(), owner.getUserId(),
// newMember.getUserId());

// assertThat(added.getMembers()).hasSize(2);

// when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(),
// newMember.getUserId()))
// .thenReturn(Optional.of(removableMember));

// var removed = chatService.removeMember(conversation.getId(),
// owner.getUserId(), newMember.getUserId());

// assertThat(removed.getType()).isEqualTo(ConversationType.GROUP);
// verify(chatMemberRepository).delete(removableMember);
// }

// @Test
// void leaveGroup_success_memberLeaves_TC008() {
// Conversation conversation = conversation(ConversationType.GROUP);
// User memberUser = user(UUID.randomUUID());
// ChatMember chatMember = member(memberUser, MemberRole.MEMBER);
// chatMember.setConversation(conversation);

// when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
// when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(),
// memberUser.getUserId()))
// .thenReturn(Optional.of(chatMember));
// when(chatMemberRepository.findByConversationId(conversation.getId())).thenReturn(List.of());

// chatService.leaveGroup(conversation.getId(), memberUser.getUserId());

// verify(chatMemberRepository).delete(chatMember);
// verify(firebaseChatService).saveConversation(any());
// }

// @Test
// void updateGroupInfo_fail_memberHasNoPermission_TC009() {
// Conversation conversation = conversation(ConversationType.GROUP);
// User memberUser = user(UUID.randomUUID());
// ChatMember chatMember = member(memberUser, MemberRole.MEMBER);
// chatMember.setConversation(conversation);
// UpdateGroupInfoRequest request = new UpdateGroupInfoRequest();
// request.setActorUserId(memberUser.getUserId());
// request.setGroupName("New name");

// when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
// when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(),
// memberUser.getUserId()))
// .thenReturn(Optional.of(chatMember));

// assertThatThrownBy(() -> chatService.updateGroupInfo(conversation.getId(),
// request))
// .isInstanceOf(ResponseStatusException.class)
// .satisfies(error -> assertThat(((ResponseStatusException)
// error).getStatusCode())
// .isEqualTo(HttpStatus.FORBIDDEN));
// }

// @Test
// void sendMessage_fail_invalidPayloadCases_TC010() {
// Conversation conversation = conversation(ConversationType.DIRECT);
// User sender = user(UUID.randomUUID());
// SendMessageRequest emptyText = sendRequest(sender.getUserId(),
// MessageType.TEXT);

// when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
// when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
// when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(),
// sender.getUserId()))
// .thenReturn(Optional.of(member(sender, MemberRole.MEMBER)));

// assertThatThrownBy(() -> chatService.sendMessage(conversation.getId(),
// emptyText))
// .isInstanceOf(ResponseStatusException.class)
// .satisfies(error -> assertThat(((ResponseStatusException)
// error).getStatusCode())
// .isEqualTo(HttpStatus.BAD_REQUEST));

// SendMessageRequest badImage = sendRequest(sender.getUserId(),
// MessageType.IMAGE);
// badImage.setImageUrls(new ArrayList<>(List.of("https://example.com/a.png")));
// badImage.getImageUrls().add(" ");

// assertThatThrownBy(() -> chatService.sendMessage(conversation.getId(),
// badImage))
// .isInstanceOf(ResponseStatusException.class)
// .satisfies(error -> assertThat(((ResponseStatusException)
// error).getStatusCode())
// .isEqualTo(HttpStatus.BAD_REQUEST));
// }

// private void mockMessageSending(Conversation conversation, User sender) {
// when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
// when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
// when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(),
// sender.getUserId()))
// .thenReturn(Optional.of(member(sender, MemberRole.MEMBER)));
// mockMessageSave();
// when(conversationRepository.save(conversation)).thenReturn(conversation);
// User recipient = user(UUID.randomUUID());
// when(chatMemberRepository.findByConversationId(conversation.getId()))
// .thenReturn(List.of(member(sender, MemberRole.MEMBER), member(recipient,
// MemberRole.MEMBER)));
// }

// private void mockConversationSave() {
// doAnswer(invocation -> {
// Conversation conversation = invocation.getArgument(0);
// conversation.setId(UUID.randomUUID());
// conversation.setCreatedAt(LocalDateTime.now());
// conversation.setUpdatedAt(conversation.getCreatedAt());
// return conversation;
// }).when(conversationRepository).save(any(Conversation.class));
// }

// private void mockMemberSave() {
// doAnswer(invocation -> {
// ChatMember member = invocation.getArgument(0);
// member.setId(UUID.randomUUID());
// member.setJoinedAt(LocalDateTime.now());
// return member;
// }).when(chatMemberRepository).save(any(ChatMember.class));
// }

// private void mockMessageSave() {
// doAnswer(invocation -> {
// ChatMessage message = invocation.getArgument(0);
// message.setId(UUID.randomUUID());
// message.setCreatedAt(LocalDateTime.now());
// message.setUpdatedAt(message.getCreatedAt());
// return message;
// }).when(chatMessageRepository).save(any(ChatMessage.class));
// }

// private User user(UUID userId) {
// User user = new User();
// user.setUserId(userId);
// user.setUserName("user-" + userId);
// user.setUserEmail(userId + "@example.com");
// user.setUserPassword("secret");
// user.setAccountStatus(true);
// return user;
// }

// private Conversation conversation(ConversationType type) {
// Conversation conversation = new Conversation();
// conversation.setId(UUID.randomUUID());
// conversation.setType(type);
// conversation.setGroupName(type == ConversationType.GROUP ? "Cafe group" :
// null);
// conversation.setCreatedAt(LocalDateTime.now());
// conversation.setUpdatedAt(conversation.getCreatedAt());
// return conversation;
// }

// private ChatMember member(User user, MemberRole role) {
// ChatMember member = new ChatMember();
// member.setId(UUID.randomUUID());
// member.setUser(user);
// member.setRole(role);
// member.setJoinedAt(LocalDateTime.now());
// return member;
// }

// private SendMessageRequest sendRequest(UUID senderId, MessageType type) {
// SendMessageRequest request = new SendMessageRequest();
// request.setSenderId(senderId);
// request.setType(type);
// return request;
// }
// }

package com.cafestory.service;

import com.cafestory.dto.requestDTO.CreateCafePageConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateDirectConversationRequestDTO;
import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.ChatMember;
import com.cafestory.entity.ChatMessage;
import com.cafestory.entity.Conversation;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ChatSenderContextType;
import com.cafestory.entity.enums.ChatTargetType;
import com.cafestory.entity.enums.ConversationType;
import com.cafestory.entity.enums.MemberRole;
import com.cafestory.entity.enums.MessageType;
import com.cafestory.mapper.ChatMapper;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ChatMemberRepository;
import com.cafestory.repository.ChatMessageRepository;
import com.cafestory.repository.ConversationRepository;
import com.cafestory.service.serviceImplement.ChatServiceImpl;
import com.cafestory.service.serviceInterface.FirebaseChatService;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMemberRepository chatMemberRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @Mock
    private FirebaseChatService firebaseChatService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(
                conversationRepository,
                chatMemberRepository,
                chatMessageRepository,
                cafePageRepository,
                cafePageValidator,
                userValidator,
                new ChatMapper(),
                firebaseChatService,
                notificationService,
                messagingTemplate);
    }

    @Test
    void createOrGetCafePageConversation_success_createsPageTargetConversation_TC001() {
        User visitor = user(UUID.randomUUID(), "visitor");
        User owner = user(UUID.randomUUID(), "owner");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        CreateCafePageConversationRequestDTO request = new CreateCafePageConversationRequestDTO();
        request.setUserId(visitor.getUserId());
        request.setCafePageId(cafePage.getId());

        mockConversationSave();
        mockMemberSave();
        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(userValidator.validateUserExists(visitor.getUserId())).thenReturn(visitor);
        when(conversationRepository.findCafePageConversation(visitor.getUserId(), cafePage.getId()))
                .thenReturn(Optional.empty());
        when(chatMemberRepository.findByConversationId(any(UUID.class)))
                .thenReturn(List.of(member(visitor, MemberRole.MEMBER), member(owner, MemberRole.OWNER)));

        var result = chatService.createOrGetCafePageConversation(request);

        assertThat(result.getType()).isEqualTo(ConversationType.CAFE_PAGE);
        assertThat(result.getTargetType()).isEqualTo(ChatTargetType.CAFE_PAGE);
        assertThat(result.getTargetId()).isEqualTo(cafePage.getId());
        assertThat(result.getTargetCafePageId()).isEqualTo(cafePage.getId());
        assertThat(result.getTargetUserId()).isNull();
        assertThat(result.getChatName()).isEqualTo(cafePage.getName());
        assertThat(result.getChatAvatar()).isEqualTo(cafePage.getAvatarUrl());
        verify(conversationRepository, never()).findDirectConversation(visitor.getUserId(), owner.getUserId());
        verify(firebaseChatService).saveConversation(result);
    }

    @Test
    void createOrGetCafePageConversation_success_reusesPageTargetConversation_TC002() {
        User visitor = user(UUID.randomUUID(), "visitor");
        User owner = user(UUID.randomUUID(), "owner");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        Conversation conversation = conversation(ConversationType.CAFE_PAGE);
        conversation.setCafePage(cafePage);
        CreateCafePageConversationRequestDTO request = new CreateCafePageConversationRequestDTO();
        request.setUserId(visitor.getUserId());
        request.setCafePageId(cafePage.getId());

        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(userValidator.validateUserExists(visitor.getUserId())).thenReturn(visitor);
        when(conversationRepository.findCafePageConversation(visitor.getUserId(), cafePage.getId()))
                .thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(visitor, MemberRole.MEMBER), member(owner, MemberRole.OWNER)));

        var result = chatService.createOrGetCafePageConversation(request);

        assertThat(result.getId()).isEqualTo(conversation.getId());
        assertThat(result.getType()).isEqualTo(ConversationType.CAFE_PAGE);
        assertThat(result.getTargetCafePageId()).isEqualTo(cafePage.getId());
        verify(conversationRepository, never()).save(any(Conversation.class));
        verify(conversationRepository, never()).findDirectConversation(visitor.getUserId(), owner.getUserId());
    }

    @Test
    void createOrGetDirectConversation_success_returnsUserTargetConversation_TC003() {
        User firstUser = user(UUID.randomUUID(), "first");
        User secondUser = user(UUID.randomUUID(), "second");
        CreateDirectConversationRequestDTO request = new CreateDirectConversationRequestDTO();
        request.setFirstUserId(firstUser.getUserId());
        request.setSecondUserId(secondUser.getUserId());

        mockConversationSave();
        mockMemberSave();
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(userValidator.validateUserExists(secondUser.getUserId())).thenReturn(secondUser);
        when(conversationRepository.findDirectConversation(firstUser.getUserId(), secondUser.getUserId()))
                .thenReturn(Optional.empty());
        when(chatMemberRepository.findByConversationId(any(UUID.class)))
                .thenReturn(List.of(member(firstUser, MemberRole.MEMBER), member(secondUser, MemberRole.MEMBER)));

        var result = chatService.createOrGetDirectConversation(request);

        assertThat(result.getType()).isEqualTo(ConversationType.DIRECT);
        assertThat(result.getTargetType()).isEqualTo(ChatTargetType.USER);
        assertThat(result.getTargetId()).isEqualTo(secondUser.getUserId());
        assertThat(result.getTargetUserId()).isEqualTo(secondUser.getUserId());
        assertThat(result.getTargetCafePageId()).isNull();
    }

    @Test
    void getCafePageConversations_success_returnsPageMailboxOnly_TC004() {
        User owner = user(UUID.randomUUID(), "owner");
        User visitor = user(UUID.randomUUID(), "visitor");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        Conversation conversation = conversation(ConversationType.CAFE_PAGE);
        conversation.setCafePage(cafePage);

        when(userValidator.validateUserExists(owner.getUserId())).thenReturn(owner);
        when(conversationRepository.findCafePageConversationsOrderByLatestActivity(cafePage.getId()))
                .thenReturn(List.of(conversation));
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(visitor, MemberRole.MEMBER), member(owner, MemberRole.OWNER)));

        var result = chatService.getCafePageConversations(cafePage.getId(), owner.getUserId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getType()).isEqualTo(ConversationType.CAFE_PAGE);
        assertThat(result.getFirst().getTargetType()).isEqualTo(ChatTargetType.CAFE_PAGE);
        assertThat(result.getFirst().getTargetCafePageId()).isEqualTo(cafePage.getId());
        assertThat(result.getFirst().isCanReplyAsCafePage()).isTrue();
        verify(cafePageValidator, atLeastOnce()).validateUserCanManagePage(cafePage.getId(), owner.getUserId());
    }

    @Test
    void sendMessage_success_usesCafePageSenderContext_TC005() {
        User visitor = user(UUID.randomUUID(), "visitor");
        User owner = user(UUID.randomUUID(), "owner");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        Conversation conversation = conversation(ConversationType.CAFE_PAGE);
        conversation.setCafePage(cafePage);
        SendMessageRequestDTO request = sendTextRequest(owner.getUserId(), "Hello from page");
        request.setSenderContextType(ChatSenderContextType.CAFE_PAGE);
        request.setSenderCafePageId(cafePage.getId());

        mockMessageSave();
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(owner.getUserId())).thenReturn(owner);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(true);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(visitor, MemberRole.MEMBER), member(owner, MemberRole.OWNER)));

        var result = chatService.sendMessage(conversation.getId(), request);

        assertThat(result.getSenderId()).isEqualTo(owner.getUserId());
        assertThat(result.getSenderContextType()).isEqualTo(ChatSenderContextType.CAFE_PAGE);
        assertThat(result.getSenderCafePageId()).isEqualTo(cafePage.getId());
        assertThat(result.getSenderDisplayName()).isEqualTo(cafePage.getName());
        assertThat(result.getSenderAvatar()).isEqualTo(cafePage.getAvatarUrl());
        verify(cafePageValidator, atLeastOnce()).validateUserCanManagePage(cafePage.getId(), owner.getUserId());
        verify(firebaseChatService).saveMessage(result);
    }

    @Test
    void sendMessage_error_rejectsCafePageSenderInDirectConversation_TC006() {
        User firstUser = user(UUID.randomUUID(), "first");
        Conversation conversation = conversation(ConversationType.DIRECT);
        SendMessageRequestDTO request = sendTextRequest(firstUser.getUserId(), "Hello");
        request.setSenderContextType(ChatSenderContextType.CAFE_PAGE);
        request.setSenderCafePageId(UUID.randomUUID());

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), firstUser.getUserId()))
                .thenReturn(true);

        assertThatThrownBy(() -> chatService.sendMessage(conversation.getId(), request))
                .hasMessageContaining("Cafe page sender is only valid in cafe page conversations");

        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
        verify(cafePageValidator, never()).validateUserCanManagePage(any(UUID.class), any(UUID.class));
        verify(firebaseChatService, never()).saveMessage(any());
        verify(conversationRepository, never()).save(conversation);
    }

    private void mockConversationSave() {
        doAnswer(invocation -> {
            Conversation conversation = invocation.getArgument(0);
            conversation.setId(UUID.randomUUID());
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdatedAt(conversation.getCreatedAt());
            return conversation;
        }).when(conversationRepository).save(any(Conversation.class));
    }

    private void mockMemberSave() {
        doAnswer(invocation -> {
            ChatMember member = invocation.getArgument(0);
            member.setId(UUID.randomUUID());
            member.setJoinedAt(LocalDateTime.now());
            return member;
        }).when(chatMemberRepository).save(any(ChatMember.class));
    }

    private void mockMessageSave() {
        doAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            message.setCreatedAt(LocalDateTime.now());
            message.setUpdatedAt(message.getCreatedAt());
            return message;
        }).when(chatMessageRepository).save(any(ChatMessage.class));
    }

    private SendMessageRequestDTO sendTextRequest(UUID senderId, String text) {
        SendMessageRequestDTO request = new SendMessageRequestDTO();
        request.setSenderId(senderId);
        request.setType(MessageType.TEXT);
        request.setText(text);
        return request;
    }

    private User user(UUID userId, String username) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName(username);
        user.setUserFullName(username + " full");
        user.setUserAvatar("https://cdn.example.com/" + username + ".png");
        user.setUserEmail(username + "@example.com");
        user.setUserPassword("secret");
        user.setAccountStatus(true);
        return user;
    }

    private CafePage cafePage(UUID cafePageId, User owner) {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setOwner(owner);
        cafePage.setName("Cafe Story Nguyen Hue");
        cafePage.setAddress("Nguyen Hue");
        cafePage.setAvatarUrl("https://cdn.example.com/cafe-page.png");
        return cafePage;
    }

    private Conversation conversation(ConversationType type) {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setType(type);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(conversation.getCreatedAt());
        return conversation;
    }

    private ChatMember member(User user, MemberRole role) {
        ChatMember member = new ChatMember();
        member.setId(UUID.randomUUID());
        member.setUser(user);
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());
        return member;
    }
}
