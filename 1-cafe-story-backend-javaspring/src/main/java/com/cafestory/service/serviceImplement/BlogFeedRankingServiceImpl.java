package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRecommendationScore;
import com.cafestory.entity.BlogTrendingScore;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.BlogRecommendationScoreRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogTrendingScoreRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.validation.UserValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BlogFeedRankingServiceImpl implements BlogFeedRankingService {

    private final BlogRepository blogRepository;
    private final BlogTrendingScoreRepository blogTrendingScoreRepository;
    private final BlogRecommendationScoreRepository blogRecommendationScoreRepository;
    private final CafePageRepository cafePageRepository;
    private final PageFollowRepository pageFollowRepository;
    private final UserFollowRepository userFollowRepository;
    private final BlogEventRepository blogEventRepository;
    private final AiModerationResultRepository aiModerationResultRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public BlogFeedRankingServiceImpl(
            BlogRepository blogRepository,
            BlogTrendingScoreRepository blogTrendingScoreRepository,
            BlogRecommendationScoreRepository blogRecommendationScoreRepository,
            CafePageRepository cafePageRepository,
            PageFollowRepository pageFollowRepository,
            UserFollowRepository userFollowRepository,
            BlogEventRepository blogEventRepository,
            AiModerationResultRepository aiModerationResultRepository,
            RegionRepository regionRepository,
            UserRepository userRepository,
            UserValidator userValidator) {
        this.blogRepository = blogRepository;
        this.blogTrendingScoreRepository = blogTrendingScoreRepository;
        this.blogRecommendationScoreRepository = blogRecommendationScoreRepository;
        this.cafePageRepository = cafePageRepository;
        this.pageFollowRepository = pageFollowRepository;
        this.userFollowRepository = userFollowRepository;
        this.blogEventRepository = blogEventRepository;
        this.aiModerationResultRepository = aiModerationResultRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public List<BlogFeedResponse> getPersonalizedFeed(
            UUID userId,
            TrendWindowType windowType,
            UUID regionId,
            int page,
            int size) {
        User user = validateActiveUser(userId);
        UUID contextRegionId = resolveContextRegionId(user, regionId);
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);

        LocalDateTime latestComputedAt = blogRecommendationScoreRepository.findLatestComputedAt(
                userId,
                windowType,
                contextRegionId);
        if (latestComputedAt == null) {
            rebuildRecommendationCache(userId, windowType, contextRegionId);
            latestComputedAt = blogRecommendationScoreRepository.findLatestComputedAt(userId, windowType, contextRegionId);
        }
        if (latestComputedAt == null) {
            return List.of();
        }

        List<BlogRecommendationScore> scores = blogRecommendationScoreRepository.findLatestPage(
                        userId,
                        windowType,
                        contextRegionId,
                        latestComputedAt,
                        PageRequest.of(safePage, safeSize));
        return toFeedResponses(scores);
    }

    @Override
    @Transactional
    public List<BlogFeedResponse> rebuildRecommendationCache(
            UUID userId,
            TrendWindowType windowType,
            UUID regionId) {
        User user = validateActiveUser(userId);
        UUID contextRegionId = resolveContextRegionId(user, regionId);
        LocalDateTime now = LocalDateTime.now();
        Map<UUID, BlogTrendingScore> trendingScores = latestTrendingScores(windowType);

        List<BlogRecommendationScore> scores = blogRepository.findByStatus(PostStatus.PUBLISHED)
                .stream()
                .filter(blog -> !aiModerationResultRepository.existsByBlogIdAndDecision(
                        blog.getId(),
                        ModerationDecision.VIOLATION))
                .map(blog -> createRecommendationScore(
                        user,
                        blog,
                        trendingScores.get(blog.getId()),
                        windowType,
                        contextRegionId,
                        resolveContextCity(user, contextRegionId),
                        now))
                .sorted(Comparator.comparing(BlogRecommendationScore::getFeedScore).reversed())
                .toList();

        for (int index = 0; index < scores.size(); index++) {
            scores.get(index).setRankPosition(index + 1);
        }

        blogRecommendationScoreRepository.deleteByUserWindowAndContextRegion(userId, windowType, contextRegionId);
        blogRecommendationScoreRepository.saveAll(scores);
        return toFeedResponses(scores);
    }

    @Override
    @Transactional
    public void rebuildRecommendationCacheForAllActiveUsers() {
        userRepository.findByAccountStatusTrue().forEach(user -> {
            UUID regionId = user.getRegion() == null ? null : user.getRegion().getRegionId();
            for (TrendWindowType windowType : TrendWindowType.values()) {
                rebuildRecommendationCache(user.getUserId(), windowType, regionId);
            }
        });
    }

    private User validateActiveUser(UUID userId) {
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);
        return user;
    }

    private UUID resolveContextRegionId(User user, UUID regionId) {
        if (regionId != null) {
            return regionId;
        }
        return user.getRegion() == null ? null : user.getRegion().getRegionId();
    }

    private Map<UUID, BlogTrendingScore> latestTrendingScores(TrendWindowType windowType) {
        LocalDateTime latestComputedAt = blogTrendingScoreRepository.findLatestComputedAt(windowType);
        if (latestComputedAt == null) {
            return Map.of();
        }
        return blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(windowType, latestComputedAt)
                .stream()
                .collect(Collectors.toMap(score -> score.getBlog().getId(), Function.identity()));
    }

    private BlogRecommendationScore createRecommendationScore(
            User user,
            Blog blog,
            BlogTrendingScore trendingScore,
            TrendWindowType windowType,
            UUID contextRegionId,
            String contextCity,
            LocalDateTime now) {
        double baseTrendingScore = trendingScore == null ? 0.0 : trendingScore.getTrendScore();
        double trendingComponent = baseTrendingScore * 0.4;
        double followedPageScore = calculateFollowedPageScore(blog, user.getUserId());
        double followedUserScore = calculateFollowedUserScore(blog, user.getUserId());
        double sameRegionScore = calculateSameRegionScore(blog, contextCity);
        double freshnessScore = calculateFreshnessScore(blog, now);
        double reportPenalty = calculateReportPenalty(blog, windowType, now);
        double feedScore = trendingComponent
                + followedPageScore
                + followedUserScore
                + sameRegionScore
                + freshnessScore
                - reportPenalty;

        BlogRecommendationScore score = new BlogRecommendationScore();
        score.setUser(user);
        score.setBlog(blog);
        score.setWindowType(windowType);
        score.setContextRegionId(contextRegionId);
        score.setFeedScore(feedScore);
        score.setTrendingScore(baseTrendingScore);
        score.setFollowedPageScore(followedPageScore);
        score.setFollowedUserScore(followedUserScore);
        score.setSameRegionScore(sameRegionScore);
        score.setFreshnessScore(freshnessScore);
        score.setReportPenalty(reportPenalty);
        score.setRankPosition(0);
        score.setReason(buildReason(trendingComponent, followedPageScore, followedUserScore, sameRegionScore,
                freshnessScore, reportPenalty));
        score.setComputedAt(now);
        return score;
    }

    private List<BlogFeedResponse> toFeedResponses(List<BlogRecommendationScore> scores) {
        List<UUID> pageIds = scores.stream()
                .map(score -> score.getBlog().getPageId())
                .filter(pageId -> pageId != null)
                .distinct()
                .toList();
        Map<UUID, CafePage> cafePagesById = cafePageRepository.findAllById(pageIds)
                .stream()
                .collect(Collectors.toMap(CafePage::getId, Function.identity()));
        List<UUID> regionIds = scores.stream()
                .map(score -> score.getBlog().getRegionId())
                .filter(regionId -> regionId != null)
                .distinct()
                .toList();
        Map<UUID, Region> regionsById = regionRepository.findAllById(regionIds)
                .stream()
                .collect(Collectors.toMap(Region::getRegionId, Function.identity()));

        return scores.stream()
                .map(score -> toFeedResponse(score, cafePagesById, regionsById))
                .toList();
    }

    private BlogFeedResponse toFeedResponse(
            BlogRecommendationScore score,
            Map<UUID, CafePage> cafePagesById,
            Map<UUID, Region> regionsById) {
        Blog blog = score.getBlog();
        User author = blog.getAuthor();
        CafePage cafePage = blog.getPageId() == null ? null : cafePagesById.get(blog.getPageId());
        Region region = blog.getRegionId() == null ? null : regionsById.get(blog.getRegionId());
        BlogFeedResponse response = new BlogFeedResponse();
        response.setBlogId(blog.getId());
        response.setContentPreview(toPreview(blog.getContent()));
        response.setImageUrls(blog.getImageUrls());
        response.setLikeCount(blog.getLikeCount());
        response.setCommentCount(blog.getCommentCount());
        response.setShareCount(blog.getShareCount());
        response.setAuthorUserId(author.getUserId());
        response.setAuthorUserName(author.getUserName());
        response.setAuthorUserFullName(author.getUserFullName());
        response.setAuthorAvatar(author.getUserAvatar());
        response.setPageId(blog.getPageId());
        if (cafePage != null) {
            response.setPageName(cafePage.getName());
            response.setPageAddress(cafePage.getAddress());
            response.setPageAvatarUrl(cafePage.getAvatarUrl());
            response.setPageCoverUrl(cafePage.getCoverUrl());
        }
        response.setRegionId(blog.getRegionId());
        if (region != null) {
            response.setRegionCity(region.getCity());
            response.setRegionProvince(region.getProvince());
            response.setRegionArea(region.getArea());
        }
        response.setRankPosition(score.getRankPosition());
        response.setCreatedAt(blog.getCreatedAt());
        return response;
    }

    private double calculateFollowedPageScore(Blog blog, UUID userId) {
        if (blog.getPageId() == null) {
            return 0.0;
        }
        return pageFollowRepository.existsByUserUserIdAndCafePageId(userId, blog.getPageId()) ? 30.0 : 0.0;
    }

    private double calculateFollowedUserScore(Blog blog, UUID userId) {
        if (blog.getAuthor() == null || blog.getAuthor().getUserId() == null) {
            return 0.0;
        }
        return userFollowRepository.existsByFollowerUserIdAndFollowingUserId(userId, blog.getAuthor().getUserId())
                ? 25.0
                : 0.0;
    }

    private double calculateSameRegionScore(Blog blog, String contextCity) {
        if (isBlank(contextCity) || blog.getRegionId() == null) {
            return 0.0;
        }
        String blogCity = regionRepository.findById(blog.getRegionId())
                .map(Region::getCity)
                .orElse(null);
        if (isBlank(blogCity)) {
            return 0.0;
        }
        return normalizeCity(contextCity).equals(normalizeCity(blogCity)) ? 15.0 : 0.0;
    }

    private String resolveContextCity(User user, UUID contextRegionId) {
        if (contextRegionId != null) {
            return regionRepository.findById(contextRegionId)
                    .map(Region::getCity)
                    .orElse(null);
        }
        return user.getRegion() == null ? null : user.getRegion().getCity();
    }

    private String normalizeCity(String city) {
        return city.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private double calculateFreshnessScore(Blog blog, LocalDateTime now) {
        if (blog.getCreatedAt() == null) {
            return 0.0;
        }
        double ageHours = Math.max(0, Duration.between(blog.getCreatedAt(), now).toMinutes() / 60.0);
        return Math.exp(-ageHours / 48.0) * 10.0;
    }

    private double calculateReportPenalty(Blog blog, TrendWindowType windowType, LocalDateTime now) {
        LocalDateTime startAt = switch (windowType) {
            case HOUR_24 -> now.minusHours(24);
            case DAY_7 -> now.minusDays(7);
            case MONTH_1 -> now.minusMonths(1);
        };
        long reports = blogEventRepository.countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                blog.getId(),
                BlogEventType.REPORT,
                startAt,
                now);
        return reports * 10.0;
    }

    private String buildReason(
            double trendingComponent,
            double followedPageScore,
            double followedUserScore,
            double sameRegionScore,
            double freshnessScore,
            double reportPenalty) {
        return "trendingComponent=" + trendingComponent
                + ", followedPageScore=" + followedPageScore
                + ", followedUserScore=" + followedUserScore
                + ", sameRegionScore=" + sameRegionScore
                + ", freshnessScore=" + freshnessScore
                + ", reportPenalty=" + reportPenalty;
    }

    private String toPreview(String content) {
        if (content == null || content.length() <= 140) {
            return content;
        }
        return content.substring(0, 140);
    }
}
