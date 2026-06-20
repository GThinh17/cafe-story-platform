package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewerFormulaRequestDTO {

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

    @NotNull(message = "ironMultiplier is required")
    @DecimalMin(value = "1.0", message = "ironMultiplier must be >= 1.0")
    private BigDecimal ironMultiplier;

    @NotNull(message = "bronzeMultiplier is required")
    @DecimalMin(value = "1.0", message = "bronzeMultiplier must be >= 1.0")
    private BigDecimal bronzeMultiplier;

    @NotNull(message = "silverMultiplier is required")
    @DecimalMin(value = "1.0", message = "silverMultiplier must be >= 1.0")
    private BigDecimal silverMultiplier;

    @NotNull(message = "goldMultiplier is required")
    @DecimalMin(value = "1.0", message = "goldMultiplier must be >= 1.0")
    private BigDecimal goldMultiplier;

    @NotNull(message = "diamondMultiplier is required")
    @DecimalMin(value = "1.0", message = "diamondMultiplier must be >= 1.0")
    private BigDecimal diamondMultiplier;

    private String description;
}
