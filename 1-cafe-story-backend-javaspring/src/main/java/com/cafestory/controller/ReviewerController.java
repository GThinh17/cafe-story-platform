package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerDiscoveryResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerEarningsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerIncomeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.cafestory.dto.responseDTO.ReviewerRankingResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerSegmentResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStatsResponseDTO;
import com.cafestory.service.serviceInterface.ReviewerService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;

    public ReviewerController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
    }

    @PostMapping("/create")
    public ReviewerResponseDTO createReviewer(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return reviewerService.createReviewer(requireUserId(principal));
    }

    @GetMapping("/region")
    public List<ReviewerDiscoveryResponseDTO> getReviewersInRegion(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String street,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return reviewerService.getReviewersInRegion(
                optionalUserId(principal),
                city,
                province,
                ward,
                firstNonBlank(area, district),
                street,
                page,
                size);
    }

    @GetMapping("/trending")
    public List<ReviewerDiscoveryResponseDTO> getTrendingReviewers(
            @RequestParam(defaultValue = "DAY_7") String window,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return reviewerService.getTrendingReviewers(optionalUserId(principal), window, page, size);
    }

    @GetMapping("/top")
    public List<ReviewerDiscoveryResponseDTO> getTopReviewers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return reviewerService.getTopReviewers(optionalUserId(principal), page, size);
    }

    @GetMapping("/{userId}")
    public ReviewerResponseDTO getReviewer(@PathVariable UUID userId) {
        return reviewerService.getReviewer(userId);
    }

    @GetMapping
    public List<ReviewerResponseDTO> getAllReviewer(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean activeOnly,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        UUID viewerUserId = optionalUserId(principal);
        if (query != null && !query.isBlank()) {
            return reviewerService.searchReviewers(query, viewerUserId);
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            return reviewerService.getAllActiveReviewers(viewerUserId);
        }
        return reviewerService.getAllReviewer(viewerUserId);
    }

    @GetMapping("/{reviewerId}/stats")
    public ReviewerStatsResponseDTO getReviewerStats(
            @PathVariable UUID reviewerId,
            @RequestParam String period,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return reviewerService.countReviewerStats(requireUserId(principal), reviewerId, period);
    }

    @GetMapping("/ranking")
    public List<ReviewerRankingResponseDTO> getReviewerRanking(
            @RequestParam String period,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String district) {
        return reviewerService.getReviewerRanking(period, page, limit, city, province, firstNonBlank(area, district));
    }

    @GetMapping("/segments")
    public List<ReviewerSegmentResponseDTO> getReviewersBySegment(
            @RequestParam String month,
            @RequestParam String segment) {
        return reviewerService.getReviewersBySegment(month, segment);
    }

    @GetMapping("/geo")
    public List<ReviewerGeoAnalyticsResponseDTO> getGeoAnalytics(
            @RequestParam String period,
            @RequestParam String groupBy) {
        return reviewerService.getGeoAnalytics(period, groupBy);
    }

    /**
     * Lịch sử thu nhập theo tháng của chính reviewer. Đọc từ admin_payout —
     * nguồn do job hằng tháng sinh ra, không phải bảng payout thủ công cũ.
     */
    @GetMapping("/{reviewerId}/payouts")
    public List<ReviewerEarningsResponseDTO> getReviewerEarnings(
            @PathVariable UUID reviewerId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return reviewerService.getReviewerEarnings(requireUserId(principal), reviewerId);
    }

    @GetMapping("/{reviewerId}/income")
    public Page<ReviewerIncomeResponseDTO> getReviewerIncome(
            @PathVariable UUID reviewerId,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "31") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("incomeDate").descending());
        return reviewerService.getReviewerIncome(requireUserId(principal), reviewerId, month, pageable);
    }

    @PostMapping("/badges/generate")
    public List<ReviewerBadgeResponseDTO> generateMonthlyBadges(
            @RequestParam String month,
            @RequestParam(defaultValue = "false") boolean overwrite,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return reviewerService.generateMonthlyBadges(requireUserId(principal), month, overwrite);
    }

    @GetMapping("/{reviewerId}/badges")
    public List<ReviewerBadgeResponseDTO> getReviewerBadges(
            @PathVariable UUID reviewerId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return reviewerService.getReviewerBadgeHistory(requireUserId(principal), reviewerId);
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private UUID optionalUserId(AuthenticatedUserPrincipal principal) {
        return principal == null ? null : principal.userId();
    }
}
