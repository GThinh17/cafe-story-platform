package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewerIncomeResponseDTO {

    private UUID id;
    private UUID reviewerId;
    private String reviewerUserName;
    private String reviewerUserAvatar;
    private LocalDate incomeDate;
    private long likeCount;
    private long commentCount;
    private long shareCount;
    private ReviewerBadge badge;
    private BigDecimal badgeMultiplier;
    private long baseAmount;
    private long finalAmount;
    private UUID formulaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
