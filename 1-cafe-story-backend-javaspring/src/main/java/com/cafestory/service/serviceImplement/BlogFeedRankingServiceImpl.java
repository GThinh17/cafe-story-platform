package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.config.FeedRebuildExecutorConfig;
import com.cafestory.dto.responseDTO.BlogDisplayAuthorType;
import com.cafestory.dto.responseDTO.BlogFeedCursorPageResponseDTO;
import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.dto.responseDTO.FeedItemResponseDTO;
import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRecommendationScore;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.FeedItemType;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRecommendationScoreRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.repository.FeedImpressionRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.service.serviceInterface.BlogRecommendationScoreBatchWriter;
import com.cafestory.service.serviceInterface.FeedScoreCalculationService;
import com.cafestory.validation.UserValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BlogFeedRankingServiceImpl implements BlogFeedRankingService {
    private static final Logger log = LoggerFactory.getLogger(BlogFeedRankingServiceImpl.class);
    private static final int DEFAULT_ORGANIC_FEED_SIZE = 20;
    private static final int MAX_ORGANIC_FEED_SIZE = 50;
    private static final int ORGANIC_CURSOR_VERSION = 1;
    private static final int PERSONALIZED_CURSOR_VERSION = 1;
    private static final LocalDateTime REPORT_EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final Set<ReportStatus> ACTIVE_REPORT_STATUSES = Set.of(ReportStatus.OPEN, ReportStatus.REVIEWING);
    private static final ObjectMapper CURSOR_OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
    private final ConcurrentMap<RecommendationCacheKey, Object> recommendationRebuildLocks = new ConcurrentHashMap<>();
    private final Set<RecommendationCacheKey> recommendationRebuildsInFlight = ConcurrentHashMap.newKeySet();

    private final BlogRepository blogRepository;
    private final BlogRecommendationScoreRepository blogRecommendationScoreRepository;
    private final CafePageRepository cafePageRepository;
    private final PageFollowRepository pageFollowRepository;
    private final UserFollowRepository userFollowRepository;
    private final BlogEventRepository blogEventRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final CommentRepository commentRepository;
    private final BlogShareRepository blogShareRepository;
    private final BlogSaveRepository blogSaveRepository;
    private final FeedImpressionRepository feedImpressionRepository;
    private final AiModerationResultRepository aiModerationResultRepository;
    private final ContentReportRepository contentReportRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final CacheManager cacheManager;
    private final FeedScoreCalculationService feedScoreCalculationService;
    private final BlogRecommendationScoreBatchWriter recommendationScoreBatchWriter;
    private final TaskExecutor taskExecutor;
    private final TransactionTemplate transactionTemplate;

    public BlogFeedRankingServiceImpl(
            BlogRepository blogRepository,
            BlogRecommendationScoreRepository blogRecommendationScoreRepository,
            CafePageRepository cafePageRepository,
            PageFollowRepository pageFollowRepository,
            UserFollowRepository userFollowRepository,
            BlogEventRepository blogEventRepository,
            BlogLikeRepository blogLikeRepository,
            CommentRepository commentRepository,
            BlogShareRepository blogShareRepository,
            BlogSaveRepository blogSaveRepository,
            FeedImpressionRepository feedImpressionRepository,
            AiModerationResultRepository aiModerationResultRepository,
            ContentReportRepository contentReportRepository,
            RegionRepository regionRepository,
            UserRepository userRepository,
            UserValidator userValidator,
            CacheManager cacheManager,
            FeedScoreCalculationService feedScoreCalculationService,
            BlogRecommendationScoreBatchWriter recommendationScoreBatchWriter,
            @Qualifier(FeedRebuildExecutorConfig.FEED_REBUILD_EXECUTOR) TaskExecutor taskExecutor,
            PlatformTransactionManager transactionManager) {
        this.blogRepository = blogRepository;
        this.blogRecommendationScoreRepository = blogRecommendationScoreRepository;
        this.cafePageRepository = cafePageRepository;
        this.pageFollowRepository = pageFollowRepository;
        this.userFollowRepository = userFollowRepository;
        this.blogEventRepository = blogEventRepository;
        this.blogLikeRepository = blogLikeRepository;
        this.commentRepository = commentRepository;
        this.blogShareRepository = blogShareRepository;
        this.blogSaveRepository = blogSaveRepository;
        this.feedImpressionRepository = feedImpressionRepository;
        this.aiModerationResultRepository = aiModerationResultRepository;
        this.contentReportRepository = contentReportRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.cacheManager = cacheManager;
        this.feedScoreCalculationService = feedScoreCalculationService;
        this.recommendationScoreBatchWriter = recommendationScoreBatchWriter;
        this.taskExecutor = taskExecutor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedResponseDTO getOrganicFeed(String cursor, int size) {
        int safeSize = normalizeOrganicFeedSize(size);
        OrganicFeedCursor organicCursor = decodeOrganicCursor(cursor);
        LocalDateTime scoredAt = organicCursor == null ? LocalDateTime.now() : organicCursor.scoredAt();
        String cacheKey = organicRankingCacheKey(cursor, safeSize);
        OrganicFeedRankingPage rankingPage = getCachedOrganicRankingPage(cacheKey);

        if (rankingPage == null) {
            rankingPage = buildOrganicRankingPage(organicCursor, safeSize, scoredAt);
            putCachedOrganicRankingPage(cacheKey, rankingPage);
            log.debug("Organic feed ranking cache miss key={}", cacheKey);
        } else {
            log.debug("Organic feed ranking cache hit key={}", cacheKey);
        }

        BlogFeedCursorPageResponseDTO response = new BlogFeedCursorPageResponseDTO();
        response.setItems(toOrganicFeedResponses(rankingPage.items()));
        response.setHasMore(rankingPage.hasMore());
        response.setNextCursor(rankingPage.nextCursor());
        return toFeedResponse(response);
    }

    private OrganicFeedRankingPage buildOrganicRankingPage(
            OrganicFeedCursor organicCursor,
            int safeSize,
            LocalDateTime scoredAt) {
        List<Blog> publishedBlogs = blogRepository.findByStatus(PostStatus.PUBLISHED);
        List<UUID> blogIds = publishedBlogs.stream()
                .map(Blog::getId)
                .filter(blogId -> blogId != null)
                .distinct()
                .toList();
        Set<UUID> violationBlogIds = violationBlogIds(blogIds);
        Map<UUID, Long> reportCountsByBlogId = reportCountsByBlogId(blogIds, REPORT_EPOCH, scoredAt);

        List<ScoredOrganicBlog> scoredBlogs = publishedBlogs
                .stream()
                .filter(blog -> !violationBlogIds.contains(blog.getId()))
                .map(blog -> new ScoredOrganicBlog(blog, calculateOrganicScore(blog, scoredAt, reportCountsByBlogId)))
                .sorted(organicFeedComparator())
                .filter(item -> isAfterOrganicCursor(item, organicCursor))
                .limit(safeSize + 1L)
                .toList();

        boolean hasMore = scoredBlogs.size() > safeSize;
        List<ScoredOrganicBlog> pageItems = hasMore ? scoredBlogs.subList(0, safeSize) : scoredBlogs;
        String nextCursor = hasMore && !pageItems.isEmpty()
                ? encodeOrganicCursor(pageItems.getLast(), scoredAt)
                : null;
        List<OrganicFeedRankingItem> items = java.util.stream.IntStream.range(0, pageItems.size())
                .mapToObj(index -> new OrganicFeedRankingItem(
                        pageItems.get(index).blog().getId(),
                        index + 1,
                        scoredAt))
                .toList();

        return new OrganicFeedRankingPage(scoredAt, items, hasMore, nextCursor);
    }

    private String organicRankingCacheKey(String cursor, int safeSize) {
        return "ranking:" + (cursor == null || cursor.isBlank() ? "first" : cursor) + ":" + safeSize;
    }

    private OrganicFeedRankingPage getCachedOrganicRankingPage(String cacheKey) {
        Cache cache = cacheManager.getCache(CacheConfig.ORGANIC_FEED_CACHE);
        if (cache == null) {
            return null;
        }

        Cache.ValueWrapper wrapper = cache.get(cacheKey);
        if (wrapper == null || !(wrapper.get() instanceof OrganicFeedRankingPage rankingPage)) {
            return null;
        }

        return rankingPage;
    }

    private void putCachedOrganicRankingPage(String cacheKey, OrganicFeedRankingPage rankingPage) {
        Cache cache = cacheManager.getCache(CacheConfig.ORGANIC_FEED_CACHE);
        if (cache != null) {
            cache.put(cacheKey, rankingPage);
        }
    }

    @Override
    @Transactional
    public FeedResponseDTO getPersonalizedFeedPage(
            UUID userId,
            TrendWindowType windowType,
            UUID regionId,
            String cursor,
            int size) {
        int safeSize = normalizeOrganicFeedSize(size);
        PersonalizedFeedCursor personalizedCursor = decodePersonalizedCursor(cursor);
        int page = personalizedCursor == null ? 0 : personalizedCursor.nextPage();
        List<BlogFeedResponse> blogs = getPersonalizedFeed(userId, windowType, regionId, page, safeSize);

        FeedResponseDTO response = new FeedResponseDTO();
        response.setItems(toFeedItems(blogs));
        response.setHasMore(blogs.size() == safeSize);
        response.setNextCursor(Boolean.TRUE.equals(response.getHasMore())
                ? encodePersonalizedCursor(page + 1)
                : null);
        return response;
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
        String cacheKey = personalizedRankingCacheKey(userId, windowType, contextRegionId, safePage, safeSize);
        List<BlogFeedResponse> cachedResponses = getCachedPersonalizedFeed(cacheKey);
        if (cachedResponses != null) {
            return cachedResponses;
        }

        LocalDateTime latestComputedAt = blogRecommendationScoreRepository.findLatestComputedAt(
                userId,
                windowType,
                contextRegionId);
        boolean stale = latestComputedAt == null
                || isPersonalizedCacheStale(userId, windowType, contextRegionId, latestComputedAt);
        if (stale) {
            scheduleRecommendationRebuild(userId, windowType, contextRegionId);
            if (latestComputedAt == null) {
                return fallbackOrganicFeedResponses(safeSize);
            }
        }

        List<BlogRecommendationScore> scores = blogRecommendationScoreRepository.findLatestPage(
                        userId,
                        windowType,
                        contextRegionId,
                        latestComputedAt,
                        PageRequest.of(safePage, safeSize));
        List<BlogFeedResponse> responses = toFeedResponses(scores, userId);
        if (!stale) {
            putCachedPersonalizedFeed(cacheKey, responses);
        }
        return responses;
    }

    @Override
    @Transactional
    public List<BlogFeedResponse> rebuildRecommendationCache(
            UUID userId,
            TrendWindowType windowType,
            UUID regionId) {
        User user = validateActiveUser(userId);
        UUID contextRegionId = resolveContextRegionId(user, regionId);
        RecommendationCacheKey cacheKey = new RecommendationCacheKey(userId, windowType, contextRegionId);
        Object lock = recommendationRebuildLocks.computeIfAbsent(cacheKey, ignored -> new Object());

        synchronized (lock) {
            return rebuildRecommendationCacheLocked(user, windowType, contextRegionId);
        }
    }

    private List<BlogFeedResponse> rebuildRecommendationCacheLocked(
            User user,
            TrendWindowType windowType,
            UUID contextRegionId) {
        List<BlogRecommendationScore> scores = buildRecommendationScores(user, windowType, contextRegionId);

        upsertRecommendationScores(scores, LocalDateTime.now());
        return toFeedResponses(scores, user.getUserId());
    }

    private void scheduleRecommendationRebuild(
            UUID userId,
            TrendWindowType windowType,
            UUID contextRegionId) {
        RecommendationCacheKey cacheKey = new RecommendationCacheKey(userId, windowType, contextRegionId);
        if (!recommendationRebuildsInFlight.add(cacheKey)) {
            return;
        }

        try {
            taskExecutor.execute(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        User currentUser = validateActiveUser(userId);
                        rebuildRecommendationCacheLocked(currentUser, windowType, contextRegionId);
                    });
                } catch (RuntimeException error) {
                    log.warn("Background personalized feed rebuild failed userId={} windowType={} contextRegionId={}",
                            userId,
                            windowType,
                            contextRegionId,
                            error);
                } finally {
                    recommendationRebuildsInFlight.remove(cacheKey);
                }
            });
        } catch (RuntimeException error) {
            recommendationRebuildsInFlight.remove(cacheKey);
            log.warn("Unable to schedule personalized feed rebuild userId={} windowType={} contextRegionId={}",
                    userId,
                    windowType,
                    contextRegionId,
                    error);
        }
    }

    private String personalizedRankingCacheKey(
            UUID userId,
            TrendWindowType windowType,
            UUID contextRegionId,
            int page,
            int size) {
        return FeedScoreCalculationService.FORMULA_VERSION
                + ":" + userId
                + ":" + windowType.name()
                + ":" + (contextRegionId == null ? "none" : contextRegionId)
                + ":" + page
                + ":" + size;
    }

    private List<BlogFeedResponse> getCachedPersonalizedFeed(String cacheKey) {
        Cache cache = cacheManager.getCache(CacheConfig.PERSONALIZED_FEED_RANKING_CACHE);
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(cacheKey);
        if (wrapper == null || !(wrapper.get() instanceof String cachedJson)) {
            return null;
        }
        try {
            return CURSOR_OBJECT_MAPPER.readValue(
                    cachedJson,
                    CURSOR_OBJECT_MAPPER.getTypeFactory()
                            .constructCollectionType(List.class, BlogFeedResponse.class));
        } catch (JsonProcessingException exception) {
            log.warn("Personalized feed cache payload is invalid key={}: {}", cacheKey, exception.getMessage());
            return null;
        }
    }

    private void putCachedPersonalizedFeed(String cacheKey, List<BlogFeedResponse> responses) {
        Cache cache = cacheManager.getCache(CacheConfig.PERSONALIZED_FEED_RANKING_CACHE);
        if (cache == null) {
            return;
        }
        try {
            cache.put(cacheKey, CURSOR_OBJECT_MAPPER.writeValueAsString(responses));
        } catch (JsonProcessingException exception) {
            log.warn("Unable to serialize personalized feed cache key={}: {}", cacheKey, exception.getMessage());
        }
    }

    private List<BlogRecommendationScore> buildRecommendationScores(
            User user,
            TrendWindowType windowType,
            UUID contextRegionId) {
        LocalDateTime now = LocalDateTime.now();
        List<Blog> publishedBlogs = blogRepository.findByStatus(PostStatus.PUBLISHED);
        FeedScoringContext scoringContext = buildFeedScoringContext(user, contextRegionId, windowType, now, publishedBlogs);

        List<BlogRecommendationScore> candidates = publishedBlogs
                .stream()
                .filter(blog -> !scoringContext.violationBlogIds().contains(blog.getId()))
                .map(blog -> createRecommendationScore(
                        user,
                        blog,
                        windowType,
                        contextRegionId,
                        scoringContext,
                        now))
                .toList();
        return selectWithDiversity(candidates);
    }

    private List<BlogRecommendationScore> pageScores(List<BlogRecommendationScore> scores, int page, int size) {
        if (scores.isEmpty()) {
            return List.of();
        }

        int fromIndex = Math.min(page * size, scores.size());
        int toIndex = Math.min(fromIndex + size, scores.size());
        return scores.subList(fromIndex, toIndex);
    }

    private List<BlogFeedResponse> pageFeedResponses(List<BlogFeedResponse> responses, int page, int size) {
        if (responses.isEmpty()) {
            return List.of();
        }

        int fromIndex = Math.min(page * size, responses.size());
        int toIndex = Math.min(fromIndex + size, responses.size());
        return responses.subList(fromIndex, toIndex);
    }

    private List<BlogFeedResponse> fallbackOrganicFeedResponses(int size) {
        return getOrganicFeed(null, size)
                .getItems()
                .stream()
                .filter(item -> item.getBlog() != null)
                .map(FeedItemResponseDTO::getBlog)
                .toList();
    }

    private FeedScoringContext buildFeedScoringContext(
            User user,
            UUID contextRegionId,
            TrendWindowType windowType,
            LocalDateTime now,
            List<Blog> blogs) {
        List<UUID> blogIds = blogs.stream()
                .map(Blog::getId)
                .filter(blogId -> blogId != null)
                .distinct()
                .toList();
        List<UUID> pageIds = blogs.stream()
                .map(Blog::getPageId)
                .filter(pageId -> pageId != null)
                .distinct()
                .toList();
        List<UUID> authorIds = blogs.stream()
                .map(Blog::getAuthor)
                .filter(author -> author != null && author.getUserId() != null)
                .map(User::getUserId)
                .distinct()
                .toList();
        List<UUID> regionIds = blogs.stream()
                .map(Blog::getRegionId)
                .filter(regionId -> regionId != null)
                .distinct()
                .toList();

        LocalDateTime interactionStartAt = now.minusDays(30);
        LocalDateTime seenAfter = now.minusDays(7);
        Set<UUID> recentlyInteractedBlogIds = recentlyInteractedBlogIds(
                user.getUserId(), blogIds, interactionStartAt, now);
        Map<UUID, Set<String>> tagsByBlogId = tagsByBlogId(blogIds);
        Set<String> userInterestTags = recentlyInteractedBlogIds.stream()
                .flatMap(blogId -> tagsByBlogId.getOrDefault(blogId, Set.of()).stream())
                .collect(Collectors.toUnmodifiableSet());
        Set<UUID> positivelyInteractedSourceKeys = blogs.stream()
                .filter(blog -> recentlyInteractedBlogIds.contains(blog.getId()))
                .map(this::sourceKey)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        return new FeedScoringContext(
                user.getUserId(),
                resolveContextCity(user, contextRegionId),
                violationBlogIds(blogIds),
                followedPageIds(user.getUserId(), pageIds),
                followedUserIds(user.getUserId(), authorIds),
                cityByRegionId(regionIds),
                activeReportCountsByBlogId(blogIds),
                activityCountsByBlogId(blogIds, interactionStartAt, now),
                seenBlogIds(user.getUserId(), blogIds, seenAfter),
                positivelyInteractedSourceKeys,
                tagsByBlogId,
                userInterestTags);
    }

    private Set<UUID> violationBlogIds(List<UUID> blogIds) {
        if (blogIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(aiModerationResultRepository.findBlogIdsByBlogIdInAndDecision(
                blogIds,
                ModerationDecision.VIOLATION));
    }

    private Set<UUID> followedPageIds(UUID userId, List<UUID> pageIds) {
        if (pageIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(pageFollowRepository.findFollowedCafePageIds(userId, pageIds));
    }

    private Set<UUID> followedUserIds(UUID userId, List<UUID> authorIds) {
        if (authorIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(userFollowRepository.findFollowedUserIds(userId, authorIds));
    }

    private Set<UUID> recentlyInteractedBlogIds(
            UUID userId,
            List<UUID> blogIds,
            LocalDateTime startAt,
            LocalDateTime endAt) {
        if (blogIds.isEmpty()) {
            return Set.of();
        }
        Set<UUID> interactedBlogIds = new HashSet<>();
        interactedBlogIds.addAll(blogLikeRepository.findRecentlyLikedBlogIds(userId, blogIds, startAt, endAt));
        interactedBlogIds.addAll(commentRepository.findRecentlyCommentedBlogIds(userId, blogIds, startAt, endAt));
        interactedBlogIds.addAll(blogShareRepository.findRecentlySharedBlogIds(userId, blogIds, startAt, endAt));
        interactedBlogIds.addAll(blogSaveRepository.findRecentlySavedBlogIds(userId, blogIds, startAt, endAt));
        return Set.copyOf(interactedBlogIds);
    }

    private Map<UUID, Set<String>> tagsByBlogId(List<UUID> blogIds) {
        if (blogIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Set<String>> tagsByBlogId = new HashMap<>();
        for (AiModerationResult result : aiModerationResultRepository.findWithTagsByBlogIds(blogIds)) {
            if (result.getBlog() == null || result.getBlog().getId() == null
                    || tagsByBlogId.containsKey(result.getBlog().getId())) {
                continue;
            }
            Set<String> tags = result.getTags() == null
                    ? Set.of()
                    : result.getTags().stream()
                    .filter(tag -> tag != null && !tag.isBlank())
                    .collect(Collectors.toUnmodifiableSet());
            tagsByBlogId.put(result.getBlog().getId(), tags);
        }
        return Map.copyOf(tagsByBlogId);
    }

    private Map<UUID, Long> activeReportCountsByBlogId(List<UUID> blogIds) {
        if (blogIds.isEmpty()) {
            return Map.of();
        }
        return contentReportRepository.countByBlogIdsAndStatusIn(blogIds, ACTIVE_REPORT_STATUSES)
                .stream()
                .collect(Collectors.toMap(
                        ContentReportRepository.ReportCountRow::getTargetId,
                        ContentReportRepository.ReportCountRow::getReportCount));
    }

    private Map<UUID, String> cityByRegionId(List<UUID> regionIds) {
        if (regionIds.isEmpty()) {
            return Map.of();
        }
        return regionRepository.findAllById(regionIds)
                .stream()
                .filter(region -> region.getRegionId() != null && region.getCity() != null)
                .collect(Collectors.toMap(Region::getRegionId, Region::getCity));
    }

    private Map<UUID, Long> reportCountsByBlogId(List<UUID> blogIds, LocalDateTime startAt, LocalDateTime endAt) {
        if (blogIds.isEmpty()) {
            return Map.of();
        }
        return blogEventRepository.countByBlogIdsAndEventTypeAndCreatedAtBetween(
                        blogIds,
                        BlogEventType.REPORT,
                        startAt,
                        endAt)
                .stream()
                .collect(Collectors.toMap(
                        BlogEventRepository.BlogEventCountRow::getBlogId,
                        row -> row.getEventCount() == null ? 0L : row.getEventCount()));
    }

    private Map<UUID, ActivityCounts> activityCountsByBlogId(List<UUID> blogIds, LocalDateTime startAt, LocalDateTime endAt) {
        if (blogIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Long> views = eventCountsByBlogId(blogIds, BlogEventType.VIEW, startAt, endAt);
        Map<UUID, Long> likes = blogLikeRepository.countByBlogIdsAndCreatedAtBetween(blogIds, startAt, endAt)
                .stream()
                .collect(Collectors.toMap(
                        BlogLikeRepository.BlogInteractionCountRow::getBlogId,
                        row -> row.getEventCount() == null ? 0L : row.getEventCount()));
        Map<UUID, Long> comments = commentRepository.countRootCommentsByBlogIdsAndCreatedAtBetween(blogIds, startAt, endAt)
                .stream()
                .collect(Collectors.toMap(
                        CommentRepository.CommentCountRow::getBlogId,
                        row -> row.getEventCount() == null ? 0L : row.getEventCount()));
        Map<UUID, Long> replies = commentRepository.countRepliesByBlogIdsAndCreatedAtBetween(blogIds, startAt, endAt)
                .stream()
                .collect(Collectors.toMap(
                        CommentRepository.CommentCountRow::getBlogId,
                        row -> row.getEventCount() == null ? 0L : row.getEventCount()));
        Map<UUID, Long> shares = blogShareRepository.countByBlogIdsAndCreatedAtBetween(blogIds, startAt, endAt)
                .stream()
                .collect(Collectors.toMap(
                        BlogShareRepository.BlogShareCountRow::getBlogId,
                        row -> row.getEventCount() == null ? 0L : row.getEventCount()));
        Map<UUID, Long> saves = blogSaveRepository.countByBlogIdsAndCreatedAtBetween(blogIds, startAt, endAt)
                .stream()
                .collect(Collectors.toMap(
                        BlogSaveRepository.BlogSaveCountRow::getBlogId,
                        row -> row.getEventCount() == null ? 0L : row.getEventCount()));

        return blogIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        blogId -> new ActivityCounts(
                                views.getOrDefault(blogId, 0L),
                                likes.getOrDefault(blogId, 0L),
                                comments.getOrDefault(blogId, 0L),
                                replies.getOrDefault(blogId, 0L),
                                shares.getOrDefault(blogId, 0L),
                                saves.getOrDefault(blogId, 0L))));
    }

    private Map<UUID, Long> eventCountsByBlogId(
            List<UUID> blogIds,
            BlogEventType eventType,
            LocalDateTime startAt,
            LocalDateTime endAt) {
        return blogEventRepository.countByBlogIdsAndEventTypeAndCreatedAtBetween(blogIds, eventType, startAt, endAt)
                .stream()
                .collect(Collectors.toMap(
                        BlogEventRepository.BlogEventCountRow::getBlogId,
                        row -> row.getEventCount() == null ? 0L : row.getEventCount()));
    }

    private Set<UUID> seenBlogIds(UUID userId, List<UUID> blogIds, LocalDateTime shownAfter) {
        if (blogIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(feedImpressionRepository.findSeenBlogIds(userId, blogIds, shownAfter));
    }

    private void upsertRecommendationScores(List<BlogRecommendationScore> scores, LocalDateTime createdAt) {
        recommendationScoreBatchWriter.upsertAll(scores, createdAt);
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

    private boolean isPersonalizedCacheStale(
            UUID userId,
            TrendWindowType windowType,
            UUID contextRegionId,
            LocalDateTime latestComputedAt) {
        boolean hasNewOwnPost = blogRepository
                .findFirstByAuthorUserIdAndStatusOrderByCreatedAtDescIdDesc(userId, PostStatus.PUBLISHED)
                .map(blog -> blog.getCreatedAt() != null && blog.getCreatedAt().isAfter(latestComputedAt))
                .orElse(false);
        if (hasNewOwnPost) {
            return true;
        }
        List<String> formulaVersions = blogRecommendationScoreRepository.findFormulaVersionsAtComputedAt(
                userId,
                windowType,
                contextRegionId,
                latestComputedAt);
        return formulaVersions.size() != 1
                || !FeedScoreCalculationService.FORMULA_VERSION.equals(formulaVersions.getFirst());
    }

    private BlogRecommendationScore createRecommendationScore(
            User user,
            Blog blog,
            TrendWindowType windowType,
            UUID contextRegionId,
            FeedScoringContext scoringContext,
            LocalDateTime now) {
        UUID sourceKey = sourceKey(blog);
        UUID authorId = blog.getAuthor() == null ? null : blog.getAuthor().getUserId();
        boolean ownAuthor = user.getUserId().equals(authorId);
        boolean followedSource = blog.getPageId() != null
                ? scoringContext.followedPageIds().contains(blog.getPageId())
                : scoringContext.followedUserIds().contains(authorId);
        boolean positivelyInteracted = scoringContext.positivelyInteractedSourceKeys().contains(sourceKey);
        ActivityCounts counts = scoringContext.activityCountsByBlogId().get(blog.getId());
        if (counts == null) {
            counts = new ActivityCounts(0, 0, 0, 0, 0, 0);
        }

        double relationshipScore = feedScoreCalculationService.calculateRelationshipScore(
                ownAuthor, followedSource, positivelyInteracted);
        double interestScore = feedScoreCalculationService.calculateInterestScore(
                scoringContext.userInterestTags(),
                scoringContext.tagsByBlogId().getOrDefault(blog.getId(), Set.of()));
        double engagementScore = feedScoreCalculationService.calculateEngagementScore(
                counts.views(), counts.likes(), counts.comments(), counts.replies(), counts.shares(), counts.saves());
        double freshnessScore = feedScoreCalculationService.calculateFreshnessScore(blog.getCreatedAt(), now);
        double qualityScore = feedScoreCalculationService.calculateQualityScore(
                scoringContext.activeReportCountsByBlogId().getOrDefault(blog.getId(), 0L));
        double locationScore = feedScoreCalculationService.calculateLocationScore(
                scoringContext.contextCity(),
                scoringContext.cityByRegionId().get(blog.getRegionId()));
        double unseenScore = feedScoreCalculationService.calculateUnseenScore(
                scoringContext.seenBlogIds().contains(blog.getId()));
        FeedScoreCalculationService.ScoreComponents components = new FeedScoreCalculationService.ScoreComponents(
                relationshipScore,
                interestScore,
                engagementScore,
                freshnessScore,
                qualityScore,
                locationScore,
                0.0,
                unseenScore);

        BlogRecommendationScore score = new BlogRecommendationScore();
        score.setUser(user);
        score.setBlog(blog);
        score.setWindowType(windowType);
        score.setContextRegionId(contextRegionId);
        score.setFeedScore(feedScoreCalculationService.calculateStaticScore(components));
        score.setFormulaVersion(FeedScoreCalculationService.FORMULA_VERSION);
        score.setRelationshipScore(relationshipScore);
        score.setInterestScore(interestScore);
        score.setEngagementScore(engagementScore);
        score.setQualityScore(qualityScore);
        score.setLocationScore(locationScore);
        score.setDiversityScore(0.0);
        score.setUnseenScore(unseenScore);
        score.setTrendingScore(0.0);
        score.setFollowedPageScore(0.0);
        score.setFollowedUserScore(0.0);
        score.setSameRegionScore(0.0);
        score.setFreshnessScore(freshnessScore);
        score.setActivityScore(0.0);
        score.setOwnAuthorScore(0.0);
        score.setReviewerScore(0.0);
        score.setReportPenalty(0.0);
        score.setSeenPenalty(0.0);
        score.setRepetitionPenalty(0.0);
        score.setRankPosition(0);
        score.setReason(buildExpertReason(score));
        score.setComputedAt(now);
        return score;
    }

    private List<BlogRecommendationScore> selectWithDiversity(List<BlogRecommendationScore> candidates) {
        List<BlogRecommendationScore> remaining = new ArrayList<>(candidates);
        List<BlogRecommendationScore> selected = new ArrayList<>(candidates.size());
        List<UUID> selectedSourceKeys = new ArrayList<>();
        Comparator<BlogRecommendationScore> comparator = Comparator
                .comparingDouble(BlogRecommendationScore::getFeedScore)
                .thenComparing(
                        score -> score.getBlog().getCreatedAt(),
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(
                        score -> score.getBlog().getId(),
                        Comparator.nullsFirst(Comparator.naturalOrder()));

        while (!remaining.isEmpty()) {
            for (BlogRecommendationScore candidate : remaining) {
                double diversityScore = feedScoreCalculationService.calculateDiversityScore(
                        sourceKey(candidate.getBlog()), selectedSourceKeys);
                candidate.setDiversityScore(diversityScore);
                candidate.setFeedScore(feedScoreCalculationService.calculateFinalScore(toComponents(candidate)));
            }
            BlogRecommendationScore winner = remaining.stream().max(comparator).orElseThrow();
            remaining.remove(winner);
            winner.setRankPosition(selected.size() + 1);
            winner.setReason(buildExpertReason(winner));
            selected.add(winner);
            selectedSourceKeys.add(sourceKey(winner.getBlog()));
        }
        return List.copyOf(selected);
    }

    private FeedScoreCalculationService.ScoreComponents toComponents(BlogRecommendationScore score) {
        return new FeedScoreCalculationService.ScoreComponents(
                score.getRelationshipScore(),
                score.getInterestScore(),
                score.getEngagementScore(),
                score.getFreshnessScore(),
                score.getQualityScore(),
                score.getLocationScore(),
                score.getDiversityScore(),
                score.getUnseenScore());
    }

    private UUID sourceKey(Blog blog) {
        if (blog.getPageId() != null) {
            return blog.getPageId();
        }
        return blog.getAuthor() == null ? null : blog.getAuthor().getUserId();
    }

    private String buildExpertReason(BlogRecommendationScore score) {
        return "formulaVersion=" + FeedScoreCalculationService.FORMULA_VERSION
                + ", relationship=" + score.getRelationshipScore()
                + ", interest=" + score.getInterestScore()
                + ", engagement=" + score.getEngagementScore()
                + ", freshness=" + score.getFreshnessScore()
                + ", quality=" + score.getQualityScore()
                + ", location=" + score.getLocationScore()
                + ", diversity=" + score.getDiversityScore()
                + ", unseen=" + score.getUnseenScore()
                + ", feedScore=" + score.getFeedScore();
    }

    private List<BlogFeedResponse> toFeedResponses(List<BlogRecommendationScore> scores, UUID viewerUserId) {
        List<UUID> blogIds = scores.stream()
                .map(score -> score.getBlog().getId())
                .filter(blogId -> blogId != null)
                .distinct()
                .toList();
        Map<UUID, List<String>> imageUrlsByBlogId = imageUrlsByBlogId(blogIds);
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
        List<UUID> authorIds = scores.stream()
                .map(score -> score.getBlog().getAuthor())
                .filter(author -> author != null && author.getUserId() != null)
                .map(User::getUserId)
                .filter(authorId -> viewerUserId == null || !viewerUserId.equals(authorId))
                .distinct()
                .toList();
        Set<UUID> followedAuthorIds = viewerUserId == null
                ? Set.of()
                : followedUserIds(viewerUserId, authorIds);
        Set<UUID> followedCafePageIds = viewerUserId == null
                ? Set.of()
                : followedPageIds(viewerUserId, pageIds);

        return scores.stream()
                .map(score -> toFeedResponse(
                        score,
                        cafePagesById,
                        regionsById,
                        imageUrlsByBlogId,
                        followedAuthorIds,
                        followedCafePageIds))
                .toList();
    }

    private Map<UUID, List<String>> imageUrlsByBlogId(List<UUID> blogIds) {
        if (blogIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, List<String>> imageUrlsByBlogId = new HashMap<>();
        blogRepository.findImageUrlsByBlogIds(blogIds).forEach(row ->
                imageUrlsByBlogId.computeIfAbsent(row.getBlogId(), ignored -> new java.util.ArrayList<>())
                        .add(row.getImageUrl()));
        return imageUrlsByBlogId;
    }

    private BlogFeedResponse toFeedResponse(
            BlogRecommendationScore score,
            Map<UUID, CafePage> cafePagesById,
            Map<UUID, Region> regionsById,
            Map<UUID, List<String>> imageUrlsByBlogId,
            Set<UUID> followedAuthorIds,
            Set<UUID> followedCafePageIds) {
        Blog blog = score.getBlog();
        User author = blog.getAuthor();
        CafePage cafePage = blog.getPageId() == null ? null : cafePagesById.get(blog.getPageId());
        Region region = blog.getRegionId() == null ? null : regionsById.get(blog.getRegionId());
        BlogFeedResponse response = new BlogFeedResponse();
        response.setBlogId(blog.getId());
        response.setContentPreview(toPreview(blog.getContent()));
        response.setImageUrls(imageUrlsByBlogId.getOrDefault(blog.getId(), List.of()));
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
        UUID authorUserId = author.getUserId();
        response.setIsAuthorFollowing(authorUserId != null && followedAuthorIds.contains(authorUserId));
        UUID pageId = blog.getPageId();
        response.setIsPageFollowing(pageId != null && followedCafePageIds.contains(pageId));
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
        response.setDisplayName(firstNonBlank(author.getUserName(), author.getUserFullName()));
        response.setDisplayAvatarUrl(author.getUserAvatar());
    }

    private String firstNonBlank(String first, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        return fallback;
    }

    private String resolveContextCity(User user, UUID contextRegionId) {
        if (contextRegionId != null) {
            return regionRepository.findById(contextRegionId)
                    .map(Region::getCity)
                    .orElse(null);
        }
        return user.getRegion() == null ? null : user.getRegion().getCity();
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

    private double calculateOrganicScore(Blog blog, LocalDateTime scoredAt, Map<UUID, Long> reportCountsByBlogId) {
        double engagementScore = valueOrZero(blog.getLikeCount()) * 2.0
                + valueOrZero(blog.getCommentCount()) * 4.0
                + valueOrZero(blog.getShareCount()) * 5.0;
        double freshnessScore = calculateOrganicFreshnessScore(blog, scoredAt);
        double pageBonus = blog.getPageId() == null ? 0.0 : 5.0;
        double reportPenalty = reportCountsByBlogId.getOrDefault(blog.getId(), 0L) * 10.0;
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

    private List<BlogFeedResponse> toOrganicFeedResponses(List<OrganicFeedRankingItem> rankingItems) {
        if (rankingItems.isEmpty()) {
            return List.of();
        }

        List<UUID> blogIds = rankingItems.stream()
                .map(OrganicFeedRankingItem::blogId)
                .toList();
        Map<UUID, Blog> blogsById = blogRepository.findByIdIn(blogIds)
                .stream()
                .collect(Collectors.toMap(Blog::getId, Function.identity()));
        List<BlogRecommendationScore> recommendationScores = rankingItems.stream()
                .map(item -> {
                    Blog blog = blogsById.get(item.blogId());
                    if (blog == null) {
                        return null;
                    }

                    BlogRecommendationScore score = new BlogRecommendationScore();
                    score.setBlog(blog);
                    score.setRankPosition(item.rankPosition());
                    return score;
                })
                .filter(score -> score != null)
                .toList();
        return toFeedResponses(recommendationScores, null);
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

    private PersonalizedFeedCursor decodePersonalizedCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            PersonalizedFeedCursorPayload payload = CURSOR_OBJECT_MAPPER.readValue(json, PersonalizedFeedCursorPayload.class);
            if (payload.version() != PERSONALIZED_CURSOR_VERSION || payload.nextPage() == null || payload.nextPage() < 0) {
                throw invalidPersonalizedCursor();
            }
            return new PersonalizedFeedCursor(payload.nextPage());
        } catch (IllegalArgumentException | JsonProcessingException error) {
            throw invalidPersonalizedCursor();
        }
    }

    private String encodePersonalizedCursor(int nextPage) {
        try {
            PersonalizedFeedCursorPayload payload = new PersonalizedFeedCursorPayload(nextPage, PERSONALIZED_CURSOR_VERSION);
            String json = CURSOR_OBJECT_MAPPER.writeValueAsString(payload);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create personalized feed cursor");
        }
    }

    private ResponseStatusException invalidPersonalizedCursor() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid personalized feed cursor");
    }

    private record ScoredOrganicBlog(Blog blog, double score) {
    }

    private record OrganicFeedRankingPage(
            LocalDateTime computedAt,
            List<OrganicFeedRankingItem> items,
            boolean hasMore,
            String nextCursor) {
    }

    private record OrganicFeedRankingItem(
            UUID blogId,
            int rankPosition,
            LocalDateTime computedAt) {
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

    private record PersonalizedFeedCursor(int nextPage) {
    }

    private record PersonalizedFeedCursorPayload(
            Integer nextPage,
            int version) {
    }

    private record FeedScoringContext(
            UUID userId,
            String contextCity,
            Set<UUID> violationBlogIds,
            Set<UUID> followedPageIds,
            Set<UUID> followedUserIds,
            Map<UUID, String> cityByRegionId,
            Map<UUID, Long> activeReportCountsByBlogId,
            Map<UUID, ActivityCounts> activityCountsByBlogId,
            Set<UUID> seenBlogIds,
            Set<UUID> positivelyInteractedSourceKeys,
            Map<UUID, Set<String>> tagsByBlogId,
            Set<String> userInterestTags) {
    }

    private record ActivityCounts(
            long views,
            long likes,
            long comments,
            long replies,
            long shares,
            long saves) {
    }

    private record RecommendationCacheKey(
            UUID userId,
            TrendWindowType windowType,
            UUID contextRegionId) {
    }
}
