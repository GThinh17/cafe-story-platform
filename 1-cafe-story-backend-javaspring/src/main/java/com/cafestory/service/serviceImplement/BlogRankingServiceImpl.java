package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.BlogRankingOverrideRequest;
import com.cafestory.dto.responseDTO.BlogRankingOverrideResponse;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogDailyMetric;
import com.cafestory.entity.BlogRankingOverride;
import com.cafestory.repository.BlogRankingOverrideRepository;
import com.cafestory.service.serviceInterface.BlogRankingService;
import com.cafestory.validation.BlogValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BlogRankingServiceImpl implements BlogRankingService {

    private final BlogRankingOverrideRepository blogRankingOverrideRepository;
    private final BlogValidator blogValidator;

    public BlogRankingServiceImpl(
            BlogRankingOverrideRepository blogRankingOverrideRepository,
            BlogValidator blogValidator) {
        this.blogRankingOverrideRepository = blogRankingOverrideRepository;
        this.blogValidator = blogValidator;
    }

    @Override
    @Transactional
    public BlogRankingOverrideResponse createOverride(UUID blogId, BlogRankingOverrideRequest request) {
        Blog blog = blogValidator.validateBlogExists(blogId);

        BlogRankingOverride override = new BlogRankingOverride();
        override.setBlog(blog);
        override.setBoostScore(request.getBoostScore() == null ? 0.0 : request.getBoostScore());
        override.setIsPinned(Boolean.TRUE.equals(request.getIsPinned()));
        override.setReason(request.getReason());
        override.setStartAt(request.getStartAt());
        override.setEndAt(request.getEndAt());
        override.setCreatedBy(request.getCreatedBy());

        return toResponse(blogRankingOverrideRepository.save(override));
    }

    @Override
    @Transactional(readOnly = true)
    public double getActiveAdminBoost(UUID blogId, LocalDateTime now) {
        Double boostScore = blogRankingOverrideRepository.sumActiveBoostScore(blogId, now);
        return boostScore == null ? 0.0 : boostScore;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActivePinnedOverride(UUID blogId, LocalDateTime now) {
        return blogRankingOverrideRepository.existsActivePinnedOverride(blogId, now);
    }

    @Override
    public double calculateTrendScore(
            BlogDailyMetric metric,
            LocalDateTime blogCreatedAt,
            double adminBoost,
            LocalDateTime now) {
        double engagementScore = safe(metric.getViews()) * 0.2
                + safe(metric.getLikes()) * 2
                + safe(metric.getComments()) * 4
                + safe(metric.getShares()) * 6
                + safe(metric.getSaves()) * 5;
        double penaltyScore = safe(metric.getReports()) * 10;
        double ageHours = Math.max(0, Duration.between(blogCreatedAt, now).toMinutes() / 60.0);
        double freshnessScore = Math.exp(-ageHours / 48.0);
        return (engagementScore * freshnessScore) - penaltyScore + adminBoost;
    }

    @Override
    public String buildTrendReason(BlogDailyMetric metric, double adminBoost) {
        return "views=" + safe(metric.getViews())
                + ", likes=" + safe(metric.getLikes())
                + ", comments=" + safe(metric.getComments())
                + ", shares=" + safe(metric.getShares())
                + ", saves=" + safe(metric.getSaves())
                + ", reports=" + safe(metric.getReports())
                + ", adminBoost=" + adminBoost;
    }

    private BlogRankingOverrideResponse toResponse(BlogRankingOverride override) {
        BlogRankingOverrideResponse response = new BlogRankingOverrideResponse();
        response.setId(override.getId());
        response.setBlogId(override.getBlog().getId());
        response.setBoostScore(override.getBoostScore());
        response.setIsPinned(override.getIsPinned());
        response.setReason(override.getReason());
        response.setStartAt(override.getStartAt());
        response.setEndAt(override.getEndAt());
        response.setCreatedBy(override.getCreatedBy());
        response.setCreatedAt(override.getCreatedAt());
        return response;
    }

    private long safe(Long value) {
        return value == null ? 0L : value;
    }
}
