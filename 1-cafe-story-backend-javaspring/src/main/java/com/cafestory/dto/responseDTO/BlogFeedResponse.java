package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BlogFeedResponse {
    private UUID blogId;
    private String contentPreview;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private UUID authorUserId;
    private String authorUserName;
    private String authorUserFullName;
    private String authorAvatar;
    private String authorUserAvatar;
    private UUID pageId;
    private String pageName;
    private String pageAddress;
    private String pageAvatarUrl;
    private String pageCoverUrl;
    private BlogDisplayAuthorType displayAuthorType;
    private String displayName;
    private String displayAvatarUrl;
    private UUID regionId;
    private String regionCity;
    private String regionProvince;
    private String regionArea;
    private Integer rankPosition;
    private Boolean isAuthorFollowing;
    private Boolean isPageFollowing;
    private Boolean isLike;
    private Boolean isSave;
    private LocalDateTime createdAt;
}
