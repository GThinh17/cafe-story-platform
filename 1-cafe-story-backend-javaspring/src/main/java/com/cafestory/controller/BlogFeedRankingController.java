package com.cafestory.controller;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/blogs/feed")
public class BlogFeedRankingController {

    private final BlogFeedRankingService blogFeedRankingService;

    public BlogFeedRankingController(BlogFeedRankingService blogFeedRankingService) {
        this.blogFeedRankingService = blogFeedRankingService;
    }

    @GetMapping
    public List<BlogFeedResponse> getPersonalizedFeed(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "HOUR_24") TrendWindowType windowType,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return blogFeedRankingService.getPersonalizedFeed(requireUserId(principal), windowType, regionId, page, size);
    }

    @PostMapping("/rebuild")
    public List<BlogFeedResponse> rebuildPersonalizedFeed(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "HOUR_24") TrendWindowType windowType,
            @RequestParam(required = false) UUID regionId) {
        return blogFeedRankingService.rebuildRecommendationCache(requireUserId(principal), windowType, regionId);
    }
}
