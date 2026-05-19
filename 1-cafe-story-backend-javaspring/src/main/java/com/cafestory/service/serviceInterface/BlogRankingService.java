package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.BlogRankingOverrideRequest;
import com.cafestory.dto.responseDTO.BlogRankingOverrideResponse;
import com.cafestory.entity.BlogDailyMetric;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BlogRankingService {
    BlogRankingOverrideResponse createOverride(UUID blogId, BlogRankingOverrideRequest request);

    double getActiveAdminBoost(UUID blogId, LocalDateTime now);

    boolean hasActivePinnedOverride(UUID blogId, LocalDateTime now);

    double calculateTrendScore(
            BlogDailyMetric metric,
            LocalDateTime blogCreatedAt,
            double adminBoost,
            LocalDateTime now);

    String buildTrendReason(BlogDailyMetric metric, double adminBoost);
}
