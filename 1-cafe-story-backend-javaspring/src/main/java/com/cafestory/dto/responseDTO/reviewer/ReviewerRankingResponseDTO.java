package com.cafestory.dto.responseDTO.reviewer;

import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerRankingResponseDTO {

    private int rank;

    private UUID reviewerId;

    private long score;

    private long likeCount;

    private long shareCount;

    private long commentCount;

    private ReviewerBadge badge;

    private String location;
}
