package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogRankingOverrideResponse {
    private UUID id;
    private UUID blogId;
    private Double boostScore;
    private Boolean isPinned;
    private String reason;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
