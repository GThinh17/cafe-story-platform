package com.cafestory.controller;

import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.service.serviceInterface.FeedService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final BlogFeedRankingService blogFeedRankingService;
    private final FeedService feedService;

    public FeedController(BlogFeedRankingService blogFeedRankingService, FeedService feedService) {
        this.blogFeedRankingService = blogFeedRankingService;
        this.feedService = feedService;
    }

    @GetMapping
    public FeedResponseDTO getFeed(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return feedService.getFeed(principal == null ? null : principal.userId(), cursor, size);
    }

    @GetMapping("/organic")
    public FeedResponseDTO getOrganicFeed(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return blogFeedRankingService.getOrganicFeed(cursor, size);
    }
}
