package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateNotificationRequestDTO {

    @NotNull(message = "Recipient id is mandatory")
    private UUID recipientId;

    @NotNull(message = "Actor id is mandatory")
    private UUID actorId;

    private ActorContextType actorContextType;

    private UUID actorCafePageId;

    @NotNull(message = "Notification type is mandatory")
    private NotificationType type;

    private UUID blogId;

    private UUID conversationId;

    private UUID userId;

    private FollowTargetType targetType;

    private UUID cafePageId;

    private UUID commentId;

    private UUID messageId;
}
