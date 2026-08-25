package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.NotificationResponseDTO;

import java.util.UUID;

public interface NotificationRealtimeService {

    void emitNewNotification(UUID recipientId, NotificationResponseDTO notification);

    void emitUnreadCountUpdated(UUID recipientId, long unreadCount);
}
