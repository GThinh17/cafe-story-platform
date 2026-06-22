package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerDiscoveryResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerPayoutResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerRankingResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerSegmentResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStatsResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReviewerService {

    ReviewerResponseDTO createReviewer(UUID userId);

    ReviewerResponseDTO getReviewer(UUID userId);

    List<ReviewerResponseDTO> getAllReviewer(UUID viewerUserId);

    List<ReviewerResponseDTO> getAllActiveReviewers(UUID viewerUserId);

    List<ReviewerResponseDTO> searchReviewers(String query, UUID viewerUserId);

    ReviewerStatsResponseDTO countReviewerStats(UUID requesterId, UUID reviewerId, String period);

    ReviewerStatsResponseDTO countReviewerStatsByDateRange(UUID reviewerId, LocalDateTime startDate, LocalDateTime endDate);

    List<ReviewerRankingResponseDTO> getReviewerRanking(String period, int page, int limit, String city, String province, String area);

    List<ReviewerDiscoveryResponseDTO> getReviewersInRegion(
            UUID viewerUserId,
            String city,
            String province,
            String ward,
            String area,
            String street,
            int page,
            int size);

    List<ReviewerDiscoveryResponseDTO> getTrendingReviewers(UUID viewerUserId, String window, int page, int size);

    List<ReviewerDiscoveryResponseDTO> getTopReviewers(UUID viewerUserId, int page, int size);

    long calculateReviewerScore(ReviewerStatsResponseDTO stats);

    long calculateReviewerPayout(ReviewerStatsResponseDTO stats);

    String calculateReviewerBadge(long score);

    String calculateReviewerSegment(long score);

    List<ReviewerPayoutResponseDTO> generateMonthlyPayouts(UUID requesterId, String month, boolean overwrite);

    List<ReviewerBadgeResponseDTO> generateMonthlyBadges(UUID requesterId, String month, boolean overwrite);

    List<ReviewerPayoutResponseDTO> getReviewerPayoutHistory(UUID requesterId, UUID reviewerId);

    List<ReviewerBadgeResponseDTO> getReviewerBadgeHistory(UUID requesterId, UUID reviewerId);

    List<ReviewerSegmentResponseDTO> getReviewersBySegment(String month, String segment);

    List<ReviewerGeoAnalyticsResponseDTO> getGeoAnalytics(String period, String groupBy);
}
