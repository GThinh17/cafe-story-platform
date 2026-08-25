package com.cafestory.controller;

import com.cafestory.dto.responseDTO.NotificationResponseDTO;
import com.cafestory.dto.responseDTO.UnreadCountResponseDTO;
import com.cafestory.entity.enums.NotificationType;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link NotificationController} — người nhận luôn lấy từ token.
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private AuthenticatedUserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedUserPrincipal(UUID.randomUUID(), "an", List.of("USER"));
    }

    @Test
    void getUserNotifications_success_passesFiltersThrough_TC001() {
        List<NotificationResponseDTO> notifications = List.of(new NotificationResponseDTO());
        when(notificationService.getUserNotifications(
                principal.userId(), 1, 30, Boolean.FALSE, NotificationType.LIKE))
                .thenReturn(notifications);

        assertThat(notificationController.getUserNotifications(principal, 1, 30, false, NotificationType.LIKE))
                .isSameAs(notifications);
    }

    @Test
    void getUnreadCount_success_wrapsCount_TC002() {
        when(notificationService.getUnreadCount(principal.userId())).thenReturn(7L);

        UnreadCountResponseDTO result = notificationController.getUnreadCount(principal);

        assertThat(result.getUnreadCount()).isEqualTo(7L);
    }

    @Test
    void markAsRead_success_delegates_TC003() {
        UUID notificationId = UUID.randomUUID();
        NotificationResponseDTO response = new NotificationResponseDTO();
        when(notificationService.markAsRead(principal.userId(), notificationId)).thenReturn(response);

        assertThat(notificationController.markAsRead(notificationId, principal)).isSameAs(response);
    }

    @Test
    void markAllAsRead_success_delegates_TC004() {
        notificationController.markAllAsRead(principal);

        verify(notificationService).markAllAsRead(principal.userId());
    }

    @Test
    void deleteNotification_success_delegates_TC005() {
        UUID notificationId = UUID.randomUUID();

        notificationController.deleteNotification(notificationId, principal);

        verify(notificationService).deleteNotification(principal.userId(), notificationId);
    }

    @Test
    void getUnreadCount_fail_missingPrincipal_TC006() {
        assertThatThrownBy(() -> notificationController.getUnreadCount(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }
}
