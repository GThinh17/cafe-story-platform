package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.TrendWindowType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogFeedResponse {
    private UUID blogId;
    private String contentPreview;
    private UUID authorUserId;
    private String authorUserName;
    private UUID pageId;
    private UUID regionId;
    private TrendWindowType windowType;
    private Double feedScore;
    private Double trendingScore;
    private Double followedPageScore;
    private Double followedUserScore;
    private Double sameRegionScore;
    private Double freshnessScore;
    private Double reportPenalty;
    private Integer rankPosition;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime computedAt;
}
