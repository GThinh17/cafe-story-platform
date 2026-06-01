package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogSaveResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID blogId;
    private Boolean saved;
    private Long saveCount;
    private LocalDateTime createdAt;
}
