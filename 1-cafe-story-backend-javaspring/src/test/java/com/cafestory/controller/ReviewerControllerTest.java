package com.cafestory.controller;

import com.cafestory.dto.responseDTO.reviewer.ReviewerResponseDTO;
import com.cafestory.service.serviceInterface.ReviewerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        ReviewerResponseDTO result = reviewerController.createReviewer(userId);

        assertThat(result).isEqualTo(response);
        verify(reviewerService).createReviewer(userId);
    }
}
