package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PageLikeResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID cafePageId;
    private LocalDateTime createdAt;
}
