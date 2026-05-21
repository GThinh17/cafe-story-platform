package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRecommendationScore;
import com.cafestory.entity.BlogTrendingScore;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.BlogRecommendationScoreRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogTrendingScoreRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.BlogFeedRankingServiceImpl;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
    private PageFollowRepository pageFollowRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private BlogEventRepository blogEventRepository;

    @Mock
    private AiModerationResultRepository aiModerationResultRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Test
    void getPersonalizedFeed_success_readsLatestCachedScores_TC001() {
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

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFeedScore()).isEqualTo(91.0);
        assertThat(result.getFirst().getRankPosition()).isEqualTo(1);
        assertThat(result.getFirst().getComputedAt()).isEqualTo(score.getComputedAt());
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void rebuildRecommendationCache_success_scoresAndStoresCache_TC002() {
        User user = user();
        UUID regionId = user.getRegion().getRegionId();
        Region blogRegion = region("Ho Chi Minh");
        Blog blog = blog(blogRegion.getRegionId());
        BlogTrendingScore trendingScore = trendingScore(blog, 100.0);
        LocalDateTime trendingComputedAt = LocalDateTime.of(2026, 5, 19, 10, 0);
        BlogFeedRankingServiceImpl service = service();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.HOUR_24)).thenReturn(trendingComputedAt);
        when(blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                TrendWindowType.HOUR_24,
                trendingComputedAt)).thenReturn(List.of(trendingScore));
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(blog));
        when(aiModerationResultRepository.existsByBlogIdAndDecision(blog.getId(), ModerationDecision.VIOLATION))
                .thenReturn(false);
        when(regionRepository.findById(regionId)).thenReturn(java.util.Optional.of(user.getRegion()));
        when(regionRepository.findById(blog.getRegionId())).thenReturn(java.util.Optional.of(blogRegion));
        when(pageFollowRepository.existsByUserUserIdAndCafePageId(user.getUserId(), blog.getPageId())).thenReturn(true);
        when(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(user.getUserId(), blog.getAuthor().getUserId()))
                .thenReturn(true);

        List<BlogFeedResponse> result = service.rebuildRecommendationCache(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                regionId);

        assertThat(result).hasSize(1);
        BlogFeedResponse response = result.getFirst();
        assertThat(response.getTrendingScore()).isEqualTo(100.0);
        assertThat(response.getFollowedPageScore()).isEqualTo(30.0);
        assertThat(response.getFollowedUserScore()).isEqualTo(25.0);
        assertThat(response.getSameRegionScore()).isEqualTo(15.0);
        assertThat(response.getFeedScore()).isGreaterThan(100.0 * 0.4 + 30 + 25 + 15);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BlogRecommendationScore>> scoresCaptor = ArgumentCaptor.forClass(List.class);
        verify(blogRecommendationScoreRepository).deleteByUserWindowAndContextRegion(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                regionId);
        verify(blogRecommendationScoreRepository).saveAll(scoresCaptor.capture());
        assertThat(scoresCaptor.getValue().getFirst().getRankPosition()).isEqualTo(1);
    }

    @Test
    void rebuildRecommendationCache_success_reportPenaltyReducesScore_TC003() {
        User user = user();
        Blog blog = blog(UUID.randomUUID());
        BlogTrendingScore trendingScore = trendingScore(blog, 100.0);
        LocalDateTime trendingComputedAt = LocalDateTime.of(2026, 5, 19, 10, 0);
        BlogFeedRankingServiceImpl service = service();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.HOUR_24)).thenReturn(trendingComputedAt);
        when(blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                TrendWindowType.HOUR_24,
                trendingComputedAt)).thenReturn(List.of(trendingScore));
        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(blog));
        when(aiModerationResultRepository.existsByBlogIdAndDecision(blog.getId(), ModerationDecision.VIOLATION))
                .thenReturn(false);
        when(blogEventRepository.countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(blog.getId()),
                any(),
                any(),
                any())).thenReturn(3L);

        BlogFeedResponse response = service.rebuildRecommendationCache(
                user.getUserId(),
                TrendWindowType.HOUR_24,
                null).getFirst();

        assertThat(response.getReportPenalty()).isEqualTo(30.0);
        assertThat(response.getReason()).contains("reportPenalty=30.0");
    }

    private BlogFeedRankingServiceImpl service() {
        lenient().when(blogEventRepository.countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                any(UUID.class),
                any(),
                any(),
                any())).thenReturn(0L);
        lenient().when(regionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        return new BlogFeedRankingServiceImpl(
                blogRepository,
                blogTrendingScoreRepository,
                blogRecommendationScoreRepository,
                pageFollowRepository,
                userFollowRepository,
                blogEventRepository,
                aiModerationResultRepository,
                regionRepository,
                userRepository,
                userValidator);
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

        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(author);
        blog.setPageId(UUID.randomUUID());
        blog.setRegionId(regionId);
        blog.setContent("Cafe Story personalized feed blog");
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setCreatedAt(LocalDateTime.now().minusHours(1));
        return blog;
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
        score.setReportPenalty(0.0);
        score.setRankPosition(rankPosition);
        score.setReason("cached score");
        score.setComputedAt(LocalDateTime.now());
        return score;
    }
}
