package com.cafestory.dto.responseDTO.payout;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PayoutFormulaResponse {

    private UUID id;
    private long likePayoutAmount;
    private long commentPayoutAmount;
    private long sharePayoutAmount;
    private BigDecimal ironMultiplier;
    private BigDecimal bronzeMultiplier;
    private BigDecimal silverMultiplier;
    private BigDecimal goldMultiplier;
    private BigDecimal diamondMultiplier;
    private boolean active;
    private String description;
    private LocalDateTime createdAt;
}
