package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogTrendingResponse;
import com.cafestory.entity.enums.TrendWindowType;

import java.time.LocalDate;
import java.util.List;

public interface BlogTrendingService {
    List<BlogTrendingResponse> getTrendingBlogs(TrendWindowType windowType, int page, int size);

    void aggregateDailyMetrics(LocalDate metricDate);

    void calculateTrendingScores();
}
