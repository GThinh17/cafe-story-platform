package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogDisplayAuthorType;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogDailyMetric;
import com.cafestory.entity.BlogTrendingScore;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogDailyMetricRepository;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
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
import org.springframework.data.domain.PageRequest;

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
    private BlogSaveRepository blogSaveRepository;

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

    @Test
    void getTrendingBlogs_success_noSnapshotYetReturnsEmpty_TC005() {
        BlogTrendingServiceImpl service = service();
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.HOUR_24)).thenReturn(null);

        assertThat(service.getTrendingBlogs(null, TrendWindowType.HOUR_24, 0, 10)).isEmpty();
    }

    @Test
    void getTrendingBlogs_success_emptySnapshotReturnsEmpty_TC006() {
        BlogTrendingServiceImpl service = service();
        LocalDateTime computedAt = LocalDateTime.now();
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.DAY_7)).thenReturn(computedAt);
        when(blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                eq(TrendWindowType.DAY_7), eq(computedAt), any(PageRequest.class)))
                .thenReturn(List.of());

        assertThat(service.getTrendingBlogs(UUID.randomUUID(), TrendWindowType.DAY_7, 0, 10)).isEmpty();
    }

    @Test
    void getTrendingBlogs_success_anonymousViewerHasNoLikeOrSaveFlags_TC007() {
        BlogTrendingServiceImpl service = service();
        Blog blog = blog("anon");
        blog.getAuthor().setUserFullName("Nguyen Van A");
        blog.getAuthor().setUserAvatar("https://cdn.example.com/a.png");
        blog.setLikeCount(5);
        blog.setCommentCount(2);
        blog.setShareCount(1);
        BlogTrendingScore score = trendingScore(blog, TrendWindowType.MONTH_1);
        LocalDateTime computedAt = score.getComputedAt();
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.MONTH_1)).thenReturn(computedAt);
        when(blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                eq(TrendWindowType.MONTH_1), eq(computedAt), any(PageRequest.class)))
                .thenReturn(List.of(score));
        when(blogRepository.findImageUrlsByBlogIds(List.of(blog.getId())))
                .thenReturn(List.of(imageUrlRow(blog.getId(), "https://cdn.example.com/1.png")));

        var result = service.getTrendingBlogs(null, TrendWindowType.MONTH_1, -1, 0);

        assertThat(result).hasSize(1);
        var response = result.get(0);
        assertThat(response.getBlogId()).isEqualTo(blog.getId());
        assertThat(response.getImageUrls()).containsExactly("https://cdn.example.com/1.png");
        assertThat(response.getDisplayAuthorType()).isEqualTo(BlogDisplayAuthorType.USER);
        assertThat(response.getDisplayName()).isEqualTo("Nguyen Van A");
        assertThat(response.getDisplayAvatarUrl()).isEqualTo("https://cdn.example.com/a.png");
        assertThat(response.getPageId()).isNull();
        assertThat(response.getPageName()).isNull();
        assertThat(response.getIsLike()).isFalse();
        assertThat(response.getIsSave()).isFalse();
        assertThat(response.getRankPosition()).isEqualTo(1);
        assertThat(response.getTrendScore()).isEqualTo(42.0);
        verify(blogLikeRepository, never()).findLikedBlogIdsByUserIdAndBlogIds(any(UUID.class), any());
    }

    @Test
    void getTrendingBlogs_success_signedInViewerGetsLikeAndSaveFlags_TC008() {
        BlogTrendingServiceImpl service = service();
        UUID viewerUserId = UUID.randomUUID();
        Blog blog = blog("signed");
        blog.getAuthor().setUserFullName("   ");
        BlogTrendingScore score = trendingScore(blog, TrendWindowType.HOUR_24);
        LocalDateTime computedAt = score.getComputedAt();
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.HOUR_24)).thenReturn(computedAt);
        when(blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                eq(TrendWindowType.HOUR_24), eq(computedAt), any(PageRequest.class)))
                .thenReturn(List.of(score));
        when(blogRepository.findImageUrlsByBlogIds(List.of(blog.getId()))).thenReturn(List.of());
        when(blogLikeRepository.findLikedBlogIdsByUserIdAndBlogIds(viewerUserId, List.of(blog.getId())))
                .thenReturn(List.of(blog.getId()));
        when(blogSaveRepository.findSavedBlogIdsByUserIdAndBlogIds(viewerUserId, List.of(blog.getId())))
                .thenReturn(List.of());

        var response = service.getTrendingBlogs(viewerUserId, TrendWindowType.HOUR_24, 0, 10).get(0);

        assertThat(response.getIsLike()).isTrue();
        assertThat(response.getIsSave()).isFalse();
        assertThat(response.getImageUrls()).isEmpty();
        // Họ tên rỗng thì hiển thị tên đăng nhập.
        assertThat(response.getDisplayName()).isEqualTo("signed_author");
    }

    @Test
    void getTrendingBlogs_success_cafePagePostShowsPageIdentity_TC009() {
        BlogTrendingServiceImpl service = service();
        Blog blog = blog("page");
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setName("Cafe Story Ninh Kieu");
        cafePage.setAvatarUrl("https://cdn.example.com/page.png");
        cafePage.setCoverUrl("https://cdn.example.com/cover.png");
        // setPageId dựng lại tham chiếu page rút gọn, nên phải gán trước setPage.
        blog.setPageId(cafePage.getId());
        blog.setPage(cafePage);
        BlogTrendingScore score = trendingScore(blog, TrendWindowType.DAY_7);
        LocalDateTime computedAt = score.getComputedAt();
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.DAY_7)).thenReturn(computedAt);
        when(blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                eq(TrendWindowType.DAY_7), eq(computedAt), any(PageRequest.class)))
                .thenReturn(List.of(score));
        when(blogRepository.findImageUrlsByBlogIds(List.of(blog.getId()))).thenReturn(List.of());

        var response = service.getTrendingBlogs(null, TrendWindowType.DAY_7, 0, 10).get(0);

        assertThat(response.getDisplayAuthorType()).isEqualTo(BlogDisplayAuthorType.CAFE_PAGE);
        assertThat(response.getDisplayName()).isEqualTo("Cafe Story Ninh Kieu");
        assertThat(response.getDisplayAvatarUrl()).isEqualTo("https://cdn.example.com/page.png");
        assertThat(response.getPageCoverUrl()).isEqualTo("https://cdn.example.com/cover.png");
        assertThat(response.getPageId()).isEqualTo(cafePage.getId());
    }

    @Test
    void getTrendingBlogs_success_longContentIsTruncatedToPreview_TC010() {
        BlogTrendingServiceImpl service = service();
        Blog blog = blog("long");
        blog.setContent("x".repeat(300));
        BlogTrendingScore score = trendingScore(blog, TrendWindowType.HOUR_24);
        LocalDateTime computedAt = score.getComputedAt();
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.HOUR_24)).thenReturn(computedAt);
        when(blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                eq(TrendWindowType.HOUR_24), eq(computedAt), any(PageRequest.class)))
                .thenReturn(List.of(score));
        when(blogRepository.findImageUrlsByBlogIds(List.of(blog.getId()))).thenReturn(List.of());

        assertThat(service.getTrendingBlogs(null, TrendWindowType.HOUR_24, 0, 10).get(0).getContentPreview())
                .hasSize(140);
    }

    @Test
    void getTrendingBlogs_success_nullContentIsKeptAsNull_TC011() {
        BlogTrendingServiceImpl service = service();
        Blog blog = blog("null-content");
        blog.setContent(null);
        BlogTrendingScore score = trendingScore(blog, TrendWindowType.HOUR_24);
        LocalDateTime computedAt = score.getComputedAt();
        when(blogTrendingScoreRepository.findLatestComputedAt(TrendWindowType.HOUR_24)).thenReturn(computedAt);
        when(blogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc(
                eq(TrendWindowType.HOUR_24), eq(computedAt), any(PageRequest.class)))
                .thenReturn(List.of(score));
        when(blogRepository.findImageUrlsByBlogIds(List.of(blog.getId()))).thenReturn(List.of());

        assertThat(service.getTrendingBlogs(null, TrendWindowType.HOUR_24, 0, 10).get(0).getContentPreview())
                .isNull();
    }

    @Test
    void calculateTrendingScores_success_pinnedBlogRanksFirstDespiteLowerScore_TC012() {
        Blog pinned = blog("pinned");
        Blog popular = blog("popular");
        BlogTrendingServiceImpl service = new BlogTrendingServiceImpl(
                blogRepository,
                blogEventRepository,
                blogLikeRepository,
                blogSaveRepository,
                commentRepository,
                blogShareRepository,
                blogDailyMetricRepository,
                blogTrendingScoreRepository,
                blogRankingService,
                aiModerationResultRepository,
                cafePageRepository);

        when(blogRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(pinned, popular));
        when(aiModerationResultRepository.existsByBlogIdAndDecision(any(UUID.class), eq(ModerationDecision.VIOLATION)))
                .thenReturn(false);
        when(blogRankingService.getActiveAdminBoost(any(UUID.class), any(LocalDateTime.class))).thenReturn(0.0);
        when(blogRankingService.hasActivePinnedOverride(eq(pinned.getId()), any(LocalDateTime.class)))
                .thenReturn(true);
        when(blogRankingService.hasActivePinnedOverride(eq(popular.getId()), any(LocalDateTime.class)))
                .thenReturn(false);
        when(blogRankingService.calculateTrendScore(
                any(BlogDailyMetric.class), any(), org.mockito.ArgumentMatchers.anyDouble(), any()))
                .thenAnswer(invocation -> {
                    BlogDailyMetric metric = invocation.getArgument(0);
                    return metric.getBlog() == pinned ? 1.0 : 99.0;
                });

        service.calculateTrendingScores();

        ArgumentCaptor<BlogTrendingScore> captor = ArgumentCaptor.forClass(BlogTrendingScore.class);
        verify(blogTrendingScoreRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        // Ba khung thời gian × hai bài; bài ghim luôn ở vị trí 1 của từng khung.
        assertThat(captor.getAllValues()).hasSize(6);
        assertThat(captor.getAllValues().get(0).getBlog()).isSameAs(pinned);
        assertThat(captor.getAllValues().get(0).getRankPosition()).isEqualTo(1);
        assertThat(captor.getAllValues().get(1).getBlog()).isSameAs(popular);
        assertThat(captor.getAllValues().get(1).getRankPosition()).isEqualTo(2);
        assertThat(captor.getAllValues())
                .extracting(BlogTrendingScore::getWindowType)
                .containsExactlyInAnyOrder(
                        TrendWindowType.HOUR_24, TrendWindowType.HOUR_24,
                        TrendWindowType.DAY_7, TrendWindowType.DAY_7,
                        TrendWindowType.MONTH_1, TrendWindowType.MONTH_1);
    }

    private BlogTrendingScore trendingScore(Blog blog, TrendWindowType windowType) {
        BlogTrendingScore score = new BlogTrendingScore();
        score.setId(UUID.randomUUID());
        score.setBlog(blog);
        score.setWindowType(windowType);
        score.setTrendScore(42.0);
        score.setRankPosition(1);
        score.setReason("views=1, likes=0");
        score.setComputedAt(LocalDateTime.of(2026, 5, 19, 12, 0));
        return score;
    }

    private BlogRepository.BlogImageUrlRow imageUrlRow(UUID blogId, String imageUrl) {
        return new BlogRepository.BlogImageUrlRow() {
            @Override
            public UUID getBlogId() {
                return blogId;
            }

            @Override
            public String getImageUrl() {
                return imageUrl;
            }
        };
    }

    private BlogTrendingServiceImpl service() {
        lenient().when(blogRankingService.getActiveAdminBoost(any(UUID.class), any(LocalDateTime.class))).thenReturn(0.0);
        lenient().when(blogRankingService.hasActivePinnedOverride(any(UUID.class), any(LocalDateTime.class))).thenReturn(false);
        return new BlogTrendingServiceImpl(
                blogRepository,
                blogEventRepository,
                blogLikeRepository,
                blogSaveRepository,
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
