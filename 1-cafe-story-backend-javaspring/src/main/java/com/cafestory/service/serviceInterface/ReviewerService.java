package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.reviewer.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerPayoutResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerRankingResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerSegmentResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerStatsResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReviewerService {

    ReviewerStatsResponseDTO countReviewerStats(UUID requesterId, UUID reviewerId, String period);

    ReviewerStatsResponseDTO countReviewerStatsByDateRange(UUID reviewerId, LocalDateTime startDate, LocalDateTime endDate);

    List<ReviewerRankingResponseDTO> getReviewerRanking(String period, int page, int limit, String city, String province, String district);

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
