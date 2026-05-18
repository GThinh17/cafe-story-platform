package com.cafestory.dto.requestDTO.notification;

import com.cafestory.entity.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateNotificationRequest {

    @NotNull(message = "Recipient id is mandatory")
    private UUID recipientId;

    @NotNull(message = "Actor id is mandatory")
    private UUID actorId;

    @NotNull(message = "Notification type is mandatory")
    private NotificationType type;

    private UUID blogId;

    private UUID conversationId;

    private UUID userId;

    private UUID commentId;

    private UUID messageId;
}
