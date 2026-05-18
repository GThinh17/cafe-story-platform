package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogLikeResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID blogId;
    private LocalDateTime createdAt;
}
