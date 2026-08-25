package com.cafestory.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagBlogModerationItemResponseDTO {

    private String blogId;
    private String blogStatus;
    private String decision;
    private String captionReason;
    private String imageReason;
    private String captionSnippet;
    private Boolean resolved;
    private String resolvedAction;
    private LocalDateTime createdAt;
}
