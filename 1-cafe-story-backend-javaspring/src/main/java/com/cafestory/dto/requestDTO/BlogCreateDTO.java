package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BlogCreateDTO {

    @NotNull(message = "Author user id is mandatory")
    private UUID authorUserId;

    private UUID pageId;

    private UUID regionId;

    @NotBlank(message = "Content is mandatory")
    private String content;

    private List<String> imageUrls;

    private Boolean isPinned;

    private Boolean allowComment;
}
