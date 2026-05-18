package com.cafestory.dto.responseDTO.reviewer;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerResponseDTO {
    private UUID reviewerId;
    private UUID userId;
    private String role;
}
