package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerRankingSnapshotResponseDTO {

    private UUID id;
    private UUID reviewerId;
    private String reviewerUserName;
    private String reviewerUserAvatar;
    private String period;
    private RankingPeriodType periodType;
    private int rankPosition;
    private long score;
    private long likeCount;
    private long shareCount;
    private long commentCount;
    private ReviewerBadge badge;
    private UUID formulaId;
}
