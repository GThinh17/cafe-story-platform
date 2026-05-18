package com.cafestory.controller;

import com.cafestory.dto.responseDTO.notification.NotificationResponseDTO;
import com.cafestory.dto.responseDTO.notification.UnreadCountResponseDTO;
import com.cafestory.entity.enums.NotificationType;
import com.cafestory.service.serviceInterface.NotificationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponseDTO> getUserNotifications(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) NotificationType type) {
        return notificationService.getUserNotifications(userId, page, limit, isRead, type);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponseDTO getUnreadCount(@RequestParam UUID userId) {
        return new UnreadCountResponseDTO(notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponseDTO markAsRead(@PathVariable UUID id, @RequestParam UUID userId) {
        return notificationService.markAsRead(userId, id);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@RequestParam UUID userId) {
        notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable UUID id, @RequestParam UUID userId) {
        notificationService.deleteNotification(userId, id);
    }
}
