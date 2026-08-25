package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerDiscoveryResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerEarningsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerIncomeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    List<ReviewerBadgeResponseDTO> generateMonthlyBadges(UUID requesterId, String month, boolean overwrite);

    /**
     * Lịch sử thu nhập theo tháng của reviewer, đọc từ admin_payout — nguồn duy
     * nhất do job hằng tháng sinh ra. Chỉ chính chủ hoặc admin xem được.
     */
    List<ReviewerEarningsResponseDTO> getReviewerEarnings(UUID requesterId, UUID reviewerId);

    /** Thu nhập chi tiết theo ngày trong một tháng. Chỉ chính chủ hoặc admin. */
    Page<ReviewerIncomeResponseDTO> getReviewerIncome(
            UUID requesterId, UUID reviewerId, String month, Pageable pageable);

    List<ReviewerBadgeResponseDTO> getReviewerBadgeHistory(UUID requesterId, UUID reviewerId);

    List<ReviewerSegmentResponseDTO> getReviewersBySegment(String month, String segment);

    List<ReviewerGeoAnalyticsResponseDTO> getGeoAnalytics(String period, String groupBy);
}
