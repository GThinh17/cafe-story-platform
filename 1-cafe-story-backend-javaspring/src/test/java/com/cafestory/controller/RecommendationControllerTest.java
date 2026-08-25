package com.cafestory.controller;

import com.cafestory.dto.responseDTO.RecommendationCardResponseDTO;
import com.cafestory.service.serviceInterface.RecommendationService;
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
class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    @Test
    void getUserRecommendations_success_usesPrincipalUserId_TC001() {
        UUID userId = UUID.randomUUID();
        List<RecommendationCardResponseDTO> response = List.of(new RecommendationCardResponseDTO());

        when(recommendationService.getUserRecommendations(userId, 0, 20)).thenReturn(response);

        List<RecommendationCardResponseDTO> result =
                recommendationController.getUserRecommendations(principal(userId), 0, 20);

        assertThat(result).isEqualTo(response);
        verify(recommendationService).getUserRecommendations(userId, 0, 20);
    }

    @Test
    void getReviewerRecommendations_success_usesPrincipalUserId_TC002() {
        UUID userId = UUID.randomUUID();
        List<RecommendationCardResponseDTO> response = List.of(new RecommendationCardResponseDTO());

        when(recommendationService.getReviewerRecommendations(userId, 0, 20)).thenReturn(response);

        List<RecommendationCardResponseDTO> result =
                recommendationController.getReviewerRecommendations(principal(userId), 0, 20);

        assertThat(result).isEqualTo(response);
        verify(recommendationService).getReviewerRecommendations(userId, 0, 20);
    }

    @Test
    void getCafePageRecommendations_success_usesPrincipalUserId_TC003() {
        UUID userId = UUID.randomUUID();
        List<RecommendationCardResponseDTO> response = List.of(new RecommendationCardResponseDTO());

        when(recommendationService.getCafePageRecommendations(userId, 0, 20)).thenReturn(response);

        List<RecommendationCardResponseDTO> result =
                recommendationController.getCafePageRecommendations(principal(userId), 0, 20);

        assertThat(result).isEqualTo(response);
        verify(recommendationService).getCafePageRecommendations(userId, 0, 20);
    }

    @Test
    void getMixedRecommendations_success_usesPrincipalUserId_TC004() {
        UUID userId = UUID.randomUUID();
        List<RecommendationCardResponseDTO> response = List.of(new RecommendationCardResponseDTO());

        when(recommendationService.getMixedRecommendations(userId, 0, 30)).thenReturn(response);

        List<RecommendationCardResponseDTO> result =
                recommendationController.getMixedRecommendations(principal(userId), 0, 30);

        assertThat(result).isEqualTo(response);
        verify(recommendationService).getMixedRecommendations(userId, 0, 30);
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
