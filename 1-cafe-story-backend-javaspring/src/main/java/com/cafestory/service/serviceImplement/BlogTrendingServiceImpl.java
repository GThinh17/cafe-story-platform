package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.BlogTrendingResponse;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogDailyMetric;
import com.cafestory.entity.BlogTrendingScore;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogDailyMetricRepository;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.BlogTrendingScoreRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.service.serviceInterface.BlogRankingService;
import com.cafestory.service.serviceInterface.BlogTrendingService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BlogTrendingServiceImpl implements BlogTrendingService {

    private final BlogRepository blogRepository;
    private final BlogEventRepository blogEventRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final CommentRepository commentRepository;
    private final BlogShareRepository blogShareRepository;
    private final BlogDailyMetricRepository blogDailyMetricRepository;
    private final BlogTrendingScoreRepository blogTrendingScoreRepository;
    private final BlogRankingService blogRankingService;
    private final AiModerationResultRepository aiModerationResultRepository;
    private final CafePageRepository cafePageRepository;

    public BlogTrendingServiceImpl(
            BlogRepository blogRepository,
            BlogEventRepository blogEventRepository,
            BlogLikeRepository blogLikeRepository,
            CommentRepository commentRepository,
            BlogShareRepository blogShareRepository,
            BlogDailyMetricRepository blogDailyMetricRepository,
            BlogTrendingScoreRepository blogTrendingScoreRepository,
            BlogRankingService blogRankingService,
            AiModerationResultRepository aiModerationResultRepository,
            CafePageRepository cafePageRepository) {
        this.blogRepository = blogRepository;
        this.blogEventRepository = blogEventRepository;
        this.blogLikeRepository = blogLikeRepository;
        this.commentRepository = commentRepository;
        this.blogShareRepository = blogShareRepository;
        this.blogDailyMetricRepository = blogDailyMetricRepository;
        this.blogTrendingScoreRepository = blogTrendingScoreRepository;
        this.blogRankingService = blogRankingService;
        this.aiModerationResultRepository = aiModerationResultRepository;
        this.cafePageRepository = cafePageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.TRENDING_BLOGS_CACHE, key = "#p0.name() + ':' + #p1 + ':' + #p2")
    public List<BlogTrendingResponse> getTrendingBlogs(TrendWindowType windowType, int page, int size) {
        LocalDateTime latestComputedAt = blogTrendingScoreRepository.findLatestComputedAt(windowType);
        if (latestComputedAt == null) {
            return List.of();
        }
        return blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                        windowType,
                        latestComputedAt,
                        PageRequest.of(Math.max(0, page), Math.max(1, size)))
                .stream()
                .map(this::toTrendingResponse)
                .toList();
    }

    @Override
    @Transactional
    public void aggregateDailyMetrics(LocalDate metricDate) {
        LocalDateTime startAt = metricDate.atStartOfDay();
        LocalDateTime endAt = metricDate.plusDays(1).atStartOfDay();

        blogRepository.findByStatus(PostStatus.PUBLISHED)
                .forEach(blog -> saveDailyMetric(blog, metricDate, startAt, endAt));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.TRENDING_BLOGS_CACHE, allEntries = true)
    public void calculateTrendingScores() {
        LocalDateTime now = LocalDateTime.now();
        for (TrendWindowType windowType : TrendWindowType.values()) {
            saveTrendingScoresForWindow(windowType, now);
        }
    }

    private void saveDailyMetric(Blog blog, LocalDate metricDate, LocalDateTime startAt, LocalDateTime endAt) {
        BlogDailyMetric metric = blogDailyMetricRepository.findByBlogIdAndMetricDate(blog.getId(), metricDate)
                .orElseGet(() -> newMetric(blog, metricDate));
        applyMetricCounts(metric, blog.getId(), startAt, endAt);
        blogDailyMetricRepository.save(metric);
    }

    private void saveTrendingScoresForWindow(TrendWindowType windowType, LocalDateTime now) {
        LocalDateTime startAt = windowStart(windowType, now);
        List<BlogTrendingScore> scores = blogRepository.findByStatus(PostStatus.PUBLISHED)
                .stream()
                .filter(blog -> !aiModerationResultRepository.existsByBlogIdAndDecision(
                        blog.getId(),
                        ModerationDecision.VIOLATION))
                .map(blog -> createScore(blog, windowType, startAt, now))
                .sorted(Comparator
                        .comparing((BlogTrendingScore score) -> !blogRankingService.hasActivePinnedOverride(
                                score.getBlog().getId(),
                                now))
                        .thenComparing(BlogTrendingScore::getTrendScore, Comparator.reverseOrder()))
                .toList();

        for (int index = 0; index < scores.size(); index++) {
            BlogTrendingScore score = scores.get(index);
            score.setRankPosition(index + 1);
            blogTrendingScoreRepository.save(score);
        }
    }

    private BlogTrendingScore createScore(
            Blog blog,
            TrendWindowType windowType,
            LocalDateTime startAt,
            LocalDateTime now) {
        BlogDailyMetric metric = newMetric(blog, now.toLocalDate());
        applyMetricCounts(metric, blog.getId(), startAt, now);
        double adminBoost = blogRankingService.getActiveAdminBoost(blog.getId(), now);

        BlogTrendingScore score = new BlogTrendingScore();
        score.setBlog(blog);
        score.setWindowType(windowType);
        score.setTrendScore(blogRankingService.calculateTrendScore(metric, blog.getCreatedAt(), adminBoost, now));
        score.setRankPosition(0);
        score.setReason(blogRankingService.buildTrendReason(metric, adminBoost));
        score.setComputedAt(now);
        return score;
    }

    private void applyMetricCounts(
            BlogDailyMetric metric,
            java.util.UUID blogId,
            LocalDateTime startAt,
            LocalDateTime endAt) {
        metric.setViews(blogEventRepository.countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                blogId,
                BlogEventType.VIEW,
                startAt,
                endAt));
        metric.setLikes(blogLikeRepository.countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                blogId,
                startAt,
                endAt));
        metric.setComments(commentRepository.countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                blogId,
                startAt,
                endAt));
        metric.setShares(blogShareRepository.countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                blogId,
                startAt,
                endAt));
        metric.setSaves(blogEventRepository.countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                blogId,
                BlogEventType.SAVE,
                startAt,
                endAt));
        metric.setReports(blogEventRepository.countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                blogId,
                BlogEventType.REPORT,
                startAt,
                endAt));
        metric.setAvgReadSeconds(0.0);
    }

    private BlogDailyMetric newMetric(Blog blog, LocalDate metricDate) {
        BlogDailyMetric metric = new BlogDailyMetric();
        metric.setBlog(blog);
        metric.setMetricDate(metricDate);
        return metric;
    }

    private LocalDateTime windowStart(TrendWindowType windowType, LocalDateTime now) {
        return switch (windowType) {
            case HOUR_24 -> now.minusHours(24);
            case DAY_7 -> now.minusDays(7);
            case MONTH_1 -> now.minusMonths(1);
        };
    }

    private BlogTrendingResponse toTrendingResponse(BlogTrendingScore score) {
        Blog blog = score.getBlog();
        BlogTrendingResponse response = new BlogTrendingResponse();
        response.setBlogId(blog.getId());
        response.setContentPreview(toPreview(blog.getContent()));
        response.setAuthorUserId(blog.getAuthor().getUserId());
        response.setAuthorUserName(blog.getAuthor().getUserName());
        response.setPageId(blog.getPageId());
        response.setPageName(findPageName(blog));
        response.setWindowType(score.getWindowType());
        response.setTrendScore(score.getTrendScore());
        response.setRankPosition(score.getRankPosition());
        response.setReason(score.getReason());
        response.setPinned(blogRankingService.hasActivePinnedOverride(blog.getId(), LocalDateTime.now()));
        response.setCreatedAt(blog.getCreatedAt());
        response.setComputedAt(score.getComputedAt());
        return response;
    }

    private String findPageName(Blog blog) {
        if (blog.getPageId() == null) {
            return null;
        }
        Optional<CafePage> cafePage = cafePageRepository.findById(blog.getPageId());
        return cafePage.map(CafePage::getName).orElse(null);
    }

    private String toPreview(String content) {
        if (content == null || content.length() <= 140) {
            return content;
        }
        return content.substring(0, 140);
    }
}
