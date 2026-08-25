package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class AdCampaignResponseDTO {
    private UUID adCampaignId;
    private UUID cafePageId;
    private UUID paymentId;
    private String title;
    private String description;
    private String imageUrl;
    private String targetUrl;
    private AdStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer priority;
    private Integer maxImpressions;
    private Integer servedImpressions;
    private Integer maxDurationDays;
    private List<AdTargetRegionResponseDTO> targetRegions = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
