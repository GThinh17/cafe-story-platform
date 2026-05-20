package com.cafestory.until.schedule;

import com.cafestory.service.serviceInterface.BlogTrendingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TrendCalculationJob {

    private final BlogTrendingService blogTrendingService;

    public TrendCalculationJob(BlogTrendingService blogTrendingService) {
        this.blogTrendingService = blogTrendingService;
    }

    @Scheduled(fixedRate = 900000)
    public void calculateTrendingScores() {
        blogTrendingService.aggregateDailyMetrics(LocalDate.now());
        blogTrendingService.calculateTrendingScores();
    }
}
