package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.FeedItemType;
import lombok.Data;

@Data
public class FeedItemResponseDTO {
    private FeedItemType itemType;
    private BlogFeedResponse blog;
    private AdCampaignResponseDTO ad;
    private Integer position;
    private String trackingToken;
}
