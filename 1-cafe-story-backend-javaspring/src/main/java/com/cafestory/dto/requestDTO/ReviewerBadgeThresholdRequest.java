package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ReviewerBadge;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewerBadgeThresholdRequest {

    @NotNull(message = "badge is required")
    private ReviewerBadge badge;

    @NotNull(message = "minScore is required")
    @Min(value = 0, message = "minScore must be >= 0")
    private Long minScore;
}
