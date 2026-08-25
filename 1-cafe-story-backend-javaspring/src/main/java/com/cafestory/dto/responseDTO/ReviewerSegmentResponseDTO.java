package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerSegmentResponseDTO {

    private UUID reviewerId;

    private String segment;

    private long score;

    private long likeCount;

    private long shareCount;

    private long commentCount;
}
