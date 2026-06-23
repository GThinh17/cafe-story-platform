package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ActorContextType;
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
    private String authorUserAvatar;
    private ActorContextType actorContextType;
    private UUID actorCafePageId;
    private String actorDisplayName;
    private String actorAvatarUrl;
    private UUID parentCommentId;
    private String content;
    private List<String> imageUrls;
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
