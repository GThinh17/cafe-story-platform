package com.cafestory.dto.responseDTO.reviewer;

import com.cafestory.entity.enums.PayoutStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerPayoutResponseDTO {

    private UUID id;

    private UUID reviewerId;

    private String payoutMonth;

    private long likeCount;

    private long shareCount;

    private long commentCount;

    private long likeAmount;

    private long shareAmount;

    private long commentAmount;

    private long totalAmount;

    private PayoutStatus payoutStatus;
}
