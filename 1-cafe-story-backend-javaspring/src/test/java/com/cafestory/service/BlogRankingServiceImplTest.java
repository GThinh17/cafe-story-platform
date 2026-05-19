package com.cafestory.service;

import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogDailyMetric;
import com.cafestory.repository.BlogRankingOverrideRepository;
import com.cafestory.service.serviceImplement.BlogRankingServiceImpl;
import com.cafestory.validation.BlogValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BlogRankingServiceImplTest {

    @Mock
    private BlogRankingOverrideRepository blogRankingOverrideRepository;

    @Mock
    private BlogValidator blogValidator;

    @Test
    void calculateTrendScore_success_formula_TC001() {
        BlogRankingServiceImpl service = service();
        BlogDailyMetric metric = metric(10, 3, 2, 1, 1, 0);
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 0);

        double score = service.calculateTrendScore(metric, now, 5.0, now);

        assertThat(score).isEqualTo(10 * 0.2 + 3 * 2 + 2 * 4 + 1 * 6 + 1 * 5 + 5);
    }

    @Test
    void calculateTrendScore_success_oldBlogLosesRanking_TC002() {
        BlogRankingServiceImpl service = service();
        BlogDailyMetric metric = metric(100, 10, 10, 10, 10, 0);
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 0);

        double freshScore = service.calculateTrendScore(metric, now.minusHours(1), 0.0, now);
        double oldScore = service.calculateTrendScore(metric, now.minusHours(96), 0.0, now);

        assertThat(freshScore).isGreaterThan(oldScore);
    }

    @Test
    void calculateTrendScore_success_reportedBlogLosesRanking_TC003() {
        BlogRankingServiceImpl service = service();
        BlogDailyMetric cleanMetric = metric(50, 5, 3, 2, 1, 0);
        BlogDailyMetric reportedMetric = metric(50, 5, 3, 2, 1, 5);
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 0);

        double cleanScore = service.calculateTrendScore(cleanMetric, now, 0.0, now);
        double reportedScore = service.calculateTrendScore(reportedMetric, now, 0.0, now);

        assertThat(cleanScore).isGreaterThan(reportedScore);
    }

    @Test
    void calculateTrendScore_success_adminBoostIncreasesRanking_TC004() {
        BlogRankingServiceImpl service = service();
        BlogDailyMetric metric = metric(10, 1, 1, 1, 1, 0);
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 0);

        double normalScore = service.calculateTrendScore(metric, now, 0.0, now);
        double boostedScore = service.calculateTrendScore(metric, now, 100.0, now);

        assertThat(boostedScore).isGreaterThan(normalScore);
    }

    private BlogRankingServiceImpl service() {
        return new BlogRankingServiceImpl(blogRankingOverrideRepository, blogValidator);
    }

    private BlogDailyMetric metric(long views, long likes, long comments, long shares, long saves, long reports) {
        BlogDailyMetric metric = new BlogDailyMetric();
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        metric.setBlog(blog);
        metric.setMetricDate(LocalDate.of(2026, 5, 19));
        metric.setViews(views);
        metric.setLikes(likes);
        metric.setComments(comments);
        metric.setShares(shares);
        metric.setSaves(saves);
        metric.setReports(reports);
        return metric;
    }
}
