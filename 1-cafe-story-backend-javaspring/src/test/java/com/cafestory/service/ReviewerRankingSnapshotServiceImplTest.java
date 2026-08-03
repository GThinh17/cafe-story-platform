package com.cafestory.service;

import com.cafestory.dto.responseDTO.ReviewerRankingSnapshotResponseDTO;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.ReviewerRankingSnapshot;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerRankingSnapshotRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceImplement.ReviewerRankingSnapshotServiceImpl;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerRankingSnapshotServiceImplTest {

    private static final UUID REVIEWER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID FORMULA_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 6, 15);
    private static final String MONTH_PERIOD = "2026-06";

    @Mock
    private ReviewerRankingSnapshotRepository snapshotRepository;

    @Mock
    private ReviewerRepository reviewerRepository;

    @Mock
    private BlogLikeRepository blogLikeRepository;

    @Mock
    private BlogShareRepository blogShareRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewerFormulaService formulaService;

    @Mock
    private ReviewerBadgeThresholdService badgeThresholdService;

    @Mock
    private ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;

    private ReviewerRankingSnapshotServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReviewerRankingSnapshotServiceImpl(
                snapshotRepository,
                reviewerRepository,
                blogLikeRepository,
                blogShareRepository,
                commentRepository,
                formulaService,
                badgeThresholdService,
                reviewerBadgeHistoryRepository);
    }

    @Test
    void generateSnapshot_monthly_upsertsReviewerBadges_TC001() {
        Reviewer reviewer = reviewer();
        stubEmptyEngagementWith(reviewer);
        when(formulaService.calculateScore(0, 0, 0)).thenReturn(150L);
        when(badgeThresholdService.badgeForScore(150L)).thenReturn(ReviewerBadge.BRONZE);
        when(reviewerBadgeHistoryRepository.findByReviewerReviewerIdAndMonth(REVIEWER_ID, MONTH_PERIOD))
                .thenReturn(Optional.empty());

        service.generateSnapshot(RankingPeriodType.MONTHLY, REFERENCE_DATE);

        ArgumentCaptor<ReviewerBadgeHistory> captor = ArgumentCaptor.forClass(ReviewerBadgeHistory.class);
        verify(reviewerBadgeHistoryRepository).save(captor.capture());
        ReviewerBadgeHistory saved = captor.getValue();
        assertThat(saved.getReviewer()).isEqualTo(reviewer);
        assertThat(saved.getMonth()).isEqualTo(MONTH_PERIOD);
        assertThat(saved.getScore()).isEqualTo(150L);
        assertThat(saved.getBadge()).isEqualTo(ReviewerBadge.BRONZE);
    }

    @Test
    void generateSnapshot_daily_doesNotUpsertReviewerBadges_TC002() {
        Reviewer reviewer = reviewer();
        stubEmptyEngagementWith(reviewer);
        when(formulaService.calculateScore(0, 0, 0)).thenReturn(50L);

        service.generateSnapshot(RankingPeriodType.DAILY, REFERENCE_DATE);

        verify(reviewerBadgeHistoryRepository, never()).save(any());
        verify(reviewerBadgeHistoryRepository, never()).findByReviewerReviewerIdAndMonth(any(), any());
    }

    @Test
    void generateSnapshot_weekly_doesNotUpsertReviewerBadges_TC003() {
        Reviewer reviewer = reviewer();
        stubEmptyEngagementWith(reviewer);
        when(formulaService.calculateScore(0, 0, 0)).thenReturn(50L);

        service.generateSnapshot(RankingPeriodType.WEEKLY, REFERENCE_DATE);

        verify(reviewerBadgeHistoryRepository, never()).save(any());
    }

    @Test
    void getRanking_monthly_enrichesBadgeFromHistory_TC004() {
        ReviewerRankingSnapshot snapshot = snapshot(150L);
        when(snapshotRepository.findByPeriodAndPeriodType(eq(MONTH_PERIOD), eq(RankingPeriodType.MONTHLY), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(snapshot)));
        ReviewerBadgeHistory history = new ReviewerBadgeHistory();
        history.setReviewer(snapshot.getReviewer());
        history.setMonth(MONTH_PERIOD);
        history.setBadge(ReviewerBadge.BRONZE);
        when(reviewerBadgeHistoryRepository.findByMonthAndReviewerReviewerIdIn(eq(MONTH_PERIOD), any()))
                .thenReturn(List.of(history));

        Page<ReviewerRankingSnapshotResponseDTO> result = service.getRanking(MONTH_PERIOD, RankingPeriodType.MONTHLY, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBadge()).isEqualTo(ReviewerBadge.BRONZE);
    }

    @Test
    void getRanking_daily_doesNotQueryBadgeHistory_TC005() {
        ReviewerRankingSnapshot snapshot = snapshot(150L);
        when(snapshotRepository.findByPeriodAndPeriodType(eq("2026-06-15"), eq(RankingPeriodType.DAILY), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(snapshot)));

        Page<ReviewerRankingSnapshotResponseDTO> result = service.getRanking("2026-06-15", RankingPeriodType.DAILY, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBadge()).isNull();
        verify(reviewerBadgeHistoryRepository, never()).findByMonthAndReviewerReviewerIdIn(any(), any());
    }

    private void stubEmptyEngagementWith(Reviewer reviewer) {
        when(reviewerRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(any(String.class), any(RankingPeriodType.class)))
                .thenReturn(List.of());
        when(formulaService.getActiveFormula()).thenReturn(formula());
    }

    private Reviewer reviewer() {
        Reviewer reviewer = new Reviewer();
        reviewer.setReviewerId(REVIEWER_ID);
        User user = new User();
        user.setUserId(USER_ID);
        reviewer.setUser(user);
        return reviewer;
    }

    private ReviewerFormula formula() {
        ReviewerFormula formula = new ReviewerFormula();
        formula.setId(FORMULA_ID);
        return formula;
    }

    private ReviewerRankingSnapshot snapshot(long score) {
        ReviewerRankingSnapshot snapshot = new ReviewerRankingSnapshot();
        snapshot.setId(UUID.randomUUID());
        Reviewer reviewer = reviewer();
        User user = reviewer.getUser();
        user.setUserName("zdexios");
        snapshot.setReviewer(reviewer);
        snapshot.setPeriod(MONTH_PERIOD);
        snapshot.setPeriodType(RankingPeriodType.MONTHLY);
        snapshot.setRankPosition(1);
        snapshot.setScore(score);
        snapshot.setFormula(formula());
        return snapshot;
    }
}
