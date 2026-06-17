package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewerScoringFormulaRequest {

    @NotNull(message = "likeWeight is required")
    @Min(value = 0, message = "likeWeight must be >= 0")
    private Integer likeWeight;

    @NotNull(message = "commentWeight is required")
    @Min(value = 0, message = "commentWeight must be >= 0")
    private Integer commentWeight;

    @NotNull(message = "shareWeight is required")
    @Min(value = 0, message = "shareWeight must be >= 0")
    private Integer shareWeight;

    @NotNull(message = "likePayoutAmount is required")
    @Min(value = 0, message = "likePayoutAmount must be >= 0")
    private Long likePayoutAmount;

    @NotNull(message = "commentPayoutAmount is required")
    @Min(value = 0, message = "commentPayoutAmount must be >= 0")
    private Long commentPayoutAmount;

    @NotNull(message = "sharePayoutAmount is required")
    @Min(value = 0, message = "sharePayoutAmount must be >= 0")
    private Long sharePayoutAmount;

    private String description;
}
