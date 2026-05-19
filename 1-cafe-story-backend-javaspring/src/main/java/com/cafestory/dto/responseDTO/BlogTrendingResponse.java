package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.TrendWindowType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogTrendingResponse {
    private UUID blogId;
    private String contentPreview;
    private UUID authorUserId;
    private String authorUserName;
    private UUID pageId;
    private String pageName;
    private TrendWindowType windowType;
    private Double trendScore;
    private Integer rankPosition;
    private String reason;
    private Boolean pinned;
    private LocalDateTime createdAt;
    private LocalDateTime computedAt;
}
