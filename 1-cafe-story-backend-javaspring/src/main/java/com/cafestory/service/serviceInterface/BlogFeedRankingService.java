package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.entity.enums.TrendWindowType;

import java.util.List;
import java.util.UUID;

public interface BlogFeedRankingService {
    List<BlogFeedResponse> getPersonalizedFeed(
            UUID userId,
            TrendWindowType windowType,
            UUID regionId,
            int page,
            int size);

    List<BlogFeedResponse> rebuildRecommendationCache(
            UUID userId,
            TrendWindowType windowType,
            UUID regionId);

    void rebuildRecommendationCacheForAllActiveUsers();
}
