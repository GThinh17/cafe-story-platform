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
import com.cafestory.dto.requestDTO.CreateGroupConversationRequestDTO;
import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.dto.requestDTO.UpdateGroupInfoRequestDTO;
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
import com.cafestory.entity.enums.PageMemberStatus;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
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

    @Test
    void createOrGetDirectConversation_fail_sameUserOnBothSides_TC007() {
        UUID userId = UUID.randomUUID();
        CreateDirectConversationRequestDTO request = new CreateDirectConversationRequestDTO();
        request.setFirstUserId(userId);
        request.setSecondUserId(userId);

        assertThatThrownBy(() -> chatService.createOrGetDirectConversation(request))
                .hasMessageContaining("Direct conversation requires two different users");
    }

    @Test
    void createOrGetDirectConversation_success_reusesExistingConversation_TC008() {
        User firstUser = user(UUID.randomUUID(), "first");
        User secondUser = user(UUID.randomUUID(), "second");
        Conversation conversation = conversation(ConversationType.DIRECT);
        CreateDirectConversationRequestDTO request = new CreateDirectConversationRequestDTO();
        request.setFirstUserId(firstUser.getUserId());
        request.setSecondUserId(secondUser.getUserId());

        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(userValidator.validateUserExists(secondUser.getUserId())).thenReturn(secondUser);
        when(conversationRepository.findDirectConversation(firstUser.getUserId(), secondUser.getUserId()))
                .thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(firstUser, MemberRole.MEMBER), member(secondUser, MemberRole.MEMBER)));

        assertThat(chatService.createOrGetDirectConversation(request).getId()).isEqualTo(conversation.getId());
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void createOrGetCafePageConversation_fail_cafePageNotFound_TC009() {
        CreateCafePageConversationRequestDTO request = new CreateCafePageConversationRequestDTO();
        request.setUserId(UUID.randomUUID());
        request.setCafePageId(UUID.randomUUID());
        when(cafePageRepository.findById(request.getCafePageId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.createOrGetCafePageConversation(request))
                .hasMessageContaining("Cafe page not found");
    }

    @Test
    void createOrGetCafePageConversation_fail_ownerMissing_TC010() {
        CafePage cafePage = cafePage(UUID.randomUUID(), null);
        User visitor = user(UUID.randomUUID(), "visitor");
        CreateCafePageConversationRequestDTO request = new CreateCafePageConversationRequestDTO();
        request.setUserId(visitor.getUserId());
        request.setCafePageId(cafePage.getId());
        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(userValidator.validateUserExists(visitor.getUserId())).thenReturn(visitor);

        assertThatThrownBy(() -> chatService.createOrGetCafePageConversation(request))
                .hasMessageContaining("Cafe page owner is missing");
    }

    @Test
    void createOrGetCafePageConversation_fail_ownerChatsWithOwnPage_TC011() {
        User owner = user(UUID.randomUUID(), "owner");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        CreateCafePageConversationRequestDTO request = new CreateCafePageConversationRequestDTO();
        request.setUserId(owner.getUserId());
        request.setCafePageId(cafePage.getId());
        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(userValidator.validateUserExists(owner.getUserId())).thenReturn(owner);

        assertThatThrownBy(() -> chatService.createOrGetCafePageConversation(request))
                .hasMessageContaining("Direct conversation requires two different users");
    }

    @Test
    void createGroupConversation_success_creatorIsOwnerAndIdsAreDeduplicated_TC012() {
        User creator = user(UUID.randomUUID(), "creator");
        User firstMember = user(UUID.randomUUID(), "member1");
        CreateGroupConversationRequestDTO request = new CreateGroupConversationRequestDTO();
        request.setCreatorUserId(creator.getUserId());
        request.setGroupName("  Nhom quan ca phe  ");
        request.setGroupAvatar("   ");
        request.setMemberIds(List.of(firstMember.getUserId(), firstMember.getUserId(), creator.getUserId()));

        mockConversationSave();
        mockMemberSave();
        when(userValidator.validateUserExists(creator.getUserId())).thenReturn(creator);
        when(userValidator.validateUserExists(firstMember.getUserId())).thenReturn(firstMember);
        when(chatMemberRepository.findByConversationId(any(UUID.class)))
                .thenReturn(List.of(member(creator, MemberRole.OWNER), member(firstMember, MemberRole.MEMBER)));

        var result = chatService.createGroupConversation(request);

        assertThat(result.getType()).isEqualTo(ConversationType.GROUP);
        assertThat(result.getChatName()).isEqualTo("Nhom quan ca phe");
        assertThat(result.getChatAvatar()).isNull();
        // Chủ nhóm + 1 thành viên: id trùng và chính chủ nhóm đều bị loại.
        verify(chatMemberRepository, org.mockito.Mockito.times(2)).save(any(ChatMember.class));
        verify(firebaseChatService).saveConversation(result);
    }

    @Test
    void createGroupConversation_fail_memberIdsMissing_TC013() {
        User creator = user(UUID.randomUUID(), "creator");
        CreateGroupConversationRequestDTO request = new CreateGroupConversationRequestDTO();
        request.setCreatorUserId(creator.getUserId());
        request.setGroupName("Nhom");
        request.setMemberIds(List.of());

        mockConversationSave();
        mockMemberSave();
        when(userValidator.validateUserExists(creator.getUserId())).thenReturn(creator);

        assertThatThrownBy(() -> chatService.createGroupConversation(request))
                .hasMessageContaining("Member ids are mandatory");
    }

    @Test
    void getUserConversations_success_listsConversationsOfViewer_TC014() {
        User viewer = user(UUID.randomUUID(), "viewer");
        Conversation conversation = conversation(ConversationType.GROUP);
        conversation.setGroupName("Nhom");
        when(userValidator.validateUserExists(viewer.getUserId())).thenReturn(viewer);
        when(conversationRepository.findUserConversationsOrderByLatestActivity(
                viewer.getUserId(), PageMemberStatus.ACTIVE, List.of("OWNER", "CO_OWNER")))
                .thenReturn(List.of(conversation));
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(viewer, MemberRole.OWNER)));

        var result = chatService.getUserConversations(viewer.getUserId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().isCanReplyAsCafePage()).isFalse();
    }

    @Test
    void getMessagesByConversationId_success_sanitizesPaging_TC015() {
        User member = user(UUID.randomUUID(), "member");
        Conversation conversation = conversation(ConversationType.GROUP);
        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setConversation(conversation);
        message.setSender(member);
        message.setType(MessageType.TEXT);
        message.setText("xin chao");
        message.setCreatedAt(LocalDateTime.now());

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), member.getUserId()))
                .thenReturn(true);
        when(chatMessageRepository.findByConversationId(
                org.mockito.ArgumentMatchers.eq(conversation.getId()), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(message)));

        var result = chatService.getMessagesByConversationId(conversation.getId(), member.getUserId(), -5, 500);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getText()).isEqualTo("xin chao");

        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(chatMessageRepository).findByConversationId(
                org.mockito.ArgumentMatchers.eq(conversation.getId()), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void getMessagesByConversationId_fail_conversationIdMissing_TC016() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> chatService.getMessagesByConversationId(null, userId, 0, 20))
                .hasMessageContaining("Conversation id is required");
    }

    @Test
    void getMessagesByConversationId_fail_conversationNotFound_TC017() {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessagesByConversationId(conversationId, userId, 0, 20))
                .hasMessageContaining("Conversation not found");
    }

    @Test
    void getMessagesByConversationId_fail_outsiderOnNonPageConversation_TC018() {
        Conversation conversation = conversation(ConversationType.GROUP);
        UUID outsiderId = UUID.randomUUID();
        UUID conversationId = conversation.getId();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversationId, outsiderId))
                .thenReturn(false);

        assertThatThrownBy(() -> chatService.getMessagesByConversationId(conversationId, outsiderId, 0, 20))
                .hasMessageContaining("User not in conversation");
    }

    @Test
    void getMessagesByConversationId_success_pageManagerReadsPageMailbox_TC019() {
        User owner = user(UUID.randomUUID(), "owner");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        Conversation conversation = conversation(ConversationType.CAFE_PAGE);
        conversation.setCafePage(cafePage);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(false);
        when(chatMessageRepository.findByConversationId(
                org.mockito.ArgumentMatchers.eq(conversation.getId()), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(chatService.getMessagesByConversationId(conversation.getId(), owner.getUserId(), 0, 20))
                .isEmpty();
        verify(cafePageValidator).validateUserCanManagePage(cafePage.getId(), owner.getUserId());
    }

    @Test
    void sendMessage_success_notifiesOtherMembersAndSwallowsNotificationErrors_TC020() {
        User sender = user(UUID.randomUUID(), "sender");
        User receiver = user(UUID.randomUUID(), "receiver");
        Conversation conversation = conversation(ConversationType.GROUP);
        SendMessageRequestDTO request = sendTextRequest(sender.getUserId(), "  xin chao  ");

        mockMessageSave();
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), sender.getUserId()))
                .thenReturn(true);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(sender, MemberRole.OWNER), member(receiver, MemberRole.MEMBER)));
        doThrow(new IllegalStateException("notification down"))
                .when(notificationService)
                .createMessageNotification(
                        org.mockito.ArgumentMatchers.eq(receiver.getUserId()),
                        any(UUID.class), any(UUID.class), any(UUID.class));

        var result = chatService.sendMessage(conversation.getId(), request);

        assertThat(result.getText()).isEqualTo("xin chao");
        assertThat(conversation.getLatestMessagePreview()).isEqualTo("xin chao");
        verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("/topic/conversations/" + conversation.getId()),
                any(Object.class));
        verify(notificationService).createMessageNotification(
                org.mockito.ArgumentMatchers.eq(receiver.getUserId()),
                any(UUID.class), any(UUID.class), any(UUID.class));
        verify(notificationService, never()).createMessageNotification(
                org.mockito.ArgumentMatchers.eq(sender.getUserId()),
                any(UUID.class), any(UUID.class), any(UUID.class));
    }

    @Test
    void sendMessage_success_imagePreviewFallsBackToPlaceholder_TC021() {
        User sender = user(UUID.randomUUID(), "sender");
        Conversation conversation = conversation(ConversationType.GROUP);
        SendMessageRequestDTO request = new SendMessageRequestDTO();
        request.setSenderId(sender.getUserId());
        request.setType(MessageType.IMAGE);
        request.setImageUrls(List.of("https://cdn.example.com/1.png"));

        mockMessageSave();
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), sender.getUserId()))
                .thenReturn(true);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(sender, MemberRole.OWNER)));

        chatService.sendMessage(conversation.getId(), request);

        assertThat(conversation.getLatestMessagePreview()).isEqualTo("[image]");
    }

    @Test
    void sendMessage_success_stickerPreviewFallsBackToPlaceholder_TC022() {
        User sender = user(UUID.randomUUID(), "sender");
        Conversation conversation = conversation(ConversationType.GROUP);
        SendMessageRequestDTO request = new SendMessageRequestDTO();
        request.setSenderId(sender.getUserId());
        request.setType(MessageType.STICKER);
        request.setStickerId("sticker-1");

        mockMessageSave();
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), sender.getUserId()))
                .thenReturn(true);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(sender, MemberRole.OWNER)));

        chatService.sendMessage(conversation.getId(), request);

        assertThat(conversation.getLatestMessagePreview()).isEqualTo("[sticker]");
    }

    @Test
    void sendMessage_success_mixedWithoutTextUsesGenericPreview_TC023() {
        User sender = user(UUID.randomUUID(), "sender");
        Conversation conversation = conversation(ConversationType.GROUP);
        SendMessageRequestDTO request = new SendMessageRequestDTO();
        request.setSenderId(sender.getUserId());
        request.setType(MessageType.MIXED);
        request.setStickerUrl("https://cdn.example.com/sticker.png");

        mockMessageSave();
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), sender.getUserId()))
                .thenReturn(true);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(sender, MemberRole.OWNER)));

        chatService.sendMessage(conversation.getId(), request);

        assertThat(conversation.getLatestMessagePreview()).isEqualTo("[message]");
    }

    @Test
    void sendMessage_fail_pageManagerMustSendAsCafePage_TC024() {
        User owner = user(UUID.randomUUID(), "owner");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        Conversation conversation = conversation(ConversationType.CAFE_PAGE);
        conversation.setCafePage(cafePage);
        SendMessageRequestDTO request = sendTextRequest(owner.getUserId(), "xin chao");
        UUID conversationId = conversation.getId();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(owner.getUserId())).thenReturn(owner);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversationId, owner.getUserId()))
                .thenReturn(false);

        assertThatThrownBy(() -> chatService.sendMessage(conversationId, request))
                .hasMessageContaining("Page managers must send as cafe page");
    }

    @Test
    void sendMessage_fail_senderCafePageDoesNotMatchConversation_TC025() {
        User owner = user(UUID.randomUUID(), "owner");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        Conversation conversation = conversation(ConversationType.CAFE_PAGE);
        conversation.setCafePage(cafePage);
        SendMessageRequestDTO request = sendTextRequest(owner.getUserId(), "xin chao");
        request.setSenderContextType(ChatSenderContextType.CAFE_PAGE);
        request.setSenderCafePageId(UUID.randomUUID());
        UUID conversationId = conversation.getId();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(owner.getUserId())).thenReturn(owner);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversationId, owner.getUserId()))
                .thenReturn(true);

        assertThatThrownBy(() -> chatService.sendMessage(conversationId, request))
                .hasMessageContaining("Sender cafe page does not match conversation");
    }

    @Test
    void sendMessage_fail_invalidPayloads_TC026() {
        User sender = user(UUID.randomUUID(), "sender");
        Conversation conversation = conversation(ConversationType.GROUP);
        UUID conversationId = conversation.getId();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversationId, sender.getUserId()))
                .thenReturn(true);

        SendMessageRequestDTO missingType = new SendMessageRequestDTO();
        missingType.setSenderId(sender.getUserId());
        assertThatThrownBy(() -> chatService.sendMessage(conversationId, missingType))
                .hasMessageContaining("Invalid message type");

        SendMessageRequestDTO blankImageUrl = new SendMessageRequestDTO();
        blankImageUrl.setSenderId(sender.getUserId());
        blankImageUrl.setType(MessageType.IMAGE);
        blankImageUrl.setImageUrls(java.util.Arrays.asList("https://ok.png", "  "));
        assertThatThrownBy(() -> chatService.sendMessage(conversationId, blankImageUrl))
                .hasMessageContaining("imageUrls must contain non-empty strings");

        SendMessageRequestDTO emptyText = sendTextRequest(sender.getUserId(), "   ");
        assertThatThrownBy(() -> chatService.sendMessage(conversationId, emptyText))
                .hasMessageContaining("Text message cannot be empty");

        SendMessageRequestDTO imageWithoutUrls = new SendMessageRequestDTO();
        imageWithoutUrls.setSenderId(sender.getUserId());
        imageWithoutUrls.setType(MessageType.IMAGE);
        assertThatThrownBy(() -> chatService.sendMessage(conversationId, imageWithoutUrls))
                .hasMessageContaining("Image message requires imageUrls");

        SendMessageRequestDTO stickerWithoutPayload = new SendMessageRequestDTO();
        stickerWithoutPayload.setSenderId(sender.getUserId());
        stickerWithoutPayload.setType(MessageType.STICKER);
        assertThatThrownBy(() -> chatService.sendMessage(conversationId, stickerWithoutPayload))
                .hasMessageContaining("Sticker message requires stickerUrl or stickerId");

        SendMessageRequestDTO emptyMixed = new SendMessageRequestDTO();
        emptyMixed.setSenderId(sender.getUserId());
        emptyMixed.setType(MessageType.MIXED);
        assertThatThrownBy(() -> chatService.sendMessage(conversationId, emptyMixed))
                .hasMessageContaining("Message cannot be empty");

        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    // --------------------------------------------------------- Quản lý nhóm

    @Test
    void addMember_success_ownerAddsNewMember_TC027() {
        User owner = user(UUID.randomUUID(), "owner");
        User newMember = user(UUID.randomUUID(), "newbie");
        Conversation conversation = conversation(ConversationType.GROUP);

        mockMemberSave();
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(Optional.of(member(owner, MemberRole.OWNER)));
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), newMember.getUserId()))
                .thenReturn(false);
        when(userValidator.validateUserExists(newMember.getUserId())).thenReturn(newMember);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(owner, MemberRole.OWNER), member(newMember, MemberRole.MEMBER)));

        var result = chatService.addMember(conversation.getId(), owner.getUserId(), newMember.getUserId());

        assertThat(result.getId()).isEqualTo(conversation.getId());
        verify(chatMemberRepository).save(any(ChatMember.class));
        verify(firebaseChatService).saveConversation(result);
    }

    @Test
    void addMember_success_existingMemberIsNotDuplicated_TC028() {
        User owner = user(UUID.randomUUID(), "owner");
        User existing = user(UUID.randomUUID(), "existing");
        Conversation conversation = conversation(ConversationType.GROUP);

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(Optional.of(member(owner, MemberRole.ADMIN)));
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), existing.getUserId()))
                .thenReturn(true);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(owner, MemberRole.ADMIN), member(existing, MemberRole.MEMBER)));

        chatService.addMember(conversation.getId(), owner.getUserId(), existing.getUserId());

        verify(chatMemberRepository, never()).save(any(ChatMember.class));
    }

    @Test
    void addMember_fail_conversationIsNotGroup_TC029() {
        Conversation conversation = conversation(ConversationType.DIRECT);
        UUID conversationId = conversation.getId();
        UUID actorId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> chatService.addMember(conversationId, actorId, memberId))
                .hasMessageContaining("Conversation is not a group");
    }

    @Test
    void addMember_fail_actorIsPlainMember_TC030() {
        User actor = user(UUID.randomUUID(), "actor");
        Conversation conversation = conversation(ConversationType.GROUP);
        UUID conversationId = conversation.getId();
        UUID memberId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversationId, actor.getUserId()))
                .thenReturn(Optional.of(member(actor, MemberRole.MEMBER)));

        assertThatThrownBy(() -> chatService.addMember(conversationId, actor.getUserId(), memberId))
                .hasMessageContaining("Unauthorized action");
    }

    @Test
    void addMember_fail_actorIdIsNull_TC031() {
        Conversation conversation = conversation(ConversationType.GROUP);
        UUID conversationId = conversation.getId();
        UUID memberId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> chatService.addMember(conversationId, null, memberId))
                .hasMessageContaining("User id is required");
    }

    @Test
    void addMember_fail_actorNotInConversation_TC032() {
        Conversation conversation = conversation(ConversationType.GROUP);
        UUID conversationId = conversation.getId();
        UUID actorId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversationId, actorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.addMember(conversationId, actorId, memberId))
                .hasMessageContaining("User not in conversation");
    }

    @Test
    void removeMember_success_ownerRemovesMember_TC033() {
        User owner = user(UUID.randomUUID(), "owner");
        User target = user(UUID.randomUUID(), "target");
        Conversation conversation = conversation(ConversationType.GROUP);
        ChatMember targetMember = member(target, MemberRole.MEMBER);

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(Optional.of(member(owner, MemberRole.OWNER)));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), target.getUserId()))
                .thenReturn(Optional.of(targetMember));
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(owner, MemberRole.OWNER)));

        chatService.removeMember(conversation.getId(), owner.getUserId(), target.getUserId());

        verify(chatMemberRepository).delete(targetMember);
    }

    @Test
    void removeMember_fail_cannotRemoveOwner_TC034() {
        User owner = user(UUID.randomUUID(), "owner");
        User admin = user(UUID.randomUUID(), "admin");
        Conversation conversation = conversation(ConversationType.GROUP);
        UUID conversationId = conversation.getId();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversationId, admin.getUserId()))
                .thenReturn(Optional.of(member(admin, MemberRole.ADMIN)));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversationId, owner.getUserId()))
                .thenReturn(Optional.of(member(owner, MemberRole.OWNER)));

        assertThatThrownBy(() ->
                chatService.removeMember(conversationId, admin.getUserId(), owner.getUserId()))
                .hasMessageContaining("Cannot remove group owner");
        verify(chatMemberRepository, never()).delete(any(ChatMember.class));
    }

    @Test
    void leaveGroup_success_plainMemberLeaves_TC035() {
        User member = user(UUID.randomUUID(), "member");
        Conversation conversation = conversation(ConversationType.GROUP);
        ChatMember chatMember = member(member, MemberRole.MEMBER);

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), member.getUserId()))
                .thenReturn(Optional.of(chatMember));
        when(chatMemberRepository.findByConversationId(conversation.getId())).thenReturn(List.of());

        chatService.leaveGroup(conversation.getId(), member.getUserId());

        verify(chatMemberRepository).delete(chatMember);
        verify(firebaseChatService).saveConversation(any());
    }

    @Test
    void leaveGroup_fail_ownerStillHasOtherMembers_TC036() {
        User owner = user(UUID.randomUUID(), "owner");
        Conversation conversation = conversation(ConversationType.GROUP);
        UUID conversationId = conversation.getId();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversationId, owner.getUserId()))
                .thenReturn(Optional.of(member(owner, MemberRole.OWNER)));
        when(chatMemberRepository.countByConversation(conversation)).thenReturn(3L);

        assertThatThrownBy(() -> chatService.leaveGroup(conversationId, owner.getUserId()))
                .hasMessageContaining("Owner must transfer ownership before leaving");
        verify(chatMemberRepository, never()).delete(any(ChatMember.class));
    }

    @Test
    void leaveGroup_success_lastOwnerCanLeave_TC037() {
        User owner = user(UUID.randomUUID(), "owner");
        Conversation conversation = conversation(ConversationType.GROUP);
        ChatMember ownerMember = member(owner, MemberRole.OWNER);

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(Optional.of(ownerMember));
        when(chatMemberRepository.countByConversation(conversation)).thenReturn(1L);
        when(chatMemberRepository.findByConversationId(conversation.getId())).thenReturn(List.of());

        chatService.leaveGroup(conversation.getId(), owner.getUserId());

        verify(chatMemberRepository).delete(ownerMember);
    }

    @Test
    void updateGroupInfo_success_updatesNameAndAvatar_TC038() {
        User owner = user(UUID.randomUUID(), "owner");
        Conversation conversation = conversation(ConversationType.GROUP);
        UpdateGroupInfoRequestDTO request = new UpdateGroupInfoRequestDTO();
        request.setActorUserId(owner.getUserId());
        request.setGroupName("  Nhom moi  ");
        request.setGroupAvatar("   ");

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(Optional.of(member(owner, MemberRole.OWNER)));
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(owner, MemberRole.OWNER)));

        chatService.updateGroupInfo(conversation.getId(), request);

        assertThat(conversation.getGroupName()).isEqualTo("Nhom moi");
        assertThat(conversation.getGroupAvatar()).isNull();
    }

    @Test
    void updateGroupInfo_success_keepsFieldsWhenRequestOmitsThem_TC039() {
        User owner = user(UUID.randomUUID(), "owner");
        Conversation conversation = conversation(ConversationType.GROUP);
        conversation.setGroupName("Nhom cu");
        conversation.setGroupAvatar("https://cdn.example.com/old.png");
        UpdateGroupInfoRequestDTO request = new UpdateGroupInfoRequestDTO();
        request.setActorUserId(owner.getUserId());

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(Optional.of(member(owner, MemberRole.OWNER)));
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(owner, MemberRole.OWNER)));

        chatService.updateGroupInfo(conversation.getId(), request);

        assertThat(conversation.getGroupName()).isEqualTo("Nhom cu");
        assertThat(conversation.getGroupAvatar()).isEqualTo("https://cdn.example.com/old.png");
    }

    @Test
    void updateGroupInfo_fail_blankGroupName_TC040() {
        User owner = user(UUID.randomUUID(), "owner");
        Conversation conversation = conversation(ConversationType.GROUP);
        UUID conversationId = conversation.getId();
        UpdateGroupInfoRequestDTO request = new UpdateGroupInfoRequestDTO();
        request.setActorUserId(owner.getUserId());
        request.setGroupName("   ");

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversationId, owner.getUserId()))
                .thenReturn(Optional.of(member(owner, MemberRole.OWNER)));

        assertThatThrownBy(() -> chatService.updateGroupInfo(conversationId, request))
                .hasMessageContaining("Group name cannot be blank");
    }

    @Test
    void toConversationResponse_success_countsUnreadForOtherViewer_TC041() {
        User sender = user(UUID.randomUUID(), "sender");
        User viewer = user(UUID.randomUUID(), "viewer");
        Conversation conversation = conversation(ConversationType.GROUP);
        ChatMessage latest = new ChatMessage();
        latest.setId(UUID.randomUUID());
        latest.setConversation(conversation);
        latest.setSender(sender);
        latest.setType(MessageType.TEXT);
        latest.setText("chua doc");
        latest.setRead(false);
        latest.setCreatedAt(LocalDateTime.now());
        conversation.setLatestMessageId(latest.getId());

        when(userValidator.validateUserExists(viewer.getUserId())).thenReturn(viewer);
        when(conversationRepository.findUserConversationsOrderByLatestActivity(
                org.mockito.ArgumentMatchers.eq(viewer.getUserId()), any(), any()))
                .thenReturn(List.of(conversation));
        when(chatMessageRepository.findById(latest.getId())).thenReturn(Optional.of(latest));
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(sender, MemberRole.OWNER), member(viewer, MemberRole.MEMBER)));

        var result = chatService.getUserConversations(viewer.getUserId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUnreadCount()).isEqualTo(1);
    }

    @Test
    void canReplyAsCafePage_success_falseWhenViewerCannotManagePage_TC042() {
        User outsider = user(UUID.randomUUID(), "outsider");
        User owner = user(UUID.randomUUID(), "owner");
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        Conversation conversation = conversation(ConversationType.CAFE_PAGE);
        conversation.setCafePage(cafePage);

        when(userValidator.validateUserExists(outsider.getUserId())).thenReturn(outsider);
        when(conversationRepository.findUserConversationsOrderByLatestActivity(
                org.mockito.ArgumentMatchers.eq(outsider.getUserId()), any(), any()))
                .thenReturn(List.of(conversation));
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(outsider, MemberRole.MEMBER), member(owner, MemberRole.OWNER)));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized"))
                .when(cafePageValidator)
                .validateUserCanManagePage(cafePage.getId(), outsider.getUserId());

        var result = chatService.getUserConversations(outsider.getUserId());

        assertThat(result.getFirst().isCanReplyAsCafePage()).isFalse();
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
