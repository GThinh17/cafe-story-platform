package com.cafestory.service.serviceInterface;

import com.cafestory.entity.BlogRecommendationScore;

import java.time.LocalDateTime;
import java.util.List;

public interface BlogRecommendationScoreBatchWriter {

    void upsertAll(List<BlogRecommendationScore> scores, LocalDateTime createdAt);
}
