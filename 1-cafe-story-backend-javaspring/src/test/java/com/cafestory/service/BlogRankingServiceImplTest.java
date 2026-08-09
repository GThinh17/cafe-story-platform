package com.cafestory.service;

import com.cafestory.dto.requestDTO.BlogRankingOverrideRequest;
import com.cafestory.dto.responseDTO.BlogRankingOverrideResponse;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogDailyMetric;
import com.cafestory.entity.BlogRankingOverride;
import com.cafestory.repository.BlogRankingOverrideRepository;
import com.cafestory.service.serviceImplement.BlogRankingServiceImpl;
import com.cafestory.validation.BlogValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link BlogRankingServiceImpl}.
 *
 * <p>Công thức điểm xu hướng gồm ba phần: điểm tương tác có trọng số, hệ số tươi
 * mới suy giảm theo hàm mũ với hằng số thời gian 48 giờ, và điểm phạt do bị báo
 * cáo. Các phép kiểm dưới đây đối chiếu từng phần với giá trị tính tay.
 */
@ExtendWith(MockitoExtension.class)
class BlogRankingServiceImplTest {

    @Mock
    private BlogRankingOverrideRepository blogRankingOverrideRepository;
    @Mock
    private BlogValidator blogValidator;

    private BlogRankingServiceImpl blogRankingService;

    private Blog blog;

    @BeforeEach
    void setUp() {
        blogRankingService = new BlogRankingServiceImpl(blogRankingOverrideRepository, blogValidator);
        blog = new Blog();
        blog.setId(UUID.randomUUID());
    }

    @Test
    void createOverride_success_mapsEveryField_TC001() {
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusDays(3);
        UUID createdBy = UUID.randomUUID();
        BlogRankingOverrideRequest request = new BlogRankingOverrideRequest();
        request.setBoostScore(25.0);
        request.setIsPinned(true);
        request.setReason("Bai viet noi bat trong thang");
        request.setStartAt(startAt);
        request.setEndAt(endAt);
        request.setCreatedBy(createdBy);

        when(blogValidator.validateBlogExists(blog.getId())).thenReturn(blog);
        when(blogRankingOverrideRepository.save(any(BlogRankingOverride.class)))
                .thenAnswer(invocation -> {
                    BlogRankingOverride saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    saved.setCreatedAt(LocalDateTime.now());
                    return saved;
                });

        BlogRankingOverrideResponse result = blogRankingService.createOverride(blog.getId(), request);

        assertThat(result.getBlogId()).isEqualTo(blog.getId());
        assertThat(result.getBoostScore()).isEqualTo(25.0);
        assertThat(result.getIsPinned()).isTrue();
        assertThat(result.getReason()).isEqualTo("Bai viet noi bat trong thang");
        assertThat(result.getStartAt()).isEqualTo(startAt);
        assertThat(result.getEndAt()).isEqualTo(endAt);
        assertThat(result.getCreatedBy()).isEqualTo(createdBy);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void createOverride_success_nullBoostAndPinFallBackToDefaults_TC002() {
        BlogRankingOverrideRequest request = new BlogRankingOverrideRequest();
        when(blogValidator.validateBlogExists(blog.getId())).thenReturn(blog);
        when(blogRankingOverrideRepository.save(any(BlogRankingOverride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BlogRankingOverrideResponse result = blogRankingService.createOverride(blog.getId(), request);

        assertThat(result.getBoostScore()).isZero();
        assertThat(result.getIsPinned()).isFalse();
    }

    @Test
    void getActiveAdminBoost_success_returnsSum_TC003() {
        LocalDateTime now = LocalDateTime.now();
        when(blogRankingOverrideRepository.sumActiveBoostScore(blog.getId(), now)).thenReturn(12.5);

        assertThat(blogRankingService.getActiveAdminBoost(blog.getId(), now)).isEqualTo(12.5);
    }

    @Test
    void getActiveAdminBoost_success_nullSumBecomesZero_TC004() {
        LocalDateTime now = LocalDateTime.now();
        when(blogRankingOverrideRepository.sumActiveBoostScore(blog.getId(), now)).thenReturn(null);

        assertThat(blogRankingService.getActiveAdminBoost(blog.getId(), now)).isZero();
    }

    @Test
    void hasActivePinnedOverride_success_delegatesToRepository_TC005() {
        LocalDateTime now = LocalDateTime.now();
        when(blogRankingOverrideRepository.existsActivePinnedOverride(blog.getId(), now)).thenReturn(true);

        assertThat(blogRankingService.hasActivePinnedOverride(blog.getId(), now)).isTrue();
    }

    @Test
    void calculateTrendScore_success_freshBlogKeepsAlmostFullEngagement_TC006() {
        LocalDateTime now = LocalDateTime.now();
        BlogDailyMetric metric = metric(100L, 10L, 5L, 2L, 3L, 0L);

        double score = blogRankingService.calculateTrendScore(metric, now, 0.0, now);

        // 100*0.2 + 10*2 + 5*4 + 2*6 + 3*5 = 87, hệ số tươi mới bằng 1 tại t = 0
        assertThat(score).isCloseTo(87.0, within(0.0001));
    }

    @Test
    void calculateTrendScore_success_ageDecaysEngagementAndReportsSubtract_TC007() {
        LocalDateTime now = LocalDateTime.now();
        BlogDailyMetric metric = metric(100L, 10L, 5L, 2L, 3L, 2L);

        double score = blogRankingService.calculateTrendScore(metric, now.minusHours(48), 5.0, now);

        // 87 * e^-1 - 2*10 + 5
        assertThat(score).isCloseTo(87.0 * Math.exp(-1.0) - 20.0 + 5.0, within(0.0001));
    }

    @Test
    void calculateTrendScore_success_futureCreatedAtIsClampedToZeroAge_TC008() {
        LocalDateTime now = LocalDateTime.now();
        BlogDailyMetric metric = metric(null, null, null, null, null, null);

        double score = blogRankingService.calculateTrendScore(metric, now.plusHours(5), 3.0, now);

        assertThat(score).isCloseTo(3.0, within(0.0001));
    }

    @Test
    void buildTrendReason_success_listsEveryCounter_TC009() {
        BlogDailyMetric metric = metric(100L, 10L, 5L, 2L, 3L, null);

        assertThat(blogRankingService.buildTrendReason(metric, 7.5))
                .isEqualTo("views=100, likes=10, comments=5, shares=2, saves=3, reports=0, adminBoost=7.5");
    }

    private BlogDailyMetric metric(Long views, Long likes, Long comments, Long shares, Long saves, Long reports) {
        BlogDailyMetric metric = new BlogDailyMetric();
        metric.setId(UUID.randomUUID());
        metric.setBlog(blog);
        metric.setMetricDate(LocalDate.now());
        metric.setViews(views);
        metric.setLikes(likes);
        metric.setComments(comments);
        metric.setShares(shares);
        metric.setSaves(saves);
        metric.setReports(reports);
        return metric;
    }
}
