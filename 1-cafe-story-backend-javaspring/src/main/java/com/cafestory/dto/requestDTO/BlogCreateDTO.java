package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BlogCreateDTO {

    private UUID authorUserId;

    private UUID pageId;

    private UUID regionId;

    @NotBlank(message = "Content is mandatory")
    private String content;

    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageUrls;

    private List<UUID> taggedUserIds;

    private Boolean isPinned;

    private Boolean allowComment;
}
