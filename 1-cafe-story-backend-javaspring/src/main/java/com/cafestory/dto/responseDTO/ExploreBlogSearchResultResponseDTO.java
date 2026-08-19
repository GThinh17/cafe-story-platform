package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExploreBlogSearchResultResponseDTO {
    private UUID id;
    private String content;
    private UUID authorUserId;
    private String authorUserName;
    private String authorUserFullName;
    private String authorUserAvatar;
    private UUID pageId;
    private String pageName;
    private String pageAvatarUrl;
    private String displayName;
    private String displayAvatarUrl;
    private LocalDateTime createdAt;
}
