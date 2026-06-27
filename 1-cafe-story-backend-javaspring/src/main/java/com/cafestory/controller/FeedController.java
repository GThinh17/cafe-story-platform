package com.cafestory.controller;

import com.cafestory.dto.requestDTO.FeedImpressionBatchRequestDTO;
import com.cafestory.dto.responseDTO.FeedImpressionResponseDTO;
import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.service.serviceInterface.FeedImpressionService;
import com.cafestory.service.serviceInterface.FeedService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final BlogFeedRankingService blogFeedRankingService;
    private final FeedService feedService;
    private final FeedImpressionService feedImpressionService;

    public FeedController(
            BlogFeedRankingService blogFeedRankingService,
            FeedService feedService,
            FeedImpressionService feedImpressionService) {
        this.blogFeedRankingService = blogFeedRankingService;
        this.feedService = feedService;
        this.feedImpressionService = feedImpressionService;
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

    @PostMapping("/impressions")
    public FeedImpressionResponseDTO recordImpressions(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody FeedImpressionBatchRequestDTO request) {
        return feedImpressionService.recordImpressions(requireUserId(principal), request);
    }
}
