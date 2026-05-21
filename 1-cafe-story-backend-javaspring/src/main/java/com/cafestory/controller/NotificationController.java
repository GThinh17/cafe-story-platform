package com.cafestory.controller;

import com.cafestory.dto.responseDTO.notification.NotificationResponseDTO;
import com.cafestory.dto.responseDTO.notification.UnreadCountResponseDTO;
import com.cafestory.entity.enums.NotificationType;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponseDTO> getUserNotifications(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) NotificationType type) {
        return notificationService.getUserNotifications(requireUserId(principal), page, limit, isRead, type);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponseDTO getUnreadCount(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return new UnreadCountResponseDTO(notificationService.getUnreadCount(requireUserId(principal)));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponseDTO markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return notificationService.markAsRead(requireUserId(principal), id);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        notificationService.markAllAsRead(requireUserId(principal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        notificationService.deleteNotification(requireUserId(principal), id);
    }
}
