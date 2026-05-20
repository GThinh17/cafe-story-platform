package com.cafestory.until.schedule;

import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BlogRecommendationCalculationJob {

    private final BlogFeedRankingService blogFeedRankingService;

    public BlogRecommendationCalculationJob(BlogFeedRankingService blogFeedRankingService) {
        this.blogFeedRankingService = blogFeedRankingService;
    }

    @Scheduled(fixedRate = 900000, initialDelay = 60000)
    public void rebuildRecommendationScores() {
        blogFeedRankingService.rebuildRecommendationCacheForAllActiveUsers();
    }
}
