package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.dto.responseDTO.FeedItemResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FeedAdService {

    List<FeedItemResponseDTO> insertAdsIntoFeed(UUID userId, List<BlogFeedResponse> organicPosts);
}
