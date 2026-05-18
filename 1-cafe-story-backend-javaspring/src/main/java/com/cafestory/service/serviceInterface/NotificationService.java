package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.notification.CreateNotificationRequest;
import com.cafestory.dto.responseDTO.notification.NotificationResponseDTO;
import com.cafestory.entity.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponseDTO createNotification(CreateNotificationRequest request);

    NotificationResponseDTO createLikeNotification(UUID recipientId, UUID actorId, UUID blogId);

    NotificationResponseDTO createShareNotification(UUID recipientId, UUID actorId, UUID blogId);

    NotificationResponseDTO createCommentNotification(UUID recipientId, UUID actorId, UUID blogId, UUID commentId);

    NotificationResponseDTO createMessageNotification(UUID recipientId, UUID actorId, UUID conversationId, UUID messageId);

    NotificationResponseDTO createFollowNotification(UUID recipientId, UUID actorId, UUID userId);

    List<NotificationResponseDTO> getUserNotifications(UUID userId, int page, int limit, Boolean isRead, NotificationType type);

    long getUnreadCount(UUID userId);

    NotificationResponseDTO markAsRead(UUID userId, UUID notificationId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID userId, UUID notificationId);
}
