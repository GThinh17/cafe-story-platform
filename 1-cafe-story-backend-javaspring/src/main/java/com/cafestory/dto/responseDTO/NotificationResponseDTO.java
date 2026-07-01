package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponseDTO {

    private UUID id;

    private UUID recipientId;

    private UUID actorId;

    private ActorContextType actorType;

    private UUID actorCafePageId;

    private String actorDisplayName;

    private String actorAvatarUrl;

    private NotificationType type;

    private UUID blogId;

    private UUID conversationId;

    private UUID userId;

    private FollowTargetType targetType;

    private UUID targetId;

    private UUID cafePageId;

    private UUID commentId;

    private UUID messageId;

    private String moderationStatus;

    private String moderationReason;

    private Boolean isRead;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private NavigationTargetResponseDTO navigation;
}
