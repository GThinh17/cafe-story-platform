package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CreateNotificationRequestDTO;
import com.cafestory.dto.responseDTO.NotificationResponseDTO;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponseDTO createNotification(CreateNotificationRequestDTO request);

    NotificationResponseDTO createLikeNotification(UUID recipientId, UUID actorId, UUID blogId);

    NotificationResponseDTO createLikeNotification(UUID recipientId, UUID actorId, UUID blogId, ActorContextType actorContextType, UUID actorCafePageId);

    NotificationResponseDTO createShareNotification(UUID recipientId, UUID actorId, UUID blogId);

    NotificationResponseDTO createShareNotification(UUID recipientId, UUID actorId, UUID blogId, ActorContextType actorContextType, UUID actorCafePageId);

    NotificationResponseDTO createCommentNotification(UUID recipientId, UUID actorId, UUID blogId, UUID commentId);

    NotificationResponseDTO createCommentNotification(UUID recipientId, UUID actorId, UUID blogId, UUID commentId, ActorContextType actorContextType, UUID actorCafePageId);

    NotificationResponseDTO createMessageNotification(UUID recipientId, UUID actorId, UUID conversationId, UUID messageId);

    NotificationResponseDTO createFollowNotification(UUID recipientId, UUID actorId, UUID userId);

    NotificationResponseDTO createFollowPageNotification(UUID recipientId, UUID actorId, UUID cafePageId);

    NotificationResponseDTO createTagNotification(UUID recipientId, UUID actorId, UUID blogId);

    NotificationResponseDTO createModerationNotification(UUID recipientId, UUID blogId, String moderationStatus, String moderationReason);

    List<NotificationResponseDTO> getUserNotifications(UUID userId, int page, int limit, Boolean isRead, NotificationType type);

    long getUnreadCount(UUID userId);

    NotificationResponseDTO markAsRead(UUID userId, UUID notificationId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID userId, UUID notificationId);
}
