package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogRankingOverrideRequest;
import com.cafestory.dto.responseDTO.BlogRankingOverrideResponse;
import com.cafestory.service.serviceInterface.BlogRankingService;
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
class AdminBlogRankingControllerTest {

    @Mock
    private BlogRankingService blogRankingService;

    @InjectMocks
    private AdminBlogRankingController adminBlogRankingController;

    @Test
    void createRankingOverride_success_TC001() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BlogRankingOverrideRequest request = request();
        BlogRankingOverrideResponse response = response(blogId, request);

        when(blogRankingService.createOverride(blogId, request)).thenReturn(response);

        BlogRankingOverrideResponse result =
                adminBlogRankingController.createRankingOverride(blogId, request, principal(userId));

        assertThat(result).isEqualTo(response);
        assertThat(request.getCreatedBy()).isEqualTo(userId);
        verify(blogRankingService).createOverride(blogId, request);
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("ADMIN"));
    }

    private BlogRankingOverrideRequest request() {
        BlogRankingOverrideRequest request = new BlogRankingOverrideRequest();
        request.setBoostScore(50.0);
        request.setIsPinned(true);
        request.setReason("Manual boost for campaign");
        request.setStartAt(LocalDateTime.now());
        request.setEndAt(LocalDateTime.now().plusDays(3));
        request.setCreatedBy(UUID.randomUUID());
        return request;
    }

    private BlogRankingOverrideResponse response(UUID blogId, BlogRankingOverrideRequest request) {
        BlogRankingOverrideResponse response = new BlogRankingOverrideResponse();
        response.setId(UUID.randomUUID());
        response.setBlogId(blogId);
        response.setBoostScore(request.getBoostScore());
        response.setIsPinned(request.getIsPinned());
        response.setReason(request.getReason());
        response.setStartAt(request.getStartAt());
        response.setEndAt(request.getEndAt());
        response.setCreatedBy(request.getCreatedBy());
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
