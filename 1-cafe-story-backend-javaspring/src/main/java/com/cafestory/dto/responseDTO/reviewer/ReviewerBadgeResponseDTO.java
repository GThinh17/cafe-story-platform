package com.cafestory.dto.responseDTO.reviewer;

import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerBadgeResponseDTO {

    private UUID id;

    private UUID reviewerId;

    private String month;

    private long score;

    private ReviewerBadge badge;

    private long likeCount;

    private long shareCount;

    private long commentCount;
}
