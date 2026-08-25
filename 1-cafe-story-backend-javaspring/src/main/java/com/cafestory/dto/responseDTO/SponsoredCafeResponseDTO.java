package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class SponsoredCafeResponseDTO {
    private UUID campaignId;
    private UUID cafePageId;
    private String cafeName;
    private String cafeAvatarUrl;
    private String cafeCoverUrl;
    private String imageUrl;
    private String headline;
    private String description;
    private String ctaLabel;
    private String targetUrl;
    private String trackingToken;
}
