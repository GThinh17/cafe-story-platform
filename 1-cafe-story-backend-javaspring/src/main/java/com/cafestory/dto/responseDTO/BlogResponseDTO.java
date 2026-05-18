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
    private UUID pageId;
    private UUID regionId;
    private String content;
    private List<String> imageUrls;
    private PostStatus status;
    private Boolean isPinned;
    private Boolean allowComment;
    private Integer likeCount;
    private Integer shareCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
