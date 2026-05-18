package com.cafestory.chat;

import com.cafestory.dto.requestDTO.chat.CreateDirectConversationRequest;
import com.cafestory.dto.requestDTO.chat.CreateGroupConversationRequest;
import com.cafestory.dto.requestDTO.chat.SendMessageRequest;
import com.cafestory.dto.requestDTO.chat.UpdateGroupInfoRequest;
import com.cafestory.dto.responseDTO.chat.ChatMessageResponseDTO;
import com.cafestory.entity.ChatMember;
import com.cafestory.entity.ChatMessage;
import com.cafestory.entity.Conversation;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ConversationType;
import com.cafestory.entity.enums.MemberRole;
import com.cafestory.entity.enums.MessageType;
import com.cafestory.mapper.ChatMapper;
import com.cafestory.repository.ChatMemberRepository;
import com.cafestory.repository.ChatMessageRepository;
import com.cafestory.repository.ConversationRepository;
import com.cafestory.service.serviceImplement.ChatServiceImpl;
import com.cafestory.service.serviceInterface.FirebaseChatService;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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
    private UserValidator userValidator;

    @Mock
    private FirebaseChatService firebaseChatService;

    @Mock
    private NotificationService notificationService;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(
                conversationRepository,
                chatMemberRepository,
                chatMessageRepository,
                userValidator,
                new ChatMapper(),
                firebaseChatService,
                notificationService);
    }

    @Test
    void createOrGetDirectConversation_success_createsWhenMissing_TC001() {
        User firstUser = user(UUID.randomUUID());
        User secondUser = user(UUID.randomUUID());
        CreateDirectConversationRequest request = new CreateDirectConversationRequest();
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
        assertThat(result.getMembers()).hasSize(2);
        verify(firebaseChatService).saveConversation(result);
    }

    @Test
    void createGroupConversation_success_TC002() {
        User owner = user(UUID.randomUUID());
        User member = user(UUID.randomUUID());
        CreateGroupConversationRequest request = new CreateGroupConversationRequest();
        request.setCreatorUserId(owner.getUserId());
        request.setGroupName("Cafe team");
        request.setGroupAvatar("https://example.com/group.png");
        request.setMemberIds(List.of(owner.getUserId(), member.getUserId()));
        mockConversationSave();
        mockMemberSave();

        when(userValidator.validateUserExists(owner.getUserId())).thenReturn(owner);
        when(userValidator.validateUserExists(member.getUserId())).thenReturn(member);
        when(chatMemberRepository.findByConversationId(any(UUID.class)))
                .thenReturn(List.of(member(owner, MemberRole.OWNER), member(member, MemberRole.MEMBER)));

        var result = chatService.createGroupConversation(request);

        assertThat(result.getType()).isEqualTo(ConversationType.GROUP);
        assertThat(result.getGroupName()).isEqualTo("Cafe team");
        assertThat(result.getMembers()).hasSize(2);
        verify(firebaseChatService).saveConversation(result);
    }

    @Test
    void sendMessage_success_text_updatesFirebaseAndLatestMessage_TC003() {
        Conversation conversation = conversation(ConversationType.DIRECT);
        User sender = user(UUID.randomUUID());
        SendMessageRequest request = sendRequest(sender.getUserId(), MessageType.TEXT);
        request.setText("hello");
        mockMessageSending(conversation, sender);

        ChatMessageResponseDTO result = chatService.sendMessage(conversation.getId(), request);

        assertThat(result.getType()).isEqualTo(MessageType.TEXT);
        assertThat(result.getText()).isEqualTo("hello");
        verify(firebaseChatService).saveMessage(result);
        verify(firebaseChatService).updateLatestMessage(any(), any());
        verify(notificationService).createMessageNotification(any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class));
        verify(conversationRepository).save(conversation);
        assertThat(conversation.getLatestMessagePreview()).isEqualTo("hello");
    }

    @Test
    void sendMessage_success_multipleImageUrls_TC004() {
        Conversation conversation = conversation(ConversationType.GROUP);
        User sender = user(UUID.randomUUID());
        SendMessageRequest request = sendRequest(sender.getUserId(), MessageType.IMAGE);
        request.setImageUrls(List.of("https://example.com/a.png", "https://example.com/b.png"));
        mockMessageSending(conversation, sender);

        ChatMessageResponseDTO result = chatService.sendMessage(conversation.getId(), request);

        assertThat(result.getType()).isEqualTo(MessageType.IMAGE);
        assertThat(result.getImageUrls()).containsExactly("https://example.com/a.png", "https://example.com/b.png");
    }

    @Test
    void sendMessage_success_sticker_TC005() {
        Conversation conversation = conversation(ConversationType.DIRECT);
        User sender = user(UUID.randomUUID());
        SendMessageRequest request = sendRequest(sender.getUserId(), MessageType.STICKER);
        request.setStickerId("latte-smile");
        mockMessageSending(conversation, sender);

        ChatMessageResponseDTO result = chatService.sendMessage(conversation.getId(), request);

        assertThat(result.getType()).isEqualTo(MessageType.STICKER);
        assertThat(result.getStickerId()).isEqualTo("latte-smile");
    }

    @Test
    void sendMessage_fail_userNotInConversation_TC006() {
        Conversation conversation = conversation(ConversationType.DIRECT);
        User sender = user(UUID.randomUUID());
        SendMessageRequest request = sendRequest(sender.getUserId(), MessageType.TEXT);
        request.setText("blocked");

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), sender.getUserId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage(conversation.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void addAndRemoveMember_success_ownerCanManage_TC007() {
        Conversation conversation = conversation(ConversationType.GROUP);
        User owner = user(UUID.randomUUID());
        User newMember = user(UUID.randomUUID());
        ChatMember ownerMember = member(owner, MemberRole.OWNER);
        ownerMember.setConversation(conversation);
        ChatMember removableMember = member(newMember, MemberRole.MEMBER);
        removableMember.setConversation(conversation);
        mockMemberSave();

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), owner.getUserId()))
                .thenReturn(Optional.of(ownerMember));
        when(chatMemberRepository.existsByConversationIdAndUserUserId(conversation.getId(), newMember.getUserId()))
                .thenReturn(false);
        when(userValidator.validateUserExists(newMember.getUserId())).thenReturn(newMember);
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(ownerMember, removableMember));

        var added = chatService.addMember(conversation.getId(), owner.getUserId(), newMember.getUserId());

        assertThat(added.getMembers()).hasSize(2);

        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), newMember.getUserId()))
                .thenReturn(Optional.of(removableMember));

        var removed = chatService.removeMember(conversation.getId(), owner.getUserId(), newMember.getUserId());

        assertThat(removed.getType()).isEqualTo(ConversationType.GROUP);
        verify(chatMemberRepository).delete(removableMember);
    }

    @Test
    void leaveGroup_success_memberLeaves_TC008() {
        Conversation conversation = conversation(ConversationType.GROUP);
        User memberUser = user(UUID.randomUUID());
        ChatMember chatMember = member(memberUser, MemberRole.MEMBER);
        chatMember.setConversation(conversation);

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), memberUser.getUserId()))
                .thenReturn(Optional.of(chatMember));
        when(chatMemberRepository.findByConversationId(conversation.getId())).thenReturn(List.of());

        chatService.leaveGroup(conversation.getId(), memberUser.getUserId());

        verify(chatMemberRepository).delete(chatMember);
        verify(firebaseChatService).saveConversation(any());
    }

    @Test
    void updateGroupInfo_fail_memberHasNoPermission_TC009() {
        Conversation conversation = conversation(ConversationType.GROUP);
        User memberUser = user(UUID.randomUUID());
        ChatMember chatMember = member(memberUser, MemberRole.MEMBER);
        chatMember.setConversation(conversation);
        UpdateGroupInfoRequest request = new UpdateGroupInfoRequest();
        request.setActorUserId(memberUser.getUserId());
        request.setGroupName("New name");

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), memberUser.getUserId()))
                .thenReturn(Optional.of(chatMember));

        assertThatThrownBy(() -> chatService.updateGroupInfo(conversation.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void sendMessage_fail_invalidPayloadCases_TC010() {
        Conversation conversation = conversation(ConversationType.DIRECT);
        User sender = user(UUID.randomUUID());
        SendMessageRequest emptyText = sendRequest(sender.getUserId(), MessageType.TEXT);

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), sender.getUserId()))
                .thenReturn(Optional.of(member(sender, MemberRole.MEMBER)));

        assertThatThrownBy(() -> chatService.sendMessage(conversation.getId(), emptyText))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        SendMessageRequest badImage = sendRequest(sender.getUserId(), MessageType.IMAGE);
        badImage.setImageUrls(new ArrayList<>(List.of("https://example.com/a.png")));
        badImage.getImageUrls().add(" ");

        assertThatThrownBy(() -> chatService.sendMessage(conversation.getId(), badImage))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private void mockMessageSending(Conversation conversation, User sender) {
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userValidator.validateUserExists(sender.getUserId())).thenReturn(sender);
        when(chatMemberRepository.findByConversationIdAndUserUserId(conversation.getId(), sender.getUserId()))
                .thenReturn(Optional.of(member(sender, MemberRole.MEMBER)));
        mockMessageSave();
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        User recipient = user(UUID.randomUUID());
        when(chatMemberRepository.findByConversationId(conversation.getId()))
                .thenReturn(List.of(member(sender, MemberRole.MEMBER), member(recipient, MemberRole.MEMBER)));
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

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("user-" + userId);
        user.setUserEmail(userId + "@example.com");
        user.setUserPassword("secret");
        user.setAccountStatus(true);
        return user;
    }

    private Conversation conversation(ConversationType type) {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setType(type);
        conversation.setGroupName(type == ConversationType.GROUP ? "Cafe group" : null);
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

    private SendMessageRequest sendRequest(UUID senderId, MessageType type) {
        SendMessageRequest request = new SendMessageRequest();
        request.setSenderId(senderId);
        request.setType(type);
        return request;
    }
}
