package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerDiscoveryResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerEarningsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerIncomeResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerRankingResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerSegmentResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStatsResponseDTO;
import com.cafestory.service.serviceInterface.ReviewerService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
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
        UUID viewerUserId = UUID.randomUUID();
        ReviewerResponseDTO response = new ReviewerResponseDTO();
        response.setReviewerId(UUID.randomUUID());
        when(reviewerService.getAllReviewer(viewerUserId)).thenReturn(List.of(response));

        List<ReviewerResponseDTO> result = reviewerController.getAllReviewer(null, null, principal(viewerUserId));

        assertThat(result).containsExactly(response);
        verify(reviewerService).getAllReviewer(viewerUserId);
    }

    @Test
    void getReviewersInRegion_success_areaFallsBackToDistrict_TC004() {
        UUID viewerUserId = UUID.randomUUID();
        List<ReviewerDiscoveryResponseDTO> reviewers = List.of(new ReviewerDiscoveryResponseDTO());
        when(reviewerService.getReviewersInRegion(
                viewerUserId, "Can Tho", "Can Tho", "An Khanh", "Ninh Kieu", "30/4", 0, 20))
                .thenReturn(reviewers);

        assertThat(reviewerController.getReviewersInRegion(
                "Can Tho", "Can Tho", "An Khanh", "  ", "Ninh Kieu", "30/4", 0, 20, principal(viewerUserId)))
                .isSameAs(reviewers);
    }

    @Test
    void getReviewersInRegion_success_anonymousViewerAndExplicitArea_TC005() {
        List<ReviewerDiscoveryResponseDTO> reviewers = List.of(new ReviewerDiscoveryResponseDTO());
        when(reviewerService.getReviewersInRegion(null, null, null, null, "Ninh Kieu", null, 1, 10))
                .thenReturn(reviewers);

        assertThat(reviewerController.getReviewersInRegion(
                null, null, null, "Ninh Kieu", "Quan khac", null, 1, 10, null))
                .isSameAs(reviewers);
    }

    @Test
    void getTrendingReviewers_success_delegates_TC006() {
        UUID viewerUserId = UUID.randomUUID();
        List<ReviewerDiscoveryResponseDTO> reviewers = List.of(new ReviewerDiscoveryResponseDTO());
        when(reviewerService.getTrendingReviewers(viewerUserId, "DAY_30", 0, 20)).thenReturn(reviewers);

        assertThat(reviewerController.getTrendingReviewers("DAY_30", 0, 20, principal(viewerUserId)))
                .isSameAs(reviewers);
    }

    @Test
    void getTopReviewers_success_delegates_TC007() {
        List<ReviewerDiscoveryResponseDTO> reviewers = List.of(new ReviewerDiscoveryResponseDTO());
        when(reviewerService.getTopReviewers(null, 0, 20)).thenReturn(reviewers);

        assertThat(reviewerController.getTopReviewers(0, 20, null)).isSameAs(reviewers);
    }

    @Test
    void getAllReviewer_success_queryTakesPrecedenceOverActiveOnly_TC008() {
        UUID viewerUserId = UUID.randomUUID();
        List<ReviewerResponseDTO> reviewers = List.of(new ReviewerResponseDTO());
        when(reviewerService.searchReviewers("an", viewerUserId)).thenReturn(reviewers);

        assertThat(reviewerController.getAllReviewer("an", true, principal(viewerUserId)))
                .isSameAs(reviewers);
        verify(reviewerService, never()).getAllActiveReviewers(viewerUserId);
    }

    @Test
    void getAllReviewer_success_activeOnlyFilter_TC009() {
        UUID viewerUserId = UUID.randomUUID();
        List<ReviewerResponseDTO> reviewers = List.of(new ReviewerResponseDTO());
        when(reviewerService.getAllActiveReviewers(viewerUserId)).thenReturn(reviewers);

        assertThat(reviewerController.getAllReviewer("   ", true, principal(viewerUserId)))
                .isSameAs(reviewers);
    }

    @Test
    void getReviewerStats_success_delegates_TC010() {
        UUID viewerUserId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        ReviewerStatsResponseDTO stats = new ReviewerStatsResponseDTO(reviewerId, "day", 1, 2, 3, 4);
        when(reviewerService.countReviewerStats(viewerUserId, reviewerId, "day")).thenReturn(stats);

        assertThat(reviewerController.getReviewerStats(reviewerId, "day", principal(viewerUserId)))
                .isSameAs(stats);
    }

    @Test
    void getReviewerStats_fail_missingPrincipal_TC011() {
        UUID reviewerId = UUID.randomUUID();

        assertThatThrownBy(() -> reviewerController.getReviewerStats(reviewerId, "day", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }

    @Test
    void getReviewerRanking_success_areaFallsBackToDistrict_TC012() {
        List<ReviewerRankingResponseDTO> ranking = List.of(new ReviewerRankingResponseDTO());
        when(reviewerService.getReviewerRanking("month", 1, 20, "Can Tho", null, "Ninh Kieu"))
                .thenReturn(ranking);

        assertThat(reviewerController.getReviewerRanking("month", 1, 20, "Can Tho", null, null, "Ninh Kieu"))
                .isSameAs(ranking);
    }

    @Test
    void getReviewersBySegment_success_delegates_TC013() {
        List<ReviewerSegmentResponseDTO> segments = List.of(new ReviewerSegmentResponseDTO());
        when(reviewerService.getReviewersBySegment("2026-07", "top")).thenReturn(segments);

        assertThat(reviewerController.getReviewersBySegment("2026-07", "top")).isSameAs(segments);
    }

    @Test
    void getGeoAnalytics_success_delegates_TC014() {
        List<ReviewerGeoAnalyticsResponseDTO> analytics = List.of(new ReviewerGeoAnalyticsResponseDTO());
        when(reviewerService.getGeoAnalytics("month", "city")).thenReturn(analytics);

        assertThat(reviewerController.getGeoAnalytics("month", "city")).isSameAs(analytics);
    }

    @Test
    void getReviewerEarnings_success_delegates_TC015() {
        UUID viewerUserId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        List<ReviewerEarningsResponseDTO> earnings = List.of(new ReviewerEarningsResponseDTO());
        when(reviewerService.getReviewerEarnings(viewerUserId, reviewerId)).thenReturn(earnings);

        assertThat(reviewerController.getReviewerEarnings(reviewerId, principal(viewerUserId)))
                .isSameAs(earnings);
    }

    @Test
    void getReviewerIncome_success_sortsByIncomeDateDescending_TC016() {
        UUID viewerUserId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        Page<ReviewerIncomeResponseDTO> page = new PageImpl<>(List.of());
        when(reviewerService.getReviewerIncome(
                org.mockito.ArgumentMatchers.eq(viewerUserId),
                org.mockito.ArgumentMatchers.eq(reviewerId),
                org.mockito.ArgumentMatchers.eq("2026-07"),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        assertThat(reviewerController.getReviewerIncome(reviewerId, "2026-07", 2, 31, principal(viewerUserId)))
                .isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewerService).getReviewerIncome(
                org.mockito.ArgumentMatchers.eq(viewerUserId),
                org.mockito.ArgumentMatchers.eq(reviewerId),
                org.mockito.ArgumentMatchers.eq("2026-07"),
                captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(31);
        assertThat(captor.getValue().getSort().getOrderFor("incomeDate").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void generateMonthlyBadges_success_delegates_TC017() {
        UUID adminUserId = UUID.randomUUID();
        List<ReviewerBadgeResponseDTO> badges = List.of(new ReviewerBadgeResponseDTO());
        when(reviewerService.generateMonthlyBadges(adminUserId, "2026-07", true)).thenReturn(badges);

        assertThat(reviewerController.generateMonthlyBadges("2026-07", true, principal(adminUserId)))
                .isSameAs(badges);
    }

    @Test
    void getReviewerBadges_success_delegates_TC018() {
        UUID viewerUserId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        List<ReviewerBadgeResponseDTO> badges = List.of(new ReviewerBadgeResponseDTO());
        when(reviewerService.getReviewerBadgeHistory(viewerUserId, reviewerId)).thenReturn(badges);

        assertThat(reviewerController.getReviewerBadges(reviewerId, principal(viewerUserId)))
                .isSameAs(badges);
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
