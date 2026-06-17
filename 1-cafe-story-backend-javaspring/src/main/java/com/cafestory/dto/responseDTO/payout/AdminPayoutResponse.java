package com.cafestory.dto.responseDTO.payout;

import com.cafestory.entity.enums.AdminPayoutStatus;
import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminPayoutResponse {

    private UUID id;
    private UUID reviewerId;
    private String payoutMonth;
    private long totalBaseAmount;
    private ReviewerBadge badge;
    private BigDecimal badgeMultiplier;
    private long totalFinalAmount;
    private AdminPayoutStatus status;
    private UUID approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
    private String note;
    private UUID formulaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
