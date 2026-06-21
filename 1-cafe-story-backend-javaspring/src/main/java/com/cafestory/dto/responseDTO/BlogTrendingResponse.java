package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.TrendWindowType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BlogTrendingResponse {
    private UUID blogId;
    private String contentPreview;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private UUID authorUserId;
    private String authorUserName;
    private String authorUserFullName;
    private String authorUserAvatar;
    private UUID pageId;
    private String pageName;
    private String pageAvatarUrl;
    private String pageCoverUrl;
    private BlogDisplayAuthorType displayAuthorType;
    private String displayName;
    private String displayAvatarUrl;
    private Boolean isLike;
    private Boolean isSave;
    private TrendWindowType windowType;
    private Double trendScore;
    private Integer rankPosition;
    private String reason;
    private Boolean pinned;
    private LocalDateTime createdAt;
    private LocalDateTime computedAt;
}
