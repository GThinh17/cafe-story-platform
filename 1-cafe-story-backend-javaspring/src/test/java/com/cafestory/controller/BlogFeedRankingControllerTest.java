package com.cafestory.controller;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogFeedRankingControllerTest {

    @Mock
    private BlogFeedRankingService blogFeedRankingService;

    @InjectMocks
    private BlogFeedRankingController blogFeedRankingController;

    @Test
    void getPersonalizedFeed_success_TC001() {
        UUID userId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        TrendWindowType windowType = TrendWindowType.DAY_7;
        int page = 1;
        int size = 20;
        List<BlogFeedResponse> response = List.of(feedResponse(userId, regionId));

        when(blogFeedRankingService.getPersonalizedFeed(userId, windowType, regionId, page, size))
                .thenReturn(response);

        List<BlogFeedResponse> result = blogFeedRankingController.getPersonalizedFeed(
                userId,
                windowType,
                regionId,
                page,
                size);

        assertThat(result).isEqualTo(response);
        verify(blogFeedRankingService).getPersonalizedFeed(userId, windowType, regionId, page, size);
    }

    @Test
    void rebuildPersonalizedFeed_success_TC002() {
        UUID userId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        TrendWindowType windowType = TrendWindowType.HOUR_24;
        List<BlogFeedResponse> response = List.of(feedResponse(userId, regionId));

        when(blogFeedRankingService.rebuildRecommendationCache(userId, windowType, regionId)).thenReturn(response);

        List<BlogFeedResponse> result = blogFeedRankingController.rebuildPersonalizedFeed(
                userId,
                windowType,
                regionId);

        assertThat(result).isEqualTo(response);
        verify(blogFeedRankingService).rebuildRecommendationCache(userId, windowType, regionId);
    }

    private BlogFeedResponse feedResponse(UUID userId, UUID regionId) {
        BlogFeedResponse response = new BlogFeedResponse();
        response.setBlogId(UUID.randomUUID());
        response.setContentPreview("Personalized cafe blog preview");
        response.setAuthorUserId(userId);
        response.setAuthorUserName("author");
        response.setPageId(UUID.randomUUID());
        response.setRegionId(regionId);
        response.setWindowType(TrendWindowType.DAY_7);
        response.setFeedScore(76.0);
        response.setTrendingScore(40.0);
        response.setFollowedPageScore(30.0);
        response.setFollowedUserScore(25.0);
        response.setSameRegionScore(15.0);
        response.setFreshnessScore(6.0);
        response.setReportPenalty(0.0);
        response.setRankPosition(1);
        response.setReason("Followed page, followed author, same region");
        response.setCreatedAt(LocalDateTime.now().minusHours(4));
        response.setComputedAt(LocalDateTime.now());
        return response;
    }
}
