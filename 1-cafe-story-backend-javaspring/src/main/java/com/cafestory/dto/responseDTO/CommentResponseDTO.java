package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.PostStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CommentResponseDTO {
    private UUID id;
    private UUID blogId;
    private UUID userId;
    private String authorUserName;
    private UUID parentCommentId;
    private String content;
    private List<String> imageUrls;
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
