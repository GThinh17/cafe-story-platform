package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.BlogDisplayAuthorType;
import com.cafestory.dto.responseDTO.BlogFeedCursorPageResponseDTO;
import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.dto.responseDTO.FeedItemResponseDTO;
import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRecommendationScore;
import com.cafestory.entity.BlogTrendingScore;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.FeedItemType;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BlogFeedRankingServiceImpl implements BlogFeedRankingService {
    private static final int DEFAULT_ORGANIC_FEED_SIZE = 20;
    private static final int MAX_ORGANIC_FEED_SIZE = 50;
    private static final int ORGANIC_CURSOR_VERSION = 1;
    private static final double OWN_AUTHOR_SCORE = 40.0;
    private static final ObjectMapper CURSOR_OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

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
    @Transactional(readOnly = true)
    public FeedResponseDTO getOrganicFeed(String cursor, int size) {
        int safeSize = normalizeOrganicFeedSize(size);
        OrganicFeedCursor organicCursor = decodeOrganicCursor(cursor);
        LocalDateTime scoredAt = organicCursor == null ? LocalDateTime.now() : organicCursor.scoredAt();

        List<ScoredOrganicBlog> scoredBlogs = blogRepository.findByStatus(PostStatus.PUBLISHED)
                .stream()
                .filter(blog -> !aiModerationResultRepository.existsByBlogIdAndDecision(
                        blog.getId(),
                        ModerationDecision.VIOLATION))
                .map(blog -> new ScoredOrganicBlog(blog, calculateOrganicScore(blog, scoredAt)))
                .sorted(organicFeedComparator())
                .filter(item -> isAfterOrganicCursor(item, organicCursor))
                .limit(safeSize + 1L)
                .toList();

        boolean hasMore = scoredBlogs.size() > safeSize;
        List<ScoredOrganicBlog> pageItems = hasMore ? scoredBlogs.subList(0, safeSize) : scoredBlogs;
        BlogFeedCursorPageResponseDTO response = new BlogFeedCursorPageResponseDTO();
        response.setItems(toOrganicFeedResponses(pageItems));
        response.setHasMore(hasMore);
        response.setNextCursor(hasMore && !pageItems.isEmpty()
                ? encodeOrganicCursor(pageItems.getLast(), scoredAt)
                : null);
        return toFeedResponse(response);
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
        if (latestComputedAt == null || isPersonalizedCacheStale(userId, latestComputedAt)) {
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

    private boolean isPersonalizedCacheStale(UUID userId, LocalDateTime latestComputedAt) {
        return blogRepository.findFirstByAuthorUserIdAndStatusOrderByCreatedAtDescIdDesc(userId, PostStatus.PUBLISHED)
                .map(blog -> blog.getCreatedAt() != null && blog.getCreatedAt().isAfter(latestComputedAt))
                .orElse(false);
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
        double ownAuthorScore = calculateOwnAuthorScore(blog, user.getUserId());
        double followedPageScore = calculateFollowedPageScore(blog, user.getUserId());
        double followedUserScore = calculateFollowedUserScore(blog, user.getUserId());
        double sameRegionScore = calculateSameRegionScore(blog, contextCity);
        double freshnessScore = calculateFreshnessScore(blog, now);
        double reportPenalty = calculateReportPenalty(blog, windowType, now);
        double feedScore = trendingComponent
                + ownAuthorScore
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
        score.setReason(buildReason(trendingComponent, ownAuthorScore, followedPageScore, followedUserScore, sameRegionScore,
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
        response.setAuthorUserAvatar(author.getUserAvatar());
        response.setPageId(blog.getPageId());
        if (cafePage != null) {
            response.setPageName(cafePage.getName());
            response.setPageAddress(cafePage.getAddress());
            response.setPageAvatarUrl(cafePage.getAvatarUrl());
            response.setPageCoverUrl(cafePage.getCoverUrl());
        }
        applyDisplayAuthor(response, author, cafePage);
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

    private void applyDisplayAuthor(BlogFeedResponse response, User author, CafePage cafePage) {
        if (cafePage != null) {
            response.setDisplayAuthorType(BlogDisplayAuthorType.CAFE_PAGE);
            response.setDisplayName(cafePage.getName());
            response.setDisplayAvatarUrl(cafePage.getAvatarUrl());
            return;
        }

        response.setDisplayAuthorType(BlogDisplayAuthorType.USER);
        response.setDisplayName(firstNonBlank(author.getUserFullName(), author.getUserName()));
        response.setDisplayAvatarUrl(author.getUserAvatar());
    }

    private String firstNonBlank(String first, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        return fallback;
    }

    private double calculateFollowedPageScore(Blog blog, UUID userId) {
        if (blog.getPageId() == null) {
            return 0.0;
        }
        return pageFollowRepository.existsByUserUserIdAndCafePageId(userId, blog.getPageId()) ? 30.0 : 0.0;
    }

    private double calculateOwnAuthorScore(Blog blog, UUID userId) {
        if (blog.getAuthor() == null || blog.getAuthor().getUserId() == null) {
            return 0.0;
        }
        return blog.getAuthor().getUserId().equals(userId) ? OWN_AUTHOR_SCORE : 0.0;
    }

    private double calculateFollowedUserScore(Blog blog, UUID userId) {
        if (blog.getAuthor() == null || blog.getAuthor().getUserId() == null) {
            return 0.0;
        }
        if (blog.getAuthor().getUserId().equals(userId)) {
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
            double ownAuthorScore,
            double followedPageScore,
            double followedUserScore,
            double sameRegionScore,
            double freshnessScore,
            double reportPenalty) {
        return "trendingComponent=" + trendingComponent
                + ", ownAuthorScore=" + ownAuthorScore
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

    private int normalizeOrganicFeedSize(int size) {
        if (size <= 0) {
            return DEFAULT_ORGANIC_FEED_SIZE;
        }
        return Math.min(size, MAX_ORGANIC_FEED_SIZE);
    }

    private double calculateOrganicScore(Blog blog, LocalDateTime scoredAt) {
        double engagementScore = valueOrZero(blog.getLikeCount()) * 2.0
                + valueOrZero(blog.getCommentCount()) * 4.0
                + valueOrZero(blog.getShareCount()) * 5.0;
        double freshnessScore = calculateOrganicFreshnessScore(blog, scoredAt);
        double pageBonus = blog.getPageId() == null ? 0.0 : 5.0;
        double reportPenalty = calculateOrganicReportPenalty(blog, scoredAt);
        return engagementScore + freshnessScore + pageBonus - reportPenalty;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double calculateOrganicFreshnessScore(Blog blog, LocalDateTime scoredAt) {
        if (blog.getCreatedAt() == null) {
            return 0.0;
        }
        double ageHours = Math.max(0, Duration.between(blog.getCreatedAt(), scoredAt).toMinutes() / 60.0);
        return Math.exp(-ageHours / 36.0) * 30.0;
    }

    private double calculateOrganicReportPenalty(Blog blog, LocalDateTime scoredAt) {
        long reports = blogEventRepository.countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                blog.getId(),
                BlogEventType.REPORT,
                LocalDateTime.of(1970, 1, 1, 0, 0),
                scoredAt);
        return reports * 10.0;
    }

    private Comparator<ScoredOrganicBlog> organicFeedComparator() {
        return Comparator.comparingDouble(ScoredOrganicBlog::score).reversed()
                .thenComparing(
                        item -> item.blog().getCreatedAt(),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(
                        item -> item.blog().getId(),
                        Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private boolean isAfterOrganicCursor(ScoredOrganicBlog item, OrganicFeedCursor cursor) {
        if (cursor == null) {
            return true;
        }
        int scoreComparison = Double.compare(item.score(), cursor.afterScore());
        if (scoreComparison < 0) {
            return true;
        }
        if (scoreComparison > 0) {
            return false;
        }

        LocalDateTime createdAt = item.blog().getCreatedAt();
        int createdAtComparison = compareCreatedAtDescPosition(createdAt, cursor.afterCreatedAt());
        if (createdAtComparison > 0) {
            return true;
        }
        if (createdAtComparison < 0) {
            return false;
        }

        UUID blogId = item.blog().getId();
        return blogId != null && cursor.afterId() != null && blogId.compareTo(cursor.afterId()) < 0;
    }

    private int compareCreatedAtDescPosition(LocalDateTime createdAt, LocalDateTime afterCreatedAt) {
        if (createdAt == null && afterCreatedAt == null) {
            return 0;
        }
        if (createdAt == null) {
            return 1;
        }
        if (afterCreatedAt == null) {
            return -1;
        }
        if (createdAt.isBefore(afterCreatedAt)) {
            return 1;
        }
        if (createdAt.isAfter(afterCreatedAt)) {
            return -1;
        }
        return 0;
    }

    private List<BlogFeedResponse> toOrganicFeedResponses(List<ScoredOrganicBlog> scoredBlogs) {
        List<BlogRecommendationScore> recommendationScores = java.util.stream.IntStream.range(0, scoredBlogs.size())
                .mapToObj(index -> {
                    ScoredOrganicBlog item = scoredBlogs.get(index);
                    BlogRecommendationScore score = new BlogRecommendationScore();
                    score.setBlog(item.blog());
                    score.setRankPosition(index + 1);
                    return score;
                })
                .toList();
        return toFeedResponses(recommendationScores);
    }

    private FeedResponseDTO toFeedResponse(BlogFeedCursorPageResponseDTO organicPage) {
        FeedResponseDTO response = new FeedResponseDTO();
        response.setItems(toFeedItems(organicPage.getItems()));
        response.setNextCursor(organicPage.getNextCursor());
        response.setHasMore(organicPage.getHasMore());
        return response;
    }

    private List<FeedItemResponseDTO> toFeedItems(List<BlogFeedResponse> blogs) {
        return java.util.stream.IntStream.range(0, blogs.size())
                .mapToObj(index -> toFeedItem(blogs.get(index), index))
                .toList();
    }

    private FeedItemResponseDTO toFeedItem(BlogFeedResponse blog, int index) {
        FeedItemResponseDTO item = new FeedItemResponseDTO();
        item.setItemType(blog.getPageId() == null ? FeedItemType.USER_BLOG : FeedItemType.CAFE_PAGE_BLOG);
        item.setBlog(blog);
        item.setAd(null);
        item.setPosition(index);
        item.setTrackingToken(null);
        return item;
    }

    private OrganicFeedCursor decodeOrganicCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            OrganicFeedCursorPayload payload = CURSOR_OBJECT_MAPPER.readValue(json, OrganicFeedCursorPayload.class);
            if (payload.version() != ORGANIC_CURSOR_VERSION
                    || payload.afterScore() == null
                    || payload.afterCreatedAt() == null
                    || payload.afterId() == null
                    || payload.scoredAt() == null) {
                throw invalidOrganicCursor();
            }
            return new OrganicFeedCursor(
                    payload.afterScore(),
                    LocalDateTime.parse(payload.afterCreatedAt()),
                    payload.afterId(),
                    LocalDateTime.parse(payload.scoredAt()));
        } catch (IllegalArgumentException | JsonProcessingException | DateTimeParseException error) {
            throw invalidOrganicCursor();
        }
    }

    private String encodeOrganicCursor(ScoredOrganicBlog item, LocalDateTime scoredAt) {
        try {
            OrganicFeedCursorPayload payload = new OrganicFeedCursorPayload(
                    item.score(),
                    item.blog().getCreatedAt().toString(),
                    item.blog().getId(),
                    scoredAt.toString(),
                    ORGANIC_CURSOR_VERSION);
            String json = CURSOR_OBJECT_MAPPER.writeValueAsString(payload);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create organic feed cursor");
        }
    }

    private ResponseStatusException invalidOrganicCursor() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid organic feed cursor");
    }

    private record ScoredOrganicBlog(Blog blog, double score) {
    }

    private record OrganicFeedCursor(
            double afterScore,
            LocalDateTime afterCreatedAt,
            UUID afterId,
            LocalDateTime scoredAt) {
    }

    private record OrganicFeedCursorPayload(
            Double afterScore,
            String afterCreatedAt,
            UUID afterId,
            String scoredAt,
            int version) {
    }
}
