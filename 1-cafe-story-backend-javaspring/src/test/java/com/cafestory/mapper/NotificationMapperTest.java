package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.NotificationResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Notification;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.NotificationTargetType;
import com.cafestory.entity.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử {@link NotificationMapper}.
 *
 * <p>Phần quan trọng là điều hướng: mỗi loại thông báo phải trỏ tới đúng màn
 * hình và đúng khoá — nhầm khoá thì người dùng bấm vào thông báo lại rơi vào
 * trang trống.
 */
class NotificationMapperTest {

    private final NotificationMapper mapper = new NotificationMapper();

    @ParameterizedTest
    @EnumSource(value = NotificationType.class, names = {"LIKE", "SHARE", "COMMENT", "TAG"})
    void toNotificationResponseDTO_success_blogTypesNavigateToBlog_TC001(NotificationType type) {
        Notification notification = notification(type);

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.BLOG);
        assertThat(result.getNavigation().getTargetId()).isEqualTo(notification.getBlogId());
        assertThat(result.getNavigation().getAction()).isEqualTo("open_blog");
    }

    @Test
    void toNotificationResponseDTO_success_messageNavigatesToConversation_TC002() {
        Notification notification = notification(NotificationType.MESSAGE);

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.CONVERSATION);
        assertThat(result.getNavigation().getTargetId()).isEqualTo(notification.getConversationId());
        assertThat(result.getNavigation().getAction()).isEqualTo("open_conversation");
    }

    @Test
    void toNotificationResponseDTO_success_moderationNavigatesToModerationResult_TC003() {
        Notification notification = notification(NotificationType.BLOG_MODERATION);
        notification.setModerationStatus("REJECTED");
        notification.setModerationReason("Noi dung vi pham");

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getNavigation().getAction()).isEqualTo("open_moderation_result");
        assertThat(result.getModerationStatus()).isEqualTo("REJECTED");
        assertThat(result.getModerationReason()).isEqualTo("Noi dung vi pham");
    }

    @Test
    void toNotificationResponseDTO_success_followCafePageNavigatesToPage_TC004() {
        Notification notification = notification(NotificationType.FOLLOW);
        notification.setTargetType(FollowTargetType.CAFE_PAGE);

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.CAFE_PAGE);
        assertThat(result.getNavigation().getTargetId()).isEqualTo(notification.getTargetCafePageId());
        assertThat(result.getTargetId()).isEqualTo(notification.getTargetCafePageId());
        assertThat(result.getCafePageId()).isEqualTo(notification.getTargetCafePageId());
    }

    @Test
    void toNotificationResponseDTO_success_followUserNavigatesToProfile_TC005() {
        Notification notification = notification(NotificationType.FOLLOW);
        notification.setTargetType(FollowTargetType.USER);

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.USER);
        assertThat(result.getNavigation().getTargetId()).isEqualTo(notification.getTargetUserId());
        assertThat(result.getTargetId()).isEqualTo(notification.getTargetUserId());
    }

    @Test
    void toNotificationResponseDTO_success_followWithoutTargetTypeFallsBackToUser_TC006() {
        Notification notification = notification(NotificationType.FOLLOW);
        notification.setTargetType(null);

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getNavigation().getTargetType()).isEqualTo(NotificationTargetType.USER);
        assertThat(result.getTargetId()).isEqualTo(notification.getTargetUserId());
    }

    @Test
    void toNotificationResponseDTO_success_cafePageActorUsesPageIdentity_TC007() {
        Notification notification = notification(NotificationType.LIKE);
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setName("Cafe Story Ninh Kieu");
        cafePage.setAvatarUrl("https://cdn.example.com/page.png");
        notification.setActorContextType(ActorContextType.CAFE_PAGE);
        notification.setActorCafePage(cafePage);

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getActorCafePageId()).isEqualTo(cafePage.getId());
        assertThat(result.getActorDisplayName()).isEqualTo("Cafe Story Ninh Kieu");
        assertThat(result.getActorAvatarUrl()).isEqualTo("https://cdn.example.com/page.png");
    }

    @Test
    void toNotificationResponseDTO_success_userActorFallsBackToUserName_TC008() {
        Notification notification = notification(NotificationType.LIKE);
        notification.getActor().setUserFullName("   ");

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getActorCafePageId()).isNull();
        assertThat(result.getActorDisplayName()).isEqualTo("an");
        assertThat(result.getActorAvatarUrl()).isEqualTo("https://cdn.example.com/an.png");
    }

    @Test
    void toNotificationResponseDTO_success_copiesEveryIdentifier_TC009() {
        Notification notification = notification(NotificationType.COMMENT);

        NotificationResponseDTO result = mapper.toNotificationResponseDTO(notification);

        assertThat(result.getId()).isEqualTo(notification.getId());
        assertThat(result.getRecipientId()).isEqualTo(notification.getRecipient().getUserId());
        assertThat(result.getActorId()).isEqualTo(notification.getActor().getUserId());
        assertThat(result.getBlogId()).isEqualTo(notification.getBlogId());
        assertThat(result.getCommentId()).isEqualTo(notification.getCommentId());
        assertThat(result.getMessageId()).isEqualTo(notification.getMessageId());
        assertThat(result.getConversationId()).isEqualTo(notification.getConversationId());
        assertThat(result.getIsRead()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(notification.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(notification.getUpdatedAt());
    }

    private Notification notification(NotificationType type) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setRecipient(user("binh", "Tran Thi Binh"));
        notification.setActor(user("an", "Nguyen Van An"));
        notification.setActorContextType(ActorContextType.USER);
        notification.setType(type);
        notification.setBlogId(UUID.randomUUID());
        notification.setConversationId(UUID.randomUUID());
        notification.setTargetUserId(UUID.randomUUID());
        notification.setTargetCafePageId(UUID.randomUUID());
        notification.setCommentId(UUID.randomUUID());
        notification.setMessageId(UUID.randomUUID());
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        notification.setUpdatedAt(LocalDateTime.now());
        return notification;
    }

    private User user(String userName, String fullName) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(userName);
        user.setUserFullName(fullName);
        user.setUserAvatar("https://cdn.example.com/" + userName + ".png");
        return user;
    }
}
