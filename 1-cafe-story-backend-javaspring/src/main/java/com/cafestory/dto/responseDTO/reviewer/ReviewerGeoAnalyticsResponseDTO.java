package com.cafestory.dto.responseDTO.reviewer;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerGeoAnalyticsResponseDTO {

    private String locationName;

    private String groupBy;

    private long reviewerCount;

    private long totalLikes;

    private long totalShares;

    private long totalComments;

    private long totalScore;

    private double averageScore;

    private UUID topReviewer;
}
