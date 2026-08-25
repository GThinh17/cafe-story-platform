package com.cafestory.until.schedule;

import com.cafestory.entity.Reviewer;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceInterface.AdCampaignService;
import com.cafestory.service.serviceInterface.AdminPaymentService;
import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.service.serviceInterface.BlogTrendingService;
import com.cafestory.service.serviceInterface.ReportModerationService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử cho toàn bộ scheduled job trong {@code com.cafestory.until.schedule}.
 *
 * <p>Mỗi job chỉ là một lớp mỏng uỷ quyền cho service tương ứng, nên phép kiểm
 * chủ yếu xác nhận job gọi đúng phương thức với đúng tham số (đặc biệt là các
 * mốc thời gian: hôm qua, tháng trước) và rẽ đúng nhánh khi service trả về 0.
 */
@ExtendWith(MockitoExtension.class)
class ScheduledJobsTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class AdCampaignExpirationJobTest {

        @Mock
        private AdCampaignService adCampaignService;

        private AdCampaignExpirationJob job;

        @BeforeEach
        void setUp() {
            job = new AdCampaignExpirationJob(adCampaignService);
        }

        @Test
        void expireActiveCampaigns_success_delegatesToService_TC001() {
            job.expireActiveCampaigns();

            verify(adCampaignService).expireActiveCampaigns();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class BlogRecommendationCalculationJobTest {

        @Mock
        private BlogFeedRankingService blogFeedRankingService;

        private BlogRecommendationCalculationJob job;

        @BeforeEach
        void setUp() {
            job = new BlogRecommendationCalculationJob(blogFeedRankingService);
        }

        @Test
        void rebuildRecommendationScores_success_delegatesToService_TC002() {
            job.rebuildRecommendationScores();

            verify(blogFeedRankingService).rebuildRecommendationCacheForAllActiveUsers();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class TrendCalculationJobTest {

        @Mock
        private BlogTrendingService blogTrendingService;

        private TrendCalculationJob job;

        @BeforeEach
        void setUp() {
            job = new TrendCalculationJob(blogTrendingService);
        }

        @Test
        void calculateTrendingScores_success_aggregatesTodayThenScores_TC003() {
            job.calculateTrendingScores();

            verify(blogTrendingService).aggregateDailyMetrics(LocalDate.now());
            verify(blogTrendingService).calculateTrendingScores();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class PaymentExpirationJobTest {

        @Mock
        private AdminPaymentService adminPaymentService;

        private PaymentExpirationJob job;

        @BeforeEach
        void setUp() {
            job = new PaymentExpirationJob(adminPaymentService);
        }

        @Test
        void expireStalePayments_success_logsWhenSomethingExpired_TC004() {
            when(adminPaymentService.expireStalePayments()).thenReturn(3);

            job.expireStalePayments();

            verify(adminPaymentService).expireStalePayments();
        }

        @Test
        void expireStalePayments_success_skipsLogWhenNothingExpired_TC005() {
            when(adminPaymentService.expireStalePayments()).thenReturn(0);

            job.expireStalePayments();

            verify(adminPaymentService).expireStalePayments();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ReportModerationJobWorkerTest {

        @Mock
        private ReportModerationService reportModerationService;

        @Test
        void processDueJobs_success_usesConfiguredBatchSize_TC006() {
            ReportModerationJobWorker worker = new ReportModerationJobWorker(reportModerationService, 25);
            when(reportModerationService.processDueJobs(25)).thenReturn(4);

            worker.processDueJobs();

            verify(reportModerationService).processDueJobs(25);
        }

        @Test
        void processDueJobs_success_skipsLogWhenNothingProcessed_TC007() {
            ReportModerationJobWorker worker = new ReportModerationJobWorker(reportModerationService, 10);
            when(reportModerationService.processDueJobs(10)).thenReturn(0);

            worker.processDueJobs();

            verify(reportModerationService).processDueJobs(10);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class AdminReportAiAutoApplyJobWorkerTest {

        @Mock
        private AdminReportAiAutoApplyJobService autoApplyJobService;

        @Test
        void processDueJobs_success_usesConfiguredBatchSize_TC008() {
            AdminReportAiAutoApplyJobWorker worker =
                    new AdminReportAiAutoApplyJobWorker(autoApplyJobService, 7);
            when(autoApplyJobService.processDueJobs(7)).thenReturn(2);

            worker.processDueJobs();

            verify(autoApplyJobService).processDueJobs(7);
        }

        @Test
        void processDueJobs_success_skipsLogWhenNothingProcessed_TC009() {
            AdminReportAiAutoApplyJobWorker worker =
                    new AdminReportAiAutoApplyJobWorker(autoApplyJobService, 10);
            when(autoApplyJobService.processDueJobs(10)).thenReturn(0);

            worker.processDueJobs();

            verify(autoApplyJobService).processDueJobs(10);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ReviewerRankingSnapshotJobTest {

        @Mock
        private ReviewerRankingSnapshotService snapshotService;

        private ReviewerRankingSnapshotJob job;

        @BeforeEach
        void setUp() {
            job = new ReviewerRankingSnapshotJob(snapshotService);
        }

        @Test
        void generateDailySnapshot_success_usesYesterday_TC010() {
            job.generateDailySnapshot();

            verify(snapshotService).generateSnapshot(RankingPeriodType.DAILY, LocalDate.now().minusDays(1));
        }

        @Test
        void generateMonthlySnapshot_success_usesYesterday_TC011() {
            job.generateMonthlySnapshot();

            verify(snapshotService).generateSnapshot(RankingPeriodType.MONTHLY, LocalDate.now().minusDays(1));
        }

        @Test
        void generateWeeklySnapshot_success_usesYesterdayNotToday_TC012() {
            job.generateWeeklySnapshot();

            verify(snapshotService).generateSnapshot(RankingPeriodType.WEEKLY, LocalDate.now().minusDays(1));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ReviewerPayoutJobTest {

        @Mock
        private ReviewerIncomeService incomeService;

        @Mock
        private AdminPayoutService payoutService;

        private ReviewerPayoutJob job;

        @BeforeEach
        void setUp() {
            job = new ReviewerPayoutJob(incomeService, payoutService);
        }

        @Test
        void generateDailyIncome_success_usesYesterday_TC013() {
            job.generateDailyIncome();

            verify(incomeService).generateDailyIncome(LocalDate.now().minusDays(1));
            verifyNoInteractions(payoutService);
        }

        @Test
        void generateMonthlyPayout_success_usesPreviousMonth_TC014() {
            job.generateMonthlyPayout();

            verify(payoutService).generateMonthlyPayout(YearMonth.now().minusMonths(1).toString());
            verifyNoInteractions(incomeService);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ReviewerExpirationJobTest {

        @Mock
        private ReviewerRepository reviewerRepository;

        private ReviewerExpirationJob job;

        @BeforeEach
        void setUp() {
            job = new ReviewerExpirationJob(reviewerRepository);
        }

        @Test
        void expireLapsedReviewers_success_lowersActiveFlagAndSaves_TC015() {
            Reviewer first = reviewer();
            Reviewer second = reviewer();
            when(reviewerRepository.findByReviewerActiveTrueAndReviewerExpiresAtBefore(any(LocalDateTime.class)))
                    .thenReturn(List.of(first, second));

            job.expireLapsedReviewers();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Reviewer>> captor = ArgumentCaptor.forClass(List.class);
            verify(reviewerRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).containsExactly(first, second);
            assertThat(first.getReviewerActive()).isFalse();
            assertThat(second.getReviewerActive()).isFalse();
        }

        @Test
        void expireLapsedReviewers_success_noLapsedReviewerSkipsSave_TC016() {
            when(reviewerRepository.findByReviewerActiveTrueAndReviewerExpiresAtBefore(any(LocalDateTime.class)))
                    .thenReturn(List.of());

            job.expireLapsedReviewers();

            verify(reviewerRepository, never()).saveAll(any());
        }

        private Reviewer reviewer() {
            Reviewer reviewer = new Reviewer();
            reviewer.setReviewerId(UUID.randomUUID());
            reviewer.setReviewerActive(true);
            reviewer.setReviewerExpiresAt(LocalDateTime.now().minusDays(1));
            return reviewer;
        }
    }
}
