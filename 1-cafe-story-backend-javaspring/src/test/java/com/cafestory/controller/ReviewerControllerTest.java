package com.cafestory.controller;

import com.cafestory.dto.responseDTO.reviewer.ReviewerResponseDTO;
import com.cafestory.service.serviceInterface.ReviewerService;
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
class ReviewerControllerTest {

    @Mock
    private ReviewerService reviewerService;

    @InjectMocks
    private ReviewerController reviewerController;

    @Test
    void createReviewer_success_TC001() {
        UUID userId = UUID.randomUUID();
        ReviewerResponseDTO response = new ReviewerResponseDTO();
        response.setUserId(userId);
        response.setRole("REVIEWER");

        when(reviewerService.createReviewer(userId)).thenReturn(response);

        ReviewerResponseDTO result = reviewerController.createReviewer(principal(userId));

        assertThat(result).isEqualTo(response);
        verify(reviewerService).createReviewer(userId);
    }

    @Test
    void getReviewer_success_TC002() {
        UUID userId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        ReviewerResponseDTO response = new ReviewerResponseDTO();
        response.setReviewerId(reviewerId);
        response.setUserId(userId);
        response.setName("Reviewer");

        when(reviewerService.getReviewer(userId)).thenReturn(response);

        ReviewerResponseDTO result = reviewerController.getReviewer(userId);

        assertThat(result).isEqualTo(response);
        verify(reviewerService).getReviewer(userId);
    }

    @Test
    void getAllReviewer_success_TC003() {
        ReviewerResponseDTO response = new ReviewerResponseDTO();
        response.setReviewerId(UUID.randomUUID());
        when(reviewerService.getAllReviewer()).thenReturn(List.of(response));

        List<ReviewerResponseDTO> result = reviewerController.getAllReviewer();

        assertThat(result).containsExactly(response);
        verify(reviewerService).getAllReviewer();
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
