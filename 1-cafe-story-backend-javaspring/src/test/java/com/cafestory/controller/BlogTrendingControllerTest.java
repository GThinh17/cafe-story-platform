package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogEventRequest;
import com.cafestory.dto.responseDTO.BlogEventResponse;
import com.cafestory.dto.responseDTO.BlogTrendingResponse;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.service.serviceInterface.BlogEventService;
import com.cafestory.service.serviceInterface.BlogTrendingService;
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
class BlogTrendingControllerTest {

    @Mock
    private BlogTrendingService blogTrendingService;

    @Mock
    private BlogEventService blogEventService;

    @InjectMocks
    private BlogTrendingController blogTrendingController;

    @Test
    void getTrendingBlogs_success_TC001() {
        TrendWindowType windowType = TrendWindowType.HOUR_24;
        int page = 0;
        int size = 10;
        UUID userId = UUID.randomUUID();
        List<BlogTrendingResponse> response = List.of(trendingResponse());

        when(blogTrendingService.getTrendingBlogs(userId, windowType, page, size)).thenReturn(response);

        List<BlogTrendingResponse> result = blogTrendingController.getTrendingBlogs(principal(userId), windowType, page, size);

        assertThat(result).isEqualTo(response);
        verify(blogTrendingService).getTrendingBlogs(userId, windowType, page, size);
    }

    @Test
    void recordBlogEvent_success_TC002() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BlogEventRequest request = eventRequest();
        BlogEventResponse response = eventResponse(blogId, userId);

        when(blogEventService.recordEvent(
                blogId,
                userId,
                request.getEventType(),
                request.getWeight())).thenReturn(response);

        BlogEventResponse result = blogTrendingController.recordBlogEvent(blogId, request, principal(userId));

        assertThat(result).isEqualTo(response);
        verify(blogEventService).recordEvent(
                blogId,
                userId,
                request.getEventType(),
                request.getWeight());
    }

    private BlogTrendingResponse trendingResponse() {
        BlogTrendingResponse response = new BlogTrendingResponse();
        response.setBlogId(UUID.randomUUID());
        response.setContentPreview("Cafe blog trending preview");
        response.setAuthorUserId(UUID.randomUUID());
        response.setAuthorUserName("author");
        response.setPageId(UUID.randomUUID());
        response.setPageName("Cafe Story");
        response.setWindowType(TrendWindowType.HOUR_24);
        response.setTrendScore(88.5);
        response.setRankPosition(1);
        response.setReason("High engagement");
        response.setPinned(false);
        response.setCreatedAt(LocalDateTime.now().minusHours(2));
        response.setComputedAt(LocalDateTime.now());
        return response;
    }

    private BlogEventRequest eventRequest() {
        BlogEventRequest request = new BlogEventRequest();
        request.setEventType(BlogEventType.VIEW);
        request.setWeight(1.0);
        return request;
    }

    private BlogEventResponse eventResponse(UUID blogId, UUID userId) {
        BlogEventResponse response = new BlogEventResponse();
        response.setId(UUID.randomUUID());
        response.setBlogId(blogId);
        response.setUserId(userId);
        response.setEventType(BlogEventType.VIEW);
        response.setWeight(1.0);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
