package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayoutFormulaRequest {

    @NotNull
    @Min(0)
    private Long likePayoutAmount;

    @NotNull
    @Min(0)
    private Long commentPayoutAmount;

    @NotNull
    @Min(0)
    private Long sharePayoutAmount;

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal ironMultiplier;

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal bronzeMultiplier;

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal silverMultiplier;

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal goldMultiplier;

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal diamondMultiplier;

    private String description;
}
