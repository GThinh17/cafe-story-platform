package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogRatingResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID blogId;
    private Integer rating;
    private Double ratingAverage;
    private Long ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
