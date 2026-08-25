package com.cafestory.service;

import com.cafestory.dto.requestDTO.CreateNotificationRequestDTO;
import com.cafestory.entity.Notification;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.NotificationTargetType;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.NotificationType;
import com.cafestory.mapper.NotificationMapper;
import com.cafestory.repository.NotificationRepository;
import com.cafestory.service.model.ActorContext;
import com.cafestory.service.serviceImplement.NotificationServiceImpl;
import com.cafestory.service.serviceInterface.NotificationRealtimeService;
import com.cafestory.validation.ActorContextResolver;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private NotificationRealtimeService notificationRealtimeService;

    @Mock
    private ActorContextResolver actorContextResolver;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                userValidator,
                actorContextResolver,
                new NotificationMapper(),
                notificationRealtimeService);
    }

    @Test
    void createLikeNotification_success_withBlogNavigation_TC001() {
        UUID blogId = UUID.randomUUID();
        mockCreate(NotificationType.LIKE);

        var result = notificationService.createLikeNotification(recipientId(), actorId(), blogId);

        assertThat(result.getType()).isEqualTo(NotificationType.LIKE);
        assertThat(result.getBlogId()).isEqualTo(blogId);
        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.BLOG);
        assertThat(result.getNavigation().getTargetId()).isEqualTo(blogId);
        assertThat(result.getNavigation().getAction()).isEqualTo("open_blog");
        verify(notificationRealtimeService).emitNewNotification(eq(recipientId()), any());
        verify(notificationRealtimeService).emitUnreadCountUpdated(eq(recipientId()), any(Long.class));
    }

    @Test
    void createShareNotification_success_withBlogId_TC002() {
        UUID blogId = UUID.randomUUID();
        mockCreate(NotificationType.SHARE);

        var result = notificationService.createShareNotification(recipientId(), actorId(), blogId);

        assertThat(result.getType()).isEqualTo(NotificationType.SHARE);
        assertThat(result.getNavigation().getTargetId()).isEqualTo(blogId);
    }

    @Test
    void createCommentNotification_success_withBlogIdAndCommentId_TC003() {
        UUID blogId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        mockCreate(NotificationType.COMMENT);

        var result = notificationService.createCommentNotification(recipientId(), actorId(), blogId, commentId);

        assertThat(result.getType()).isEqualTo(NotificationType.COMMENT);
        assertThat(result.getBlogId()).isEqualTo(blogId);
        assertThat(result.getCommentId()).isEqualTo(commentId);
    }

    @Test
    void createMessageNotification_success_withConversationNavigation_TC004() {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        mockCreate(NotificationType.MESSAGE);

        var result = notificationService.createMessageNotification(recipientId(), actorId(), conversationId, messageId);

        assertThat(result.getType()).isEqualTo(NotificationType.MESSAGE);
        assertThat(result.getConversationId()).isEqualTo(conversationId);
        assertThat(result.getMessageId()).isEqualTo(messageId);
        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.CONVERSATION);
        assertThat(result.getNavigation().getAction()).isEqualTo("open_conversation");
    }

    @Test
    void createFollowNotification_success_withUserNavigation_TC005() {
        UUID userId = UUID.randomUUID();
        mockCreate(NotificationType.FOLLOW);

        var result = notificationService.createFollowNotification(recipientId(), actorId(), userId);

        assertThat(result.getType()).isEqualTo(NotificationType.FOLLOW);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.USER);
        assertThat(result.getNavigation().getAction()).isEqualTo("open_user_profile");
    }

    @Test
    void createTagNotification_success_withBlogNavigation_TC006() {
        UUID blogId = UUID.randomUUID();
        mockCreate(NotificationType.TAG);

        var result = notificationService.createTagNotification(recipientId(), actorId(), blogId);

        assertThat(result.getType()).isEqualTo(NotificationType.TAG);
        assertThat(result.getBlogId()).isEqualTo(blogId);
        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.BLOG);
        assertThat(result.getNavigation().getTargetId()).isEqualTo(blogId);
        assertThat(result.getNavigation().getAction()).isEqualTo("open_blog");
    }

    @Test
    void createNotification_fail_rejectMissingTargets_TC007() {
        assertThatThrownBy(() -> notificationService.createLikeNotification(recipientId(), actorId(), null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> notificationService.createMessageNotification(recipientId(), actorId(), null, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> notificationService.createFollowNotification(recipientId(), actorId(), null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void createNotification_fail_invalidTypeAndMismatchedTarget_TC008() {
        CreateNotificationRequestDTO invalidType = new CreateNotificationRequestDTO();
        invalidType.setRecipientId(recipientId());
        invalidType.setActorId(actorId());

        assertThatThrownBy(() -> notificationService.createNotification(invalidType))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        CreateNotificationRequestDTO mismatched = baseRequest(NotificationType.MESSAGE);
        mismatched.setConversationId(UUID.randomUUID());
        mismatched.setBlogId(UUID.randomUUID());

        assertThatThrownBy(() -> notificationService.createNotification(mismatched))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createNotification_success_doNotNotifySelf_TC009() {
        UUID selfId = UUID.randomUUID();
        User self = user(selfId);
        when(actorContextResolver.resolve(selfId, ActorContextType.USER, null))
                .thenReturn(new ActorContext(self, ActorContextType.USER, null));

        var result = notificationService.createLikeNotification(selfId, selfId, UUID.randomUUID());

        assertThat(result).isNull();
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_success_paginationAndFilters_TC010() {
        Notification notification = notification(NotificationType.MESSAGE, false);
        when(userValidator.validateUserExists(recipientId())).thenReturn(user(recipientId()));
        when(notificationRepository.findByRecipientUserId(eq(recipientId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));
        when(notificationRepository.findByRecipientUserIdAndType(eq(recipientId()), eq(NotificationType.MESSAGE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));
        when(notificationRepository.findByRecipientUserIdAndIsRead(eq(recipientId()), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));
        when(notificationRepository.findByRecipientUserIdAndTypeAndIsRead(eq(recipientId()), eq(NotificationType.MESSAGE), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        assertThat(notificationService.getUserNotifications(recipientId(), 0, 10, null, null)).hasSize(1);
        assertThat(notificationService.getUserNotifications(recipientId(), 0, 10, null, NotificationType.MESSAGE)).hasSize(1);
        assertThat(notificationService.getUserNotifications(recipientId(), 0, 10, false, null)).hasSize(1);
        assertThat(notificationService.getUserNotifications(recipientId(), 0, 10, false, NotificationType.MESSAGE)).hasSize(1);
    }

    @Test
    void unreadCountAndReadDeleteActions_success_TC011() {
        Notification notification = notification(NotificationType.FOLLOW, false);
        when(userValidator.validateUserExists(recipientId())).thenReturn(user(recipientId()));
        when(notificationRepository.countByRecipientUserIdAndIsRead(recipientId(), false)).thenReturn(2L, 1L, 0L);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(notificationRepository.findByRecipientUserIdAndIsRead(recipientId(), false)).thenReturn(List.of(notification));

        assertThat(notificationService.getUnreadCount(recipientId())).isEqualTo(2);

        var read = notificationService.markAsRead(recipientId(), notification.getId());
        assertThat(read.getIsRead()).isTrue();

        notification.setIsRead(false);
        notificationService.markAllAsRead(recipientId());
        assertThat(notification.getIsRead()).isTrue();

        notificationService.deleteNotification(recipientId(), notification.getId());
        verify(notificationRepository).delete(notification);
    }

    @Test
    void ownedActions_fail_userCannotAccessAnotherUsersNotification_TC012() {
        UUID otherUserId = UUID.randomUUID();
        Notification notification = notification(NotificationType.LIKE, false);
        when(userValidator.validateUserExists(otherUserId)).thenReturn(user(otherUserId));
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(otherUserId, notification.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void createFollowPageNotification_success_withCafePageNavigation_TC013() {
        UUID cafePageId = UUID.randomUUID();
        mockCreate(NotificationType.FOLLOW);

        var result = notificationService.createFollowPageNotification(recipientId(), actorId(), cafePageId);

        assertThat(result.getType()).isEqualTo(NotificationType.FOLLOW);
        assertThat(result.getCafePageId()).isEqualTo(cafePageId);
        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.CAFE_PAGE);
        assertThat(result.getNavigation().getTargetId()).isEqualTo(cafePageId);
        assertThat(result.getNavigation().getAction()).isEqualTo("open_cafe_page");
    }

    @Test
    void createModerationNotification_success_actorIsRecipientThemself_TC014() {
        UUID blogId = UUID.randomUUID();
        User recipient = user(recipientId());
        when(userValidator.validateUserExists(recipientId())).thenReturn(recipient);
        when(notificationRepository.countByRecipientUserIdAndIsRead(recipientId(), false)).thenReturn(2L);
        doAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(UUID.randomUUID());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUpdatedAt(notification.getCreatedAt());
            return notification;
        }).when(notificationRepository).save(any(Notification.class));

        var result = notificationService.createModerationNotification(
                recipientId(), blogId, "REJECTED", "Noi dung vi pham");

        assertThat(result.getType()).isEqualTo(NotificationType.BLOG_MODERATION);
        assertThat(result.getBlogId()).isEqualTo(blogId);
        assertThat(result.getModerationStatus()).isEqualTo("REJECTED");
        assertThat(result.getModerationReason()).isEqualTo("Noi dung vi pham");
        assertThat(result.getRecipientId()).isEqualTo(recipientId());
        assertThat(result.getActorId()).isEqualTo(recipientId());
        assertThat(result.getNavigation().getAction()).isEqualTo("open_moderation_result");
        verify(notificationRealtimeService).emitNewNotification(recipientId(), result);
        verify(notificationRealtimeService).emitUnreadCountUpdated(recipientId(), 2L);
    }

    @Test
    void createModerationNotification_fail_missingRequiredFields_TC015() {
        UUID blogId = UUID.randomUUID();
        UUID recipientId = recipientId();

        assertThatThrownBy(() ->
                notificationService.createModerationNotification(null, blogId, "REJECTED", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("recipientId, blogId and moderationStatus are required");
        assertThatThrownBy(() ->
                notificationService.createModerationNotification(recipientId, null, "REJECTED", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("recipientId, blogId and moderationStatus are required");
        assertThatThrownBy(() ->
                notificationService.createModerationNotification(recipientId, blogId, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("recipientId, blogId and moderationStatus are required");
    }

    @Test
    void createNotification_fail_actorIdMissing_TC016() {
        CreateNotificationRequestDTO request = baseRequest(NotificationType.LIKE);
        request.setActorId(null);
        request.setBlogId(UUID.randomUUID());

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Actor id and recipient id are required");
    }

    @Test
    void createNotification_fail_blogNotificationCarriesCafePageId_TC017() {
        CreateNotificationRequestDTO request = baseRequest(NotificationType.LIKE);
        request.setBlogId(UUID.randomUUID());
        request.setCafePageId(UUID.randomUUID());

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mismatched target data");
    }

    @Test
    void createNotification_fail_messageNotificationCarriesCafePageId_TC018() {
        CreateNotificationRequestDTO request = baseRequest(NotificationType.MESSAGE);
        request.setConversationId(UUID.randomUUID());
        request.setCafePageId(UUID.randomUUID());

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mismatched target data");
    }

    @Test
    void createNotification_fail_followCafePageWithoutCafePageId_TC019() {
        CreateNotificationRequestDTO request = baseRequest(NotificationType.FOLLOW);
        request.setTargetType(FollowTargetType.CAFE_PAGE);

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cafePageId is required");
    }

    @Test
    void createNotification_fail_followMixesUserAndCafePageTargets_TC020() {
        CreateNotificationRequestDTO userTarget = baseRequest(NotificationType.FOLLOW);
        userTarget.setTargetType(FollowTargetType.USER);
        userTarget.setUserId(UUID.randomUUID());
        userTarget.setCafePageId(UUID.randomUUID());

        CreateNotificationRequestDTO cafePageTarget = baseRequest(NotificationType.FOLLOW);
        cafePageTarget.setTargetType(FollowTargetType.CAFE_PAGE);
        cafePageTarget.setCafePageId(UUID.randomUUID());
        cafePageTarget.setUserId(UUID.randomUUID());

        CreateNotificationRequestDTO withConversation = baseRequest(NotificationType.FOLLOW);
        withConversation.setTargetType(FollowTargetType.USER);
        withConversation.setUserId(UUID.randomUUID());
        withConversation.setConversationId(UUID.randomUUID());

        assertThatThrownBy(() -> notificationService.createNotification(userTarget))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mismatched target data");
        assertThatThrownBy(() -> notificationService.createNotification(cafePageTarget))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mismatched target data");
        assertThatThrownBy(() -> notificationService.createNotification(withConversation))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mismatched target data");
    }

    private void mockCreate(NotificationType type) {
        when(userValidator.validateUserExists(recipientId())).thenReturn(user(recipientId()));
        User actor = user(actorId());
        when(actorContextResolver.resolve(eq(actorId()), any(), any()))
                .thenReturn(new ActorContext(actor, ActorContextType.USER, null));
        when(notificationRepository.countByRecipientUserIdAndIsRead(recipientId(), false)).thenReturn(1L);
        doAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(UUID.randomUUID());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUpdatedAt(notification.getCreatedAt());
            assertThat(notification.getType()).isEqualTo(type);
            return notification;
        }).when(notificationRepository).save(any(Notification.class));
    }

    private CreateNotificationRequestDTO baseRequest(NotificationType type) {
        CreateNotificationRequestDTO request = new CreateNotificationRequestDTO();
        request.setRecipientId(recipientId());
        request.setActorId(actorId());
        request.setType(type);
        return request;
    }

    private Notification notification(NotificationType type, Boolean isRead) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setRecipient(user(recipientId()));
        notification.setActor(user(actorId()));
        notification.setType(type);
        notification.setIsRead(isRead);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(notification.getCreatedAt());
        if (type == NotificationType.MESSAGE) {
            notification.setConversationId(UUID.randomUUID());
            notification.setMessageId(UUID.randomUUID());
        } else if (type == NotificationType.FOLLOW) {
            notification.setTargetUserId(UUID.randomUUID());
        } else {
            notification.setBlogId(UUID.randomUUID());
        }
        return notification;
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

    private UUID recipientId() {
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }

    private UUID actorId() {
        return UUID.fromString("22222222-2222-2222-2222-222222222222");
    }
}
