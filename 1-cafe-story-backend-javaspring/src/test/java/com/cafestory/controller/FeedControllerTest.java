package com.cafestory.controller;

import com.cafestory.dto.requestDTO.FeedImpressionBatchRequestDTO;
import com.cafestory.dto.requestDTO.FeedImpressionItemRequestDTO;
import com.cafestory.dto.responseDTO.FeedImpressionResponseDTO;
import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.service.serviceInterface.FeedImpressionService;
import com.cafestory.service.serviceInterface.FeedService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    @Mock
    private BlogFeedRankingService blogFeedRankingService;

    @Mock
    private FeedService feedService;

    @Mock
    private FeedImpressionService feedImpressionService;

    @InjectMocks
    private FeedController feedController;

    @Test
    void getFeed_success_TC001() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, "reader", List.of("USER"));
        FeedResponseDTO response = new FeedResponseDTO();
        response.setHasMore(true);

        when(feedService.getFeed(userId, "cursor-token", 20)).thenReturn(response);

        FeedResponseDTO result = feedController.getFeed(principal, "cursor-token", 20);

        assertThat(result).isEqualTo(response);
        verify(feedService).getFeed(userId, "cursor-token", 20);
    }

    @Test
    void getOrganicFeed_success_TC002() {
        FeedResponseDTO response = new FeedResponseDTO();
        response.setHasMore(false);

        when(blogFeedRankingService.getOrganicFeed("cursor-token", 10)).thenReturn(response);

        FeedResponseDTO result = feedController.getOrganicFeed("cursor-token", 10);

        assertThat(result).isEqualTo(response);
        verify(blogFeedRankingService).getOrganicFeed("cursor-token", 10);
    }

    @Test
    void recordImpressions_success_usesPrincipalUserId_TC003() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, "reader", List.of("USER"));
        FeedImpressionBatchRequestDTO request = new FeedImpressionBatchRequestDTO();
        FeedImpressionItemRequestDTO item = new FeedImpressionItemRequestDTO();
        item.setBlogId(UUID.randomUUID());
        item.setPosition(1);
        request.setItems(List.of(item));
        FeedImpressionResponseDTO response = new FeedImpressionResponseDTO();
        response.setRecordedCount(1);

        when(feedImpressionService.recordImpressions(userId, request)).thenReturn(response);

        FeedImpressionResponseDTO result = feedController.recordImpressions(principal, request);

        assertThat(result).isEqualTo(response);
        verify(feedImpressionService).recordImpressions(userId, request);
    }
}
