package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.NavigationTargetResponseDTO;
import com.cafestory.dto.responseDTO.NotificationResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Notification;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.NotificationTargetType;
import com.cafestory.entity.enums.NotificationType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationMapper {

    public NotificationResponseDTO toNotificationResponseDTO(Notification notification) {
        NotificationResponseDTO response = new NotificationResponseDTO();
        response.setId(notification.getId());
        response.setRecipientId(notification.getRecipient().getUserId());
        response.setActorId(notification.getActor().getUserId());
        response.setActorType(notification.getActorContextType());
        response.setActorCafePageId(notification.getActorCafePage() == null ? null : notification.getActorCafePage().getId());
        response.setActorDisplayName(resolveActorDisplayName(notification.getActorContextType(), notification.getActorCafePage(), notification.getActor()));
        response.setActorAvatarUrl(resolveActorAvatarUrl(notification.getActorContextType(), notification.getActorCafePage(), notification.getActor()));
        response.setType(notification.getType());
        response.setBlogId(notification.getBlogId());
        response.setConversationId(notification.getConversationId());
        response.setUserId(notification.getTargetUserId());
        response.setTargetType(notification.getTargetType());
        response.setCafePageId(notification.getTargetCafePageId());
        response.setTargetId(resolveTargetId(notification));
        response.setCommentId(notification.getCommentId());
        response.setMessageId(notification.getMessageId());
        response.setModerationStatus(notification.getModerationStatus());
        response.setModerationReason(notification.getModerationReason());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setUpdatedAt(notification.getUpdatedAt());
        response.setNavigation(toNavigationTarget(notification));
        return response;
    }

    private NavigationTargetResponseDTO toNavigationTarget(Notification notification) {
        NotificationType type = notification.getType();
        if (type == NotificationType.LIKE
                || type == NotificationType.SHARE
                || type == NotificationType.COMMENT
                || type == NotificationType.TAG) {
            return navigation(NotificationTargetType.BLOG, notification.getBlogId(), "open_blog");
        }
        if (type == NotificationType.MESSAGE) {
            return navigation(NotificationTargetType.CONVERSATION, notification.getConversationId(), "open_conversation");
        }
        if (type == NotificationType.BLOG_MODERATION) {
            return navigation(NotificationTargetType.BLOG, notification.getBlogId(), "open_moderation_result");
        }
        if (notification.getTargetType() == FollowTargetType.CAFE_PAGE) {
            return navigation(NotificationTargetType.CAFE_PAGE, notification.getTargetCafePageId(), "open_cafe_page");
        }
        return navigation(NotificationTargetType.USER, notification.getTargetUserId(), "open_user_profile");
    }

    private NavigationTargetResponseDTO navigation(NotificationTargetType targetType, UUID targetId, String action) {
        NavigationTargetResponseDTO response = new NavigationTargetResponseDTO();
        response.setTargetType(targetType);
        response.setTargetId(targetId);
        response.setAction(action);
        return response;
    }

    private UUID resolveTargetId(Notification notification) {
        if (notification.getTargetType() == FollowTargetType.CAFE_PAGE) {
            return notification.getTargetCafePageId();
        }
        if (notification.getTargetType() == FollowTargetType.USER) {
            return notification.getTargetUserId();
        }
        return notification.getTargetUserId();
    }

    private String resolveActorDisplayName(ActorContextType actorContextType, CafePage actorCafePage, User actor) {
        if (actorContextType == ActorContextType.CAFE_PAGE && actorCafePage != null) {
            return actorCafePage.getName();
        }
        if (actor == null) {
            return null;
        }
        if (actor.getUserFullName() != null && !actor.getUserFullName().isBlank()) {
            return actor.getUserFullName();
        }
        return actor.getUserName();
    }

    private String resolveActorAvatarUrl(ActorContextType actorContextType, CafePage actorCafePage, User actor) {
        if (actorContextType == ActorContextType.CAFE_PAGE && actorCafePage != null) {
            return actorCafePage.getAvatarUrl();
        }
        return actor == null ? null : actor.getUserAvatar();
    }
}
