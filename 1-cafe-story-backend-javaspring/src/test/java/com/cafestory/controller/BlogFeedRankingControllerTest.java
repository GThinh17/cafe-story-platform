package com.cafestory.controller;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
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
                principal(userId),
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
                principal(userId),
                windowType,
                regionId);

        assertThat(result).isEqualTo(response);
        verify(blogFeedRankingService).rebuildRecommendationCache(userId, windowType, regionId);
    }

    private BlogFeedResponse feedResponse(UUID userId, UUID regionId) {
        BlogFeedResponse response = new BlogFeedResponse();
        response.setBlogId(UUID.randomUUID());
        response.setContentPreview("Personalized cafe blog preview");
        response.setImageUrls(List.of("/images/feed/cafe.jpg"));
        response.setLikeCount(12);
        response.setCommentCount(3);
        response.setShareCount(4);
        response.setAuthorUserId(userId);
        response.setAuthorUserName("author");
        response.setAuthorUserFullName("Author Name");
        response.setAuthorAvatar("/images/default-avatar.svg");
        response.setPageId(UUID.randomUUID());
        response.setPageName("Cafe Story Roastery");
        response.setPageAddress("123 Brew Street");
        response.setPageAvatarUrl("/images/cafe-avatar.jpg");
        response.setPageCoverUrl("/images/cafe-cover.jpg");
        response.setRegionId(regionId);
        response.setRegionCity("Ho Chi Minh");
        response.setRegionProvince("Ho Chi Minh");
        response.setRegionArea("District 1");
        response.setRankPosition(1);
        response.setCreatedAt(LocalDateTime.now().minusHours(4));
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
