package com.cafestory.controller;

import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final BlogFeedRankingService blogFeedRankingService;

    public FeedController(BlogFeedRankingService blogFeedRankingService) {
        this.blogFeedRankingService = blogFeedRankingService;
    }

    @GetMapping("/organic")
    public FeedResponseDTO getOrganicFeed(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return blogFeedRankingService.getOrganicFeed(cursor, size);
    }
}
