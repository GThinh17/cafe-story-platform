package com.cafestory.service;

import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogDailyMetric;
import com.cafestory.entity.BlogTrendingScore;
import com.cafestory.entity.User;
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
import com.cafestory.service.serviceImplement.BlogTrendingServiceImpl;
import com.cafestory.service.serviceInterface.BlogRankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BlogTrendingServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private BlogEventRepository blogEventRepository;

    @Mock
    private BlogLikeRepository blogLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BlogShareRepository blogShareRepository;

    @Mock
    private BlogDailyMetricRepository blogDailyMetricRepository;

    @Mock
    private BlogTrendingScoreRepository blogTrendingScoreRepository;

    @Mock
    private BlogRankingService blogRankingService;

    @Mock
    private AiModerationResultRepository aiModerationResultRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Test
    void calculateTrendingScores_success_blogWithMoreSharesRanksHigher_TC001() {
        Blog sharedBlog = blog("shared");
        Blog likedBlog = blog("liked");
        BlogTrendingServiceImpl service = service();

        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(sharedBlog, likedBlog));
        when(aiModerationResultRepository.existsByBlogIdAndDecision(any(UUID.class), eq(ModerationDecision.VIOLATION)))
                .thenReturn(false);
        lenient().when(blogShareRepository.countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(sharedBlog.getId()),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(4L);
        lenient().when(blogLikeRepository.countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(likedBlog.getId()),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(10L);
        when(blogRankingService.calculateTrendScore(
                any(BlogDailyMetric.class),
                any(LocalDateTime.class),
                eq(0.0),
                any(LocalDateTime.class))).thenAnswer(invocation -> {
            BlogDailyMetric metric = invocation.getArgument(0);
            return metric.getShares() * 6.0 + metric.getLikes() * 2.0;
        });
        when(blogRankingService.buildTrendReason(any(BlogDailyMetric.class), eq(0.0))).thenReturn("reason");

        service.calculateTrendingScores();

        ArgumentCaptor<BlogTrendingScore> scoreCaptor = ArgumentCaptor.forClass(BlogTrendingScore.class);
        verify(blogTrendingScoreRepository, org.mockito.Mockito.atLeastOnce()).save(scoreCaptor.capture());
        List<BlogTrendingScore> hour24Scores = scoreCaptor.getAllValues()
                .stream()
                .filter(score -> score.getWindowType() == TrendWindowType.HOUR_24)
                .toList();

        BlogTrendingScore firstRank = hour24Scores.stream()
                .filter(score -> score.getRankPosition() == 1)
                .findFirst()
                .orElseThrow();
        assertThat(firstRank.getBlog()).isEqualTo(sharedBlog);
    }

    @Test
    void calculateTrendingScores_success_hiddenRemovedBlogsDoNotAppear_TC002() {
        Blog publishedBlog = blog("published");
        BlogTrendingServiceImpl service = service();

        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(publishedBlog));
        when(aiModerationResultRepository.existsByBlogIdAndDecision(
                publishedBlog.getId(),
                ModerationDecision.VIOLATION)).thenReturn(false);
        when(blogRankingService.calculateTrendScore(
                any(BlogDailyMetric.class),
                any(LocalDateTime.class),
                eq(0.0),
                any(LocalDateTime.class))).thenReturn(1.0);
        when(blogRankingService.buildTrendReason(any(BlogDailyMetric.class), eq(0.0))).thenReturn("reason");

        service.calculateTrendingScores();

        verify(blogRepository, org.mockito.Mockito.times(3)).findByStatus(PostStatus.PUBLISHED);
        verify(blogTrendingScoreRepository, org.mockito.Mockito.times(3)).save(any(BlogTrendingScore.class));
    }

    @Test
    void calculateTrendingScores_success_violationBlogDoesNotAppear_TC003() {
        Blog violationBlog = blog("violation");
        BlogTrendingServiceImpl service = service();

        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(violationBlog));
        when(aiModerationResultRepository.existsByBlogIdAndDecision(
                violationBlog.getId(),
                ModerationDecision.VIOLATION)).thenReturn(true);

        service.calculateTrendingScores();

        verify(blogTrendingScoreRepository, never()).save(any(BlogTrendingScore.class));
    }

    @Test
    void aggregateDailyMetrics_success_duplicateMetricsForSameBlogDateAreNotCreated_TC004() {
        Blog blog = blog("metric");
        BlogDailyMetric existingMetric = new BlogDailyMetric();
        existingMetric.setId(UUID.randomUUID());
        existingMetric.setBlog(blog);
        existingMetric.setMetricDate(LocalDate.of(2026, 5, 19));
        BlogTrendingServiceImpl service = service();

        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(blog));
        when(blogDailyMetricRepository.findByBlogIdAndMetricDate(blog.getId(), existingMetric.getMetricDate()))
                .thenReturn(Optional.of(existingMetric));

        service.aggregateDailyMetrics(existingMetric.getMetricDate());

        verify(blogDailyMetricRepository).save(existingMetric);
    }

    private BlogTrendingServiceImpl service() {
        lenient().when(blogRankingService.getActiveAdminBoost(any(UUID.class), any(LocalDateTime.class))).thenReturn(0.0);
        lenient().when(blogRankingService.hasActivePinnedOverride(any(UUID.class), any(LocalDateTime.class))).thenReturn(false);
        return new BlogTrendingServiceImpl(
                blogRepository,
                blogEventRepository,
                blogLikeRepository,
                commentRepository,
                blogShareRepository,
                blogDailyMetricRepository,
                blogTrendingScoreRepository,
                blogRankingService,
                aiModerationResultRepository,
                cafePageRepository);
    }

    private Blog blog(String name) {
        User author = new User();
        author.setUserId(UUID.randomUUID());
        author.setUserName(name + "_author");

        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(author);
        blog.setContent(name + " blog content");
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setCreatedAt(LocalDateTime.of(2026, 5, 19, 10, 0));
        return blog;
    }
}
