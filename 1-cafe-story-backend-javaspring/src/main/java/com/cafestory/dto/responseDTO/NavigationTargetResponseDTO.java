package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.NotificationTargetType;
import lombok.Data;

import java.util.UUID;

@Data
public class NavigationTargetResponseDTO {

    private NotificationTargetType targetType;

    private UUID targetId;

    private String action;
}
