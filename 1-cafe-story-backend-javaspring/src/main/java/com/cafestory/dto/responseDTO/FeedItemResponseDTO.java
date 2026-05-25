package com.cafestory.dto.responseDTO;

import lombok.Data;

@Data
public class FeedItemResponseDTO {
    private String itemType;
    private BlogFeedResponse blog;
    private AdCampaignResponseDTO ad;
}
