package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.chat.SocketEventResponseDTO;
import com.cafestory.dto.responseDTO.notification.NotificationResponseDTO;
import com.cafestory.service.serviceInterface.NotificationRealtimeService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationRealtimeServiceImpl implements NotificationRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationRealtimeServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void emitNewNotification(UUID recipientId, NotificationResponseDTO notification) {
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/notifications",
                new SocketEventResponseDTO("notification:new", null, recipientId, notification));
    }

    @Override
    public void emitUnreadCountUpdated(UUID recipientId, long unreadCount) {
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/notifications",
                new SocketEventResponseDTO("notification:unread_count_updated", null, recipientId, unreadCount));
    }
}
