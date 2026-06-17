package com.cafestory.dto.responseDTO.reviewer;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewerScoringFormulaResponseDTO {

    private UUID id;
    private int likeWeight;
    private int commentWeight;
    private int shareWeight;
    private long likePayoutAmount;
    private long commentPayoutAmount;
    private long sharePayoutAmount;
    private boolean active;
    private String description;
    private LocalDateTime createdAt;
}
