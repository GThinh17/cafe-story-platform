package com.cafestory.service;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.dto.responseDTO.BlogDisplayAuthorType;
import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.BlogRecommendationScore;
import com.cafestory.entity.BlogTrendingScore;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.FeedItemType;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRecommendationScoreRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.BlogTrendingScoreRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.repository.FeedImpressionRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.BlogFeedRankingServiceImpl;
import com.cafestory.service.serviceImplement.FeedScoreCalculationServiceImpl;
import com.cafestory.service.serviceInterface.BlogRecommendationScoreBatchWriter;
import com.cafestory.service.serviceInterface.FeedScoreCalculationService;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogFeedRankingServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private BlogTrendingScoreRepository blogTrendingScoreRepository;

    @Mock
    private BlogRecommendationScoreRepository blogRecommendationScoreRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private PageFollowRepository pageFollowRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private BlogEventRepository blogEventRepository;

    @Mock
    private BlogLikeRepository blogLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BlogShareRepository blogShareRepository;

    @Mock
    private BlogSaveRepository blogSaveRepository;

    @Mock
    private FeedImpressionRepository feedImpressionRepository;

    @Mock
    private AiModerationResultRepository aiModerationResultRepository;

    @Mock
    private ContentReportRepository contentReportRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private BlogRecommendationScoreBatchWriter recommendationScoreBatchWriter;

    @Mock
    private TaskExecutor taskExecutor;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Test
    void getOrganicFeed_success_rankingOrderByScore_TC001() {
        Blog lowScoreBlog = organicBlog(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                null,
                1,
                0,
                0,
                LocalDateTime.now().minusHours(12));
        Blog highScorePageBlog = organicBlog(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.randomUUID(),
                20,
                5,
                3,
                LocalDateTime.now().minusHours(12));
        BlogFeedRankingServiceImpl service = service();

        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(lowScoreBlog, highScorePageBlog));

        FeedResponseDTO result = service.getOrganicFeed(null, 20);

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().getFirst().getItemType()).isEqualTo(FeedItemType.CAFE_PAGE_BLOG);
        assertThat(result.getItems().getFirst().getBlog().getBlogId()).isEqualTo(highScorePageBlog.getId());
        assertThat(result.getItems().getFirst().getBlog().getDisplayAuthorType()).isEqualTo(BlogDisplayAuthorType.CAFE_PAGE);
        assertThat(result.getItems().getFirst().getBlog().getDisplayName()).isEqualTo("Cafe Story Roastery");
        assertThat(result.getItems().getFirst().getAd()).isNull();
        assertThat(result.getItems().getFirst().getPosition()).isZero();
        assertThat(result.getHasMore()).isFalse();
        assertThat(result.getNextCursor()).isNull();
    }

    @Test
    void getOrganicFeed_success_nextCursorDoesNotRepeatItems_TC002() {
        LocalDateTime createdAt = LocalDateTime.now().minusHours(4);
        Blog firstBlog = organicBlog(
                UUID.fromString("00000000-0000-0000-0000-000000000101"),
                UUID.randomUUID(),
                20,
                2,
                1,
                createdAt);
        Blog secondBlog = organicBlog(
                UUID.fromString("00000000-0000-0000-0000-000000000102"),
                null,
                1,
                0,
                0,
                createdAt.minusHours(1));
        BlogFeedRankingServiceImpl service = service();

        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(secondBlog, firstBlog));

        FeedResponseDTO firstPage = service.getOrganicFeed(null, 1);
        FeedResponseDTO secondPage =
                service.getOrganicFeed(firstPage.getNextCursor(), 1);

        assertThat(firstPage.getItems()).extracting(item -> item.getBlog().getBlogId())
                .containsExactly(firstBlog.getId());
        assertThat(firstPage.getHasMore()).isTrue();
        assertThat(firstPage.getNextCursor()).isNotBlank();
        assertThat(secondPage.getItems()).extracting(item -> item.getBlog().getBlogId())
                .containsExactly(secondBlog.getId());
        assertThat(secondPage.getHasMore()).isFalse();
    }

    @Test
    void getOrganicFeed_success_sameScoreTieBreakByCreatedAtThenId_TC003() {
        LocalDateTime sameCreatedAt = LocalDateTime.of(2026, 5, 28, 10, 0);
        Blog lowerIdBlog = organicBlog(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                null,
                2,
                1,
                0,
                sameCreatedAt);
        Blog higherIdBlog = organicBlog(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                null,
                2,
                1,
                0,
                sameCreatedAt);
        BlogFeedRankingServiceImpl service = service();

        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(lowerIdBlog, higherIdBlog));

        FeedResponseDTO result = service.getOrganicFeed(null, 20);

        assertThat(result.getItems()).extracting(item -> item.getBlog().getBlogId())
                .containsExactly(higherIdBlog.getId(), lowerIdBlog.getId());
        assertThat(result.getItems()).extracting(item -> item.getItemType())
                .containsExactly(FeedItemType.USER_BLOG, FeedItemType.USER_BLOG);
    }

    @Test
    void getOrganicFeed_fail_invalidCursor_TC004() {
        BlogFeedRankingServiceImpl service = service();
        String invalidCursor = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"version\":1}".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.getOrganicFeed(invalidCursor, 20))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .satisfies(error -> assertThat(((org.springframework.web.server.ResponseStatusException) error)
                        .getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST));
    }

    @Test
    void getPersonalizedFeed_success_readsLatestCachedScores_TC005() {
        User user = user();
        Blog blog = blog(user.getRegion().getRegionId());
        BlogRecommendationScore score = recommendationScore(user, blog, 91.0, 1);
        LocalDateTime computedAt = LocalDateTime.of(2026, 5, 19, 10, 0);
        BlogFeedRankingServiceImpl service = service();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRecommendationScoreRepository.findLatestComputedAt(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                user.getRegion().getRegionId())).thenReturn(computedAt);
        when(blogRecommendationScoreRepository.findLatestPage(
                eq(user.getUserId()),
                eq(TrendWindowType.HOUR_24),
                eq(user.getRegion().getRegionId()),
                eq(computedAt),
                any(Pageable.class))).thenReturn(List.of(score));

        List<BlogFeedResponse> result = service.getPersonalizedFeed(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                null,
                0,
                10);
        List<BlogFeedResponse> cachedResult = service.getPersonalizedFeed(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                null,
                0,
                10);

        assertThat(result).hasSize(1);
        assertThat(cachedResult).usingRecursiveComparison().isEqualTo(result);
        assertThat(result.getFirst().getRankPosition()).isEqualTo(1);
        assertThat(result.getFirst().getContentPreview()).isEqualTo(blog.getContent());
        assertThat(result.getFirst().getImageUrls()).containsExactly("/images/feed/cafe.jpg");
        assertThat(result.getFirst().getAuthorUserName()).isEqualTo("author");
        assertThat(result.getFirst().getAuthorUserFullName()).isEqualTo("Author Name");
        assertThat(result.getFirst().getAuthorAvatar()).isEqualTo("/images/default-avatar.svg");
        assertThat(result.getFirst().getAuthorUserAvatar()).isEqualTo("/images/default-avatar.svg");
        assertThat(result.getFirst().getDisplayAuthorType()).isEqualTo(BlogDisplayAuthorType.CAFE_PAGE);
        assertThat(result.getFirst().getDisplayName()).isEqualTo("Cafe Story Roastery");
        assertThat(result.getFirst().getDisplayAvatarUrl()).isEqualTo("/images/cafe-avatar.jpg");
        assertThat(result.getFirst().getLikeCount()).isEqualTo(12);
        assertThat(result.getFirst().getCommentCount()).isEqualTo(3);
        assertThat(result.getFirst().getShareCount()).isEqualTo(4);
        verify(blogRecommendationScoreRepository, times(1)).findLatestComputedAt(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                user.getRegion().getRegionId());
        verify(blogRecommendationScoreRepository, times(1)).findLatestPage(
                eq(user.getUserId()),
                eq(TrendWindowType.HOUR_24),
                eq(user.getRegion().getRegionId()),
                eq(computedAt),
                any(Pageable.class));
        verify(userValidator, times(2)).validateUserActive(user);
    }

    @Test
    void getPersonalizedFeed_success_batchesViewerStateWithoutPerItemQueries_TC010() {
        User user = user();
        Blog blog = blog(user.getRegion().getRegionId());
        BlogRecommendationScore score = recommendationScore(user, blog, 91.0, 1);
        LocalDateTime computedAt = LocalDateTime.of(2026, 5, 19, 10, 0);
        BlogFeedRankingServiceImpl service = service();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRecommendationScoreRepository.findLatestComputedAt(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                user.getRegion().getRegionId())).thenReturn(computedAt);
        when(blogRecommendationScoreRepository.findLatestPage(
                eq(user.getUserId()),
                eq(TrendWindowType.HOUR_24),
                eq(user.getRegion().getRegionId()),
                eq(computedAt),
                any(Pageable.class))).thenReturn(List.of(score));
        when(userFollowRepository.findFollowedUserIds(user.getUserId(), List.of(blog.getAuthor().getUserId())))
                .thenReturn(List.of(blog.getAuthor().getUserId()));
        when(pageFollowRepository.findFollowedCafePageIds(user.getUserId(), List.of(blog.getPageId())))
                .thenReturn(List.of());
        when(blogLikeRepository.findLikedBlogIdsByUserIdAndBlogIds(user.getUserId(), List.of(blog.getId())))
                .thenReturn(List.of(blog.getId()));
        when(blogSaveRepository.findSavedBlogIdsByUserIdAndBlogIds(user.getUserId(), List.of(blog.getId())))
                .thenReturn(List.of());

        List<BlogFeedResponse> result = service.getPersonalizedFeed(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                null,
                0,
                10);

        assertThat(result).hasSize(1);
        BlogFeedResponse response = result.getFirst();
        assertThat(response.getIsAuthorFollowing()).isTrue();
        assertThat(response.getIsPageFollowing()).isFalse();
        assertThat(response.getIsLike()).isTrue();
        assertThat(response.getIsSave()).isFalse();
        verify(userFollowRepository, never())
                .existsByFollowerUserIdAndFollowingUserId(any(UUID.class), any(UUID.class));
        verify(pageFollowRepository, never())
                .existsByUserUserIdAndCafePageId(any(UUID.class), any(UUID.class));
        verify(blogLikeRepository, never())
                .existsByUserUserIdAndBlogId(any(UUID.class), any(UUID.class));
    }

    @Test
    void rebuildRecommendationCache_success_scoresAndStoresCache_TC006() {
        User user = user();
        UUID regionId = user.getRegion().getRegionId();
        Region blogRegion = region("Ho Chi Minh");
        Blog blog = blog(blogRegion.getRegionId());
        BlogFeedRankingServiceImpl service = service();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(blog));
        when(regionRepository.findById(regionId)).thenReturn(java.util.Optional.of(user.getRegion()));
        when(pageFollowRepository.findFollowedCafePageIds(user.getUserId(), List.of(blog.getPageId())))
                .thenReturn(List.of(blog.getPageId()));
        when(userFollowRepository.findFollowedUserIds(user.getUserId(), List.of(blog.getAuthor().getUserId())))
                .thenReturn(List.of(blog.getAuthor().getUserId()));

        List<BlogFeedResponse> result = service.rebuildRecommendationCache(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                regionId);

        assertThat(result).hasSize(1);
        BlogFeedResponse response = result.getFirst();
        assertThat(response.getPageName()).isEqualTo("Cafe Story Roastery");
        assertThat(response.getPageAddress()).isEqualTo("123 Brew Street");
        assertThat(response.getPageCoverUrl()).isEqualTo("/images/cafe-cover.jpg");
        assertThat(response.getDisplayAuthorType()).isEqualTo(BlogDisplayAuthorType.CAFE_PAGE);
        assertThat(response.getDisplayName()).isEqualTo("Cafe Story Roastery");
        assertThat(response.getDisplayAvatarUrl()).isEqualTo("/images/cafe-avatar.jpg");
        assertThat(response.getRegionCity()).isEqualTo("Ho Chi Minh");

        assertThat(upsertArguments()).singleElement().satisfies(arguments -> {
            assertThat(arguments[1]).isEqualTo(user.getUserId());
            assertThat(arguments[2]).isEqualTo(blog.getId());
            assertThat((Double) arguments[5]).isBetween(0.0, 1.0);
            assertThat(arguments[6]).isEqualTo(FeedScoreCalculationService.FORMULA_VERSION);
            assertThat(arguments[7]).isEqualTo(0.8);
            assertThat(arguments[25]).isEqualTo(1);
        });
        verify(userFollowRepository, never()).existsByFollowerUserIdAndFollowingUserId(any(), any());
        verify(pageFollowRepository, never()).existsByUserUserIdAndCafePageId(any(), any());
    }

    @Test
    void rebuildRecommendationCache_success_prioritizesCurrentUsersOwnBlogs_TC007() {
        User user = user();
        Blog ownBlog = blogWithAuthor(user, user.getRegion().getRegionId());
        Blog otherBlog = blog(user.getRegion().getRegionId());
        BlogFeedRankingServiceImpl service = service();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(otherBlog, ownBlog));

        service.rebuildRecommendationCache(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                null);

        ArgumentCaptor<UUID> blogIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Double> relationshipCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(blogRecommendationScoreRepository, times(2)).upsertRecommendationScore(
                any(UUID.class),
                eq(user.getUserId()),
                blogIdCaptor.capture(),
                eq(TrendWindowType.HOUR_24.name()),
                eq(user.getRegion().getRegionId()),
                any(Double.class),
                eq(FeedScoreCalculationService.FORMULA_VERSION),
                relationshipCaptor.capture(),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Integer.class),
                reasonCaptor.capture(),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
        assertThat(blogIdCaptor.getAllValues().getFirst()).isEqualTo(ownBlog.getId());
        assertThat(reasonCaptor.getAllValues().getFirst()).contains("formulaVersion=EXPERT_V1", "relationship=1.0");
        assertThat(relationshipCaptor.getAllValues().getFirst()).isEqualTo(1.0);
        verify(userFollowRepository, never())
                .existsByFollowerUserIdAndFollowingUserId(user.getUserId(), user.getUserId());
    }

    @Test
    void getPersonalizedFeed_success_rebuildsCacheWhenCurrentUserHasNewerOwnBlog_TC008() {
        User user = user();
        UUID regionId = user.getRegion().getRegionId();
        Blog ownBlog = blogWithAuthor(user, regionId);
        LocalDateTime oldComputedAt = LocalDateTime.now().minusHours(2);
        BlogFeedRankingServiceImpl service = service();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRecommendationScoreRepository.findLatestComputedAt(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                regionId)).thenReturn(oldComputedAt);
        when(blogRepository.findFirstByAuthorUserIdAndStatusOrderByCreatedAtDescIdDesc(
                user.getUserId(),
                PostStatus.PUBLISHED)).thenReturn(Optional.of(ownBlog));
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(ownBlog));
        when(blogRecommendationScoreRepository.findLatestPage(
                eq(user.getUserId()),
                eq(TrendWindowType.HOUR_24),
                eq(regionId),
                eq(oldComputedAt),
                any(Pageable.class))).thenReturn(List.of(recommendationScore(user, ownBlog, 0.5, 1)));

        List<BlogFeedResponse> result = service.getPersonalizedFeed(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                null,
                0,
                10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBlogId()).isEqualTo(ownBlog.getId());
        verify(blogRecommendationScoreRepository).upsertRecommendationScore(
                any(UUID.class),
                eq(user.getUserId()),
                eq(ownBlog.getId()),
                eq(TrendWindowType.HOUR_24.name()),
                eq(regionId),
                any(Double.class),
                eq(FeedScoreCalculationService.FORMULA_VERSION),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Integer.class),
                any(String.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void rebuildRecommendationCache_success_reportPenaltyReducesScore_TC009() {
        User user = user();
        Blog blog = blog(UUID.randomUUID());
        BlogFeedRankingServiceImpl service = service();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(blog));
        when(contentReportRepository.countByBlogIdsAndStatusIn(any(), any()))
                .thenReturn(List.of(contentReportCountRow(blog.getId(), 4L)));

        service.rebuildRecommendationCache(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                null);

        ArgumentCaptor<Double> qualityScoreCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(blogRecommendationScoreRepository).upsertRecommendationScore(
                any(UUID.class),
                eq(user.getUserId()),
                eq(blog.getId()),
                eq(TrendWindowType.HOUR_24.name()),
                eq(user.getRegion().getRegionId()),
                any(Double.class),
                eq(FeedScoreCalculationService.FORMULA_VERSION),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                qualityScoreCaptor.capture(),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                any(Double.class),
                eq(1),
                reasonCaptor.capture(),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
        assertThat(qualityScoreCaptor.getValue()).isEqualTo(0.5);
        assertThat(reasonCaptor.getValue()).contains("quality=0.5");
    }

    @Test
    void rebuildRecommendationCache_success_hardFiltersAiViolation_TC010() {
        User user = user();
        Blog allowed = blog(user.getRegion().getRegionId());
        Blog violation = blog(user.getRegion().getRegionId());
        BlogFeedRankingServiceImpl service = service();
        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(violation, allowed));
        when(aiModerationResultRepository.findBlogIdsByBlogIdInAndDecision(any(), any()))
                .thenReturn(List.of(violation.getId()));

        List<BlogFeedResponse> result = service.rebuildRecommendationCache(
                user.getUserId(), TrendWindowType.HOUR_24, null);

        assertThat(result).extracting(BlogFeedResponse::getBlogId).containsExactly(allowed.getId());
        assertThat(upsertArguments()).singleElement().satisfies(arguments ->
                assertThat(arguments[2]).isEqualTo(allowed.getId()));
    }

    @Test
    void rebuildRecommendationCache_success_usesRecentInteractionsAndLatestTags_TC011() {
        User user = user();
        Blog coffeeBlog = blog(user.getRegion().getRegionId());
        Blog teaBlog = blog(user.getRegion().getRegionId());
        coffeeBlog.setPageId(null);
        teaBlog.setPageId(null);
        AiModerationResult coffeeTags = moderationTags(coffeeBlog, List.of("Coffee", "Latte"));
        AiModerationResult teaTags = moderationTags(teaBlog, List.of("Tea"));
        BlogFeedRankingServiceImpl service = service();
        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(teaBlog, coffeeBlog));
        when(blogLikeRepository.findRecentlyLikedBlogIds(eq(user.getUserId()), any(), any(), any()))
                .thenReturn(List.of(coffeeBlog.getId()));
        when(aiModerationResultRepository.findWithTagsByBlogIds(any()))
                .thenReturn(List.of(coffeeTags, teaTags));

        service.rebuildRecommendationCache(user.getUserId(), TrendWindowType.MONTH_1, null);

        List<Object[]> calls = upsertArguments();
        assertThat(calls).hasSize(2);
        assertThat(calls.getFirst()[2]).isEqualTo(coffeeBlog.getId());
        assertThat((Double) calls.getFirst()[8]).isEqualTo(1.0);
        assertThat((Double) calls.get(1)[8]).isZero();
        assertThat((Double) calls.getFirst()[7]).isEqualTo(0.5);
    }

    @Test
    void getPersonalizedFeed_success_rebuildsLegacyFormulaOnce_TC012() {
        User user = user();
        Blog blog = blog(user.getRegion().getRegionId());
        LocalDateTime computedAt = LocalDateTime.now().minusMinutes(10);
        BlogFeedRankingServiceImpl service = service();
        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRecommendationScoreRepository.findLatestComputedAt(
                user.getUserId(), TrendWindowType.DAY_7, user.getRegion().getRegionId()))
                .thenReturn(computedAt);
        when(blogRecommendationScoreRepository.findFormulaVersionsAtComputedAt(
                user.getUserId(), TrendWindowType.DAY_7, user.getRegion().getRegionId(), computedAt))
                .thenReturn(List.of("LEGACY_V1"));
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(blog));
        when(blogRecommendationScoreRepository.findLatestPage(
                eq(user.getUserId()),
                eq(TrendWindowType.DAY_7),
                eq(user.getRegion().getRegionId()),
                eq(computedAt),
                any(Pageable.class))).thenReturn(List.of(recommendationScore(user, blog, 0.4, 1)));

        List<BlogFeedResponse> result = service.getPersonalizedFeed(
                user.getUserId(), TrendWindowType.DAY_7, null, 0, 20);

        assertThat(result).hasSize(1);
        assertThat(upsertArguments()).singleElement().satisfies(arguments ->
                assertThat(arguments[6]).isEqualTo(FeedScoreCalculationService.FORMULA_VERSION));
        verify(blogRecommendationScoreRepository).findLatestPage(any(), any(), any(), any(), any());
    }

    @Test
    void getPersonalizedFeed_success_returnsStaleSnapshotWithoutWaitingForDuplicateRebuild_TC014() {
        User user = user();
        Blog blog = blog(user.getRegion().getRegionId());
        LocalDateTime computedAt = LocalDateTime.now().minusMinutes(10);
        BlogRecommendationScore legacyScore = recommendationScore(user, blog, 0.4, 1);
        BlogFeedRankingServiceImpl service = service();
        org.mockito.Mockito.doNothing().when(taskExecutor).execute(any(Runnable.class));

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRecommendationScoreRepository.findLatestComputedAt(
                user.getUserId(), TrendWindowType.DAY_7, user.getRegion().getRegionId()))
                .thenReturn(computedAt);
        when(blogRecommendationScoreRepository.findFormulaVersionsAtComputedAt(
                user.getUserId(), TrendWindowType.DAY_7, user.getRegion().getRegionId(), computedAt))
                .thenReturn(List.of("LEGACY_V1"));
        when(blogRecommendationScoreRepository.findLatestPage(
                eq(user.getUserId()), eq(TrendWindowType.DAY_7), eq(user.getRegion().getRegionId()),
                eq(computedAt), any(Pageable.class))).thenReturn(List.of(legacyScore));

        List<BlogFeedResponse> first = service.getPersonalizedFeed(
                user.getUserId(), TrendWindowType.DAY_7, null, 0, 20);
        List<BlogFeedResponse> second = service.getPersonalizedFeed(
                user.getUserId(), TrendWindowType.DAY_7, null, 0, 20);

        assertThat(first).extracting(BlogFeedResponse::getBlogId).containsExactly(blog.getId());
        assertThat(second).extracting(BlogFeedResponse::getBlogId).containsExactly(blog.getId());
        verify(taskExecutor, times(1)).execute(any(Runnable.class));
        verify(recommendationScoreBatchWriter, never()).upsertAll(any(), any());
    }

    @Test
    void rebuildRecommendationCache_success_appliesDiversityThenStableTieBreak_TC013() {
        User user = user();
        UUID sourceA = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID sourceB = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        Blog aLow = blog(user.getRegion().getRegionId());
        aLow.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        aLow.setPageId(sourceA);
        aLow.setCreatedAt(createdAt);
        Blog bMiddle = blog(user.getRegion().getRegionId());
        bMiddle.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        bMiddle.setPageId(sourceB);
        bMiddle.setCreatedAt(createdAt);
        Blog aHigh = blog(user.getRegion().getRegionId());
        aHigh.setId(UUID.fromString("00000000-0000-0000-0000-000000000003"));
        aHigh.setPageId(sourceA);
        aHigh.setCreatedAt(createdAt);
        BlogFeedRankingServiceImpl service = service();
        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(aLow, bMiddle, aHigh));

        service.rebuildRecommendationCache(user.getUserId(), TrendWindowType.HOUR_24, null);

        List<Object[]> calls = upsertArguments();
        assertThat(calls).extracting(arguments -> (UUID) arguments[2])
                .containsExactly(aHigh.getId(), bMiddle.getId(), aLow.getId());
        assertThat(calls).extracting(arguments -> (Double) arguments[12])
                .containsExactly(1.0, 1.0, 0.5);
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> upsertArguments() {
        return mockingDetails(recommendationScoreBatchWriter).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("upsertAll"))
                .flatMap(invocation -> ((List<BlogRecommendationScore>) invocation.getArgument(0)).stream())
                .map(score -> new Object[]{
                        score.getId(),
                        score.getUser().getUserId(),
                        score.getBlog().getId(),
                        score.getWindowType().name(),
                        score.getContextRegionId(),
                        score.getFeedScore(),
                        score.getFormulaVersion(),
                        score.getRelationshipScore(),
                        score.getInterestScore(),
                        score.getEngagementScore(),
                        score.getQualityScore(),
                        score.getLocationScore(),
                        score.getDiversityScore(),
                        score.getUnseenScore(),
                        score.getTrendingScore(),
                        score.getFreshnessScore(),
                        score.getSameRegionScore(),
                        score.getFollowedUserScore(),
                        score.getFollowedPageScore(),
                        score.getActivityScore(),
                        score.getOwnAuthorScore(),
                        score.getReviewerScore(),
                        score.getReportPenalty(),
                        score.getSeenPenalty(),
                        score.getRepetitionPenalty(),
                        score.getRankPosition(),
                        score.getReason(),
                        score.getComputedAt()
                })
                .toList();
    }

    private AiModerationResult moderationTags(Blog blog, List<String> tags) {
        AiModerationResult result = new AiModerationResult();
        result.setBlog(blog);
        result.setTags(tags);
        result.setCreatedAt(LocalDateTime.now());
        return result;
    }

    private BlogFeedRankingServiceImpl service() {
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));
        lenient().doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<BlogRecommendationScore> scores = invocation.getArgument(0);
            LocalDateTime createdAt = invocation.getArgument(1);
            scores.forEach(score -> blogRecommendationScoreRepository.upsertRecommendationScore(
                    score.getId() == null ? UUID.randomUUID() : score.getId(),
                    score.getUser().getUserId(),
                    score.getBlog().getId(),
                    score.getWindowType().name(),
                    score.getContextRegionId(),
                    score.getFeedScore(),
                    score.getFormulaVersion(),
                    score.getRelationshipScore(),
                    score.getInterestScore(),
                    score.getEngagementScore(),
                    score.getQualityScore(),
                    score.getLocationScore(),
                    score.getDiversityScore(),
                    score.getUnseenScore(),
                    score.getTrendingScore(),
                    score.getFreshnessScore(),
                    score.getSameRegionScore(),
                    score.getFollowedUserScore(),
                    score.getFollowedPageScore(),
                    score.getActivityScore(),
                    score.getOwnAuthorScore(),
                    score.getReviewerScore(),
                    score.getReportPenalty(),
                    score.getSeenPenalty(),
                    score.getRepetitionPenalty(),
                    score.getRankPosition(),
                    score.getReason(),
                    score.getComputedAt(),
                    createdAt));
            return null;
        }).when(recommendationScoreBatchWriter).upsertAll(any(), any());
        lenient().when(blogEventRepository.countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                any(UUID.class),
                any(),
                any(),
                any())).thenReturn(0L);
        lenient().when(aiModerationResultRepository.existsByBlogIdAndDecision(any(UUID.class), any())).thenReturn(false);
        lenient().when(aiModerationResultRepository.findBlogIdsByBlogIdInAndDecision(any(), any())).thenReturn(List.of());
        lenient().when(aiModerationResultRepository.findWithTagsByBlogIds(any())).thenReturn(List.of());
        lenient().when(contentReportRepository.countByBlogIdsAndStatusIn(any(), any())).thenReturn(List.of());
        lenient().when(blogEventRepository.countByBlogIdsAndEventTypeAndCreatedAtBetween(any(), any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(blogLikeRepository.countByBlogIdsAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(commentRepository.countRootCommentsByBlogIdsAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(commentRepository.countRepliesByBlogIdsAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(blogShareRepository.countByBlogIdsAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(blogSaveRepository.countByBlogIdsAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(blogLikeRepository.findRecentlyLikedBlogIds(any(), any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(commentRepository.findRecentlyCommentedBlogIds(any(), any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(blogShareRepository.findRecentlySharedBlogIds(any(), any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(blogSaveRepository.findRecentlySavedBlogIds(any(), any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(feedImpressionRepository.findSeenBlogIds(any(UUID.class), any(), any()))
                .thenReturn(List.of());
        lenient().when(pageFollowRepository.findFollowedCafePageIds(any(UUID.class), any())).thenReturn(List.of());
        lenient().when(userFollowRepository.findFollowedUserIds(any(UUID.class), any())).thenReturn(List.of());
        lenient().when(blogRecommendationScoreRepository.findFormulaVersionsAtComputedAt(any(), any(), any(), any()))
                .thenReturn(List.of(FeedScoreCalculationService.FORMULA_VERSION));
        lenient().when(blogLikeRepository.findLikedBlogIdsByUserIdAndBlogIds(any(UUID.class), any())).thenReturn(List.of());
        lenient().when(blogSaveRepository.findSavedBlogIdsByUserIdAndBlogIds(any(UUID.class), any())).thenReturn(List.of());
        lenient().when(blogRepository.findByIdIn(any())).thenAnswer(invocation -> {
            List<UUID> blogIds = invocation.getArgument(0);
            List<Blog> publishedBlogs = blogRepository.findByStatus(PostStatus.PUBLISHED);
            if (publishedBlogs == null) {
                return List.of();
            }
            return publishedBlogs.stream()
                    .filter(blog -> blogIds.contains(blog.getId()))
                    .toList();
        });
        lenient().when(blogRepository.findImageUrlsByBlogIds(any())).thenAnswer(invocation -> {
            List<UUID> blogIds = invocation.getArgument(0);
            return blogIds.stream()
                    .map(blogId -> new BlogRepository.BlogImageUrlRow() {
                        @Override
                        public UUID getBlogId() {
                            return blogId;
                        }

                        @Override
                        public String getImageUrl() {
                            return "/images/feed/cafe.jpg";
                        }
                    })
                    .toList();
        });
        lenient().when(regionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        lenient().when(cafePageRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<UUID> pageIds = invocation.getArgument(0);
            List<CafePage> cafePages = new ArrayList<>();
            pageIds.forEach(pageId -> cafePages.add(cafePage(pageId)));
            return cafePages;
        });
        lenient().when(regionRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<UUID> regionIds = invocation.getArgument(0);
            List<Region> regions = new ArrayList<>();
            regionIds.forEach(regionId -> {
                Region region = region("Ho Chi Minh");
                region.setRegionId(regionId);
                regions.add(region);
            });
            return regions;
        });
        return new BlogFeedRankingServiceImpl(
                blogRepository,
                blogRecommendationScoreRepository,
                cafePageRepository,
                pageFollowRepository,
                userFollowRepository,
                blogEventRepository,
                blogLikeRepository,
                commentRepository,
                blogShareRepository,
                blogSaveRepository,
                feedImpressionRepository,
                aiModerationResultRepository,
                contentReportRepository,
                regionRepository,
                userRepository,
                userValidator,
                new ConcurrentMapCacheManager(
                        CacheConfig.ORGANIC_FEED_CACHE,
                        CacheConfig.PERSONALIZED_FEED_RANKING_CACHE),
                new FeedScoreCalculationServiceImpl(),
                recommendationScoreBatchWriter,
                taskExecutor,
                transactionManager);
    }

    private BlogEventRepository.BlogEventCountRow blogEventCountRow(UUID blogId, Long eventCount) {
        return new BlogEventRepository.BlogEventCountRow() {
            @Override
            public UUID getBlogId() {
                return blogId;
            }

            @Override
            public Long getEventCount() {
                return eventCount;
            }
        };
    }

    private ContentReportRepository.ReportCountRow contentReportCountRow(UUID blogId, long reportCount) {
        return new ContentReportRepository.ReportCountRow() {
            @Override
            public UUID getTargetId() {
                return blogId;
            }

            @Override
            public long getReportCount() {
                return reportCount;
            }
        };
    }

    private User user() {
        Region region = region("Ho Chi Minh");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("reader");
        user.setAccountStatus(true);
        user.setRegion(region);
        return user;
    }

    private Region region(String city) {
        Region region = new Region();
        region.setRegionId(UUID.randomUUID());
        region.setCity(city);
        return region;
    }

    private Blog blog(UUID regionId) {
        User author = new User();
        author.setUserId(UUID.randomUUID());
        author.setUserName("author");
        author.setUserFullName("Author Name");
        author.setUserAvatar("/images/default-avatar.svg");

        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(author);
        blog.setPageId(UUID.randomUUID());
        blog.setRegionId(regionId);
        blog.setContent("Cafe Story personalized feed blog");
        blog.setImageUrls(List.of("/images/feed/cafe.jpg"));
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setIsPinned(false);
        blog.setAllowComment(true);
        blog.setLikeCount(12);
        blog.setCommentCount(3);
        blog.setShareCount(4);
        blog.setCreatedAt(LocalDateTime.now().minusHours(1));
        return blog;
    }

    private Blog blogWithAuthor(User author, UUID regionId) {
        Blog blog = blog(regionId);
        blog.setAuthor(author);
        blog.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        return blog;
    }

    private Blog organicBlog(
            UUID blogId,
            UUID pageId,
            int likeCount,
            int commentCount,
            int shareCount,
            LocalDateTime createdAt) {
        User author = new User();
        author.setUserId(UUID.randomUUID());
        author.setUserName("author");
        author.setUserFullName("Author Name");
        author.setUserAvatar("/images/default-avatar.svg");

        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setAuthor(author);
        blog.setPageId(pageId);
        blog.setContent("Cafe Story organic feed blog");
        blog.setImageUrls(List.of("/images/feed/cafe.jpg"));
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setLikeCount(likeCount);
        blog.setCommentCount(commentCount);
        blog.setShareCount(shareCount);
        blog.setCreatedAt(createdAt);
        return blog;
    }

    private CafePage cafePage(UUID cafePageId) {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setName("Cafe Story Roastery");
        cafePage.setAddress("123 Brew Street");
        cafePage.setAvatarUrl("/images/cafe-avatar.jpg");
        cafePage.setCoverUrl("/images/cafe-cover.jpg");
        return cafePage;
    }

    private BlogTrendingScore trendingScore(Blog blog, double score) {
        BlogTrendingScore trendingScore = new BlogTrendingScore();
        trendingScore.setBlog(blog);
        trendingScore.setWindowType(TrendWindowType.HOUR_24);
        trendingScore.setTrendScore(score);
        trendingScore.setRankPosition(1);
        trendingScore.setComputedAt(LocalDateTime.now());
        return trendingScore;
    }

    private BlogRecommendationScore recommendationScore(User user, Blog blog, double feedScore, int rankPosition) {
        BlogRecommendationScore score = new BlogRecommendationScore();
        score.setUser(user);
        score.setBlog(blog);
        score.setWindowType(TrendWindowType.HOUR_24);
        score.setContextRegionId(user.getRegion().getRegionId());
        score.setFeedScore(feedScore);
        score.setTrendingScore(100.0);
        score.setFollowedPageScore(30.0);
        score.setFollowedUserScore(25.0);
        score.setSameRegionScore(15.0);
        score.setFreshnessScore(6.0);
        score.setActivityScore(0.0);
        score.setOwnAuthorScore(0.0);
        score.setReviewerScore(0.0);
        score.setReportPenalty(0.0);
        score.setSeenPenalty(0.0);
        score.setRepetitionPenalty(0.0);
        score.setRankPosition(rankPosition);
        score.setReason("cached score");
        score.setComputedAt(LocalDateTime.now());
        return score;
    }
}
