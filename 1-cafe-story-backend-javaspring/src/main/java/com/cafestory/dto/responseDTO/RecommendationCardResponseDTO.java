package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.RecommendationTargetType;
import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.util.UUID;

@Data
public class RecommendationCardResponseDTO {
    private UUID targetId;
    private RecommendationTargetType targetType;
    private UUID userId;
    private String avatar;
    private String username;
    private String fullName;
    private String reason;
    private String city;

    /** Chỉ có với target REVIEWER; null với USER và CAFE_PAGE. */
    private ReviewerBadge badge;

    /** Người đang gọi có theo dõi target này không. */
    private boolean isFollowing;
}
