package com.cafestory.dto.responseDTO.reviewer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerStatsResponseDTO {

    private UUID reviewerId;

    private String period;

    private long likeCount;

    private long shareCount;

    private long commentCount;

    private long score;
}
