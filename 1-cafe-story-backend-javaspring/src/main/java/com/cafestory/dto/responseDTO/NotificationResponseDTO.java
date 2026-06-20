package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponseDTO {

    private UUID id;

    private UUID recipientId;

    private UUID actorId;

    private NotificationType type;

    private UUID blogId;

    private UUID conversationId;

    private UUID userId;

    private UUID commentId;

    private UUID messageId;

    private Boolean isRead;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private NavigationTargetResponseDTO navigation;
}
