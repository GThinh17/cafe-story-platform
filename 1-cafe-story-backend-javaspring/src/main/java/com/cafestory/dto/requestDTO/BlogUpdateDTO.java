package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PostStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BlogUpdateDTO {
    private UUID pageId;
    private UUID regionId;
    private String content;
    private List<String> imageUrls;
    private List<UUID> taggedUserIds;
    private PostStatus status;
    private Boolean isPinned;
    private Boolean allowComment;
}
