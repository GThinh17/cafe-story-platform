package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.PostStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BlogResponseDTO {
    private UUID id;
    private UUID authorUserId;
    private String authorUserName;
    private String authorUserFullName;
    private String authorUserAvatar;
    private UUID pageId;
    private String pageName;
    private String pageAvatarUrl;
    private UUID regionId;
    private String content;
    private List<String> imageUrls;
    private PostStatus status;
    private Boolean isPinned;
    private Boolean allowComment;
    private Integer likeCount;
    private Integer shareCount;
    private Integer commentCount;
    private BlogDisplayAuthorType displayAuthorType;
    private String displayName;
    private String displayAvatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
