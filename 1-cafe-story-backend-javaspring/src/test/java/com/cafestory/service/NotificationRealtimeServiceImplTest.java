package com.cafestory.service;

import com.cafestory.dto.responseDTO.NotificationResponseDTO;
import com.cafestory.dto.responseDTO.SocketEventResponseDTO;
import com.cafestory.dto.responseDTO.UnreadCountResponseDTO;
import com.cafestory.service.serviceImplement.NotificationRealtimeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Kiểm thử {@link NotificationRealtimeServiceImpl}.
 *
 * <p>Quy tắc quan trọng: khi đang trong transaction, thông báo chỉ được đẩy sau
 * khi commit — nếu không client sẽ nhận thông báo của bản ghi bị rollback. Bài
 * kiểm thử mở/đóng {@link TransactionSynchronizationManager} thủ công để dựng
 * đúng hai bối cảnh này.
 */
@ExtendWith(MockitoExtension.class)
class NotificationRealtimeServiceImplTest {

    private static final String QUEUE = "/queue/notifications";

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private NotificationRealtimeServiceImpl realtimeService;

    @BeforeEach
    void setUp() {
        realtimeService = new NotificationRealtimeServiceImpl(messagingTemplate);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void emitNewNotification_success_sendsImmediatelyOutsideTransaction_TC001() {
        UUID recipientId = UUID.randomUUID();
        NotificationResponseDTO notification = new NotificationResponseDTO();

        realtimeService.emitNewNotification(recipientId, notification);

        ArgumentCaptor<SocketEventResponseDTO> captor = ArgumentCaptor.forClass(SocketEventResponseDTO.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq(recipientId.toString()), eq(QUEUE), captor.capture());
        assertThat(captor.getValue().getEvent()).isEqualTo("notification:new");
        assertThat(captor.getValue().getUserId()).isEqualTo(recipientId);
        assertThat(captor.getValue().getPayload()).isSameAs(notification);
    }

    @Test
    void emitUnreadCountUpdated_success_wrapsCountInPayload_TC002() {
        UUID recipientId = UUID.randomUUID();

        realtimeService.emitUnreadCountUpdated(recipientId, 4L);

        ArgumentCaptor<SocketEventResponseDTO> captor = ArgumentCaptor.forClass(SocketEventResponseDTO.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq(recipientId.toString()), eq(QUEUE), captor.capture());
        assertThat(captor.getValue().getEvent()).isEqualTo("notification:unread_count_updated");
        assertThat(((UnreadCountResponseDTO) captor.getValue().getPayload()).getUnreadCount()).isEqualTo(4L);
    }

    @Test
    void emitNewNotification_success_nullRecipientIsIgnored_TC003() {
        realtimeService.emitNewNotification(null, new NotificationResponseDTO());
        realtimeService.emitUnreadCountUpdated(null, 1L);

        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any(Object.class));
    }

    @Test
    void emitNewNotification_success_deferredUntilTransactionCommit_TC004() {
        UUID recipientId = UUID.randomUUID();
        TransactionSynchronizationManager.initSynchronization();

        realtimeService.emitNewNotification(recipientId, new NotificationResponseDTO());

        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any(Object.class));
        assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);

        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());

        verify(messagingTemplate).convertAndSendToUser(
                eq(recipientId.toString()), eq(QUEUE), any(SocketEventResponseDTO.class));
    }
}
