package com.cafestory.controller;

import com.cafestory.dto.requestDTO.FeedImpressionBatchRequestDTO;
import com.cafestory.dto.requestDTO.FeedImpressionItemRequestDTO;
import com.cafestory.dto.responseDTO.FeedImpressionResponseDTO;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.service.serviceInterface.FeedImpressionService;
import com.cafestory.service.serviceInterface.FeedService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedImpressionControllerTest {

    @Mock
    private BlogFeedRankingService blogFeedRankingService;

    @Mock
    private FeedService feedService;

    @Mock
    private FeedImpressionService feedImpressionService;

    @Test
    void recordImpressions_success_usesPrincipalUserId_TC001() {
        UUID userId = UUID.randomUUID();
        FeedImpressionBatchRequestDTO request = request(UUID.randomUUID(), 1);
        FeedImpressionResponseDTO response = new FeedImpressionResponseDTO();
        response.setRecordedCount(1);
        FeedController controller = controller();

        when(feedImpressionService.recordImpressions(userId, request)).thenReturn(response);

        FeedImpressionResponseDTO result = controller.recordImpressions(principal(userId), request);

        assertThat(result).isEqualTo(response);
        verify(feedImpressionService).recordImpressions(userId, request);
    }

    @Test
    void recordImpressions_fail_missingPrincipal_TC002() {
        FeedController controller = controller();

        assertThatThrownBy(() -> controller.recordImpressions(null, request(UUID.randomUUID(), 1)))
                .isInstanceOf(ResponseStatusException.class);
    }

    private FeedController controller() {
        return new FeedController(blogFeedRankingService, feedService, feedImpressionService);
    }

    private FeedImpressionBatchRequestDTO request(UUID blogId, int position) {
        FeedImpressionItemRequestDTO item = new FeedImpressionItemRequestDTO();
        item.setBlogId(blogId);
        item.setPosition(position);
        FeedImpressionBatchRequestDTO request = new FeedImpressionBatchRequestDTO();
        request.setItems(List.of(item));
        return request;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "reader", List.of("USER"));
    }
}
