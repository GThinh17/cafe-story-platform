package com.cafestory.controller;

import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    @Mock
    private BlogFeedRankingService blogFeedRankingService;

    @InjectMocks
    private FeedController feedController;

    @Test
    void getOrganicFeed_success_TC001() {
        FeedResponseDTO response = new FeedResponseDTO();
        response.setHasMore(false);

        when(blogFeedRankingService.getOrganicFeed("cursor-token", 10)).thenReturn(response);

        FeedResponseDTO result = feedController.getOrganicFeed("cursor-token", 10);

        assertThat(result).isEqualTo(response);
        verify(blogFeedRankingService).getOrganicFeed("cursor-token", 10);
    }
}
