package com.cafestory.controller;

import com.cafestory.dto.responseDTO.RecommendationCardResponseDTO;
import com.cafestory.service.serviceInterface.RecommendationService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/users")
    public List<RecommendationCardResponseDTO> getUserRecommendations(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return recommendationService.getUserRecommendations(requireUserId(principal), page, size);
    }

    @GetMapping("/reviewers")
    public List<RecommendationCardResponseDTO> getReviewerRecommendations(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return recommendationService.getReviewerRecommendations(requireUserId(principal), page, size);
    }

    @GetMapping("/cafe-pages")
    public List<RecommendationCardResponseDTO> getCafePageRecommendations(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return recommendationService.getCafePageRecommendations(requireUserId(principal), page, size);
    }

    @GetMapping("/mixed")
    public List<RecommendationCardResponseDTO> getMixedRecommendations(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return recommendationService.getMixedRecommendations(requireUserId(principal), page, size);
    }
}
