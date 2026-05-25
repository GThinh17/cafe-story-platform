package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ModerationResolveAction;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminModerationResultResponseDTO {

    private UUID id;

    private UUID blogId;

    private UUID commentId;

    private Double score;

    private ModerationDecision decision;

    private String labels;

    private String explanation;

    private String modelName;

    private Boolean resolved;

    private ModerationResolveAction resolvedAction;

    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;
}
