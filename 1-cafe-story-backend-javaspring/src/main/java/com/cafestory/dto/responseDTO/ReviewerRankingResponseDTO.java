package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerRankingResponseDTO {

    private int rank;

    private UUID reviewerId;

    private long score;

    private long likeCount;

    private long shareCount;

    private long commentCount;

    private String location;
}
