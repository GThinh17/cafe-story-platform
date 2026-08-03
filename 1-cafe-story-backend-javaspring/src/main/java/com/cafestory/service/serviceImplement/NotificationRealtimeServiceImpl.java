package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.SocketEventResponseDTO;
import com.cafestory.dto.responseDTO.NotificationResponseDTO;
import com.cafestory.dto.responseDTO.UnreadCountResponseDTO;
import com.cafestory.service.serviceInterface.NotificationRealtimeService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
public class NotificationRealtimeServiceImpl implements NotificationRealtimeService {

    private static final String NOTIFICATION_QUEUE = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationRealtimeServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void emitNewNotification(UUID recipientId, NotificationResponseDTO notification) {
        sendToRecipient(
                recipientId,
                new SocketEventResponseDTO("notification:new", null, recipientId, notification));
    }

    @Override
    public void emitUnreadCountUpdated(UUID recipientId, long unreadCount) {
        sendToRecipient(
                recipientId,
                new SocketEventResponseDTO(
                        "notification:unread_count_updated",
                        null,
                        recipientId,
                        new UnreadCountResponseDTO(unreadCount)));
    }

    /**
     * Push sau khi transaction commit để client không nhận notification của row bị rollback,
     * và không refetch trước khi dữ liệu hiện diện. Ngoài transaction thì gửi ngay.
     */
    private void sendToRecipient(UUID recipientId, SocketEventResponseDTO event) {
        if (recipientId == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            messagingTemplate.convertAndSendToUser(recipientId.toString(), NOTIFICATION_QUEUE, event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSendToUser(recipientId.toString(), NOTIFICATION_QUEUE, event);
            }
        });
    }
}
