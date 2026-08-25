package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminPayoutStatus;
import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class AdminPayoutResponseDTO {

    private UUID id;
    private UUID reviewerId;
    private String reviewerUserName;
    private String reviewerUserAvatar;
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
    private String stripeTransferId;
    /**
     * Các trạng thái hợp lệ tiếp theo, suy từ chính luật của server. Trả ra để
     * admin UI khỏi chép lại luồng trạng thái và trôi lệch theo thời gian.
     */
    private List<AdminPayoutStatus> allowedTransitions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
