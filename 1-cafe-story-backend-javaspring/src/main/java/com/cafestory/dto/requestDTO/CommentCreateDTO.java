package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ActorContextType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CommentCreateDTO {

    @NotNull(message = "Blog id is mandatory")
    private UUID blogId;

    private UUID userId;

    private UUID parentCommentId;

    private ActorContextType actorContextType;

    private UUID actorCafePageId;

    @NotBlank(message = "Content is mandatory")
    private String content;

    private List<String> imageUrls;
}
