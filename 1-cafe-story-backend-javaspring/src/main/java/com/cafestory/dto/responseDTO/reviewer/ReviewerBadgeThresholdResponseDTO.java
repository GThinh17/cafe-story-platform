package com.cafestory.dto.responseDTO.reviewer;

import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerBadgeThresholdResponseDTO {

    private UUID id;
    private ReviewerBadge badge;
    private long minScore;
    private UUID formulaId;
}
