package com.cafestory.controller;

import com.cafestory.dto.responseDTO.reviewer.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerPayoutResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerRankingResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerSegmentResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerStatsResponseDTO;
import com.cafestory.service.serviceInterface.ReviewerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;

    public ReviewerController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
    }

    @PostMapping("/create/{userId}")
    public ReviewerResponseDTO createReviewer(@PathVariable UUID userId) {
        return reviewerService.createReviewer(userId);
    }

    @GetMapping("/{reviewerId}/stats")
    public ReviewerStatsResponseDTO getReviewerStats(
            @PathVariable UUID reviewerId,
            @RequestParam UUID requesterId,
            @RequestParam String period) {
        return reviewerService.countReviewerStats(requesterId, reviewerId, period);
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

    @GetMapping("/{reviewerId}/payouts")
    public List<ReviewerPayoutResponseDTO> getReviewerPayouts(
            @PathVariable UUID reviewerId,
            @RequestParam UUID requesterId) {
        return reviewerService.getReviewerPayoutHistory(requesterId, reviewerId);
    }

    @PostMapping("/payouts/generate")
    public List<ReviewerPayoutResponseDTO> generateMonthlyPayouts(
            @RequestParam UUID requesterId,
            @RequestParam String month,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return reviewerService.generateMonthlyPayouts(requesterId, month, overwrite);
    }

    @PostMapping("/badges/generate")
    public List<ReviewerBadgeResponseDTO> generateMonthlyBadges(
            @RequestParam UUID requesterId,
            @RequestParam String month,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return reviewerService.generateMonthlyBadges(requesterId, month, overwrite);
    }

    @GetMapping("/{reviewerId}/badges")
    public List<ReviewerBadgeResponseDTO> getReviewerBadges(
            @PathVariable UUID reviewerId,
            @RequestParam UUID requesterId) {
        return reviewerService.getReviewerBadgeHistory(requesterId, reviewerId);
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}
