package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.notification.NavigationTargetResponseDTO;
import com.cafestory.dto.responseDTO.notification.NotificationResponseDTO;
import com.cafestory.entity.Notification;
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
        response.setType(notification.getType());
        response.setBlogId(notification.getBlogId());
        response.setConversationId(notification.getConversationId());
        response.setUserId(notification.getTargetUserId());
        response.setCommentId(notification.getCommentId());
        response.setMessageId(notification.getMessageId());
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
        return navigation(NotificationTargetType.USER, notification.getTargetUserId(), "open_user_profile");
    }

    private NavigationTargetResponseDTO navigation(NotificationTargetType targetType, UUID targetId, String action) {
        NavigationTargetResponseDTO response = new NavigationTargetResponseDTO();
        response.setTargetType(targetType);
        response.setTargetId(targetId);
        response.setAction(action);
        return response;
    }
}
