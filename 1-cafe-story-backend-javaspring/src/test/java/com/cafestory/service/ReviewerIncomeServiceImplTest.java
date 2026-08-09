package com.cafestory.service;

import com.cafestory.dto.responseDTO.ReviewerIncomeResponseDTO;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.ReviewerIncome;
import com.cafestory.entity.ReviewerRankingSnapshot;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.AuthorInteractionCountRow;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerIncomeRepository;
import com.cafestory.repository.ReviewerRankingSnapshotRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceImplement.ReviewerIncomeServiceImpl;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link ReviewerIncomeServiceImpl} — bảng thu nhập ngày của reviewer.
 *
 * <p>Trọng tâm: chỉ tính engagement mà bài viết của reviewer NHẬN được, bỏ qua
 * reviewer đã hết hạn gói, và nhân hệ số huy hiệu lấy từ snapshot DAILY.
 */
@ExtendWith(MockitoExtension.class)
class ReviewerIncomeServiceImplTest {

    @Mock
    private ReviewerIncomeRepository incomeRepository;
    @Mock
    private ReviewerRepository reviewerRepository;
    @Mock
    private ReviewerRankingSnapshotRepository snapshotRepository;
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

    private ReviewerIncomeServiceImpl incomeService;

    private final LocalDate incomeDate = LocalDate.of(2026, 7, 15);
    private ReviewerFormula formula;
    private Reviewer firstReviewer;
    private Reviewer secondReviewer;

    @BeforeEach
    void setUp() {
        incomeService = new ReviewerIncomeServiceImpl(
                incomeRepository,
                reviewerRepository,
                snapshotRepository,
                blogLikeRepository,
                blogShareRepository,
                commentRepository,
                formulaService,
                badgeThresholdService);

        formula = formula();
        firstReviewer = reviewer("an", incomeDate.plusMonths(1).atStartOfDay());
        secondReviewer = reviewer("binh", null);
    }

    @Test
    void generateDailyIncome_success_appliesBadgeMultiplierFromDailySnapshot_TC001() {
        when(formulaService.getActiveFormula()).thenReturn(formula);
        when(reviewerRepository.findAllWithUser()).thenReturn(List.of(firstReviewer, secondReviewer));
        when(blogLikeRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstReviewer.getUser().getUserId(), 10L),
                // Tác giả không phải reviewer: phải bị bỏ qua.
                interaction(UUID.randomUUID(), 99L)));
        when(blogShareRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstReviewer.getUser().getUserId(), 2L)));
        when(commentRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstReviewer.getUser().getUserId(), 3L)));
        when(snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(
                "2026-07-15", RankingPeriodType.DAILY))
                .thenReturn(List.of(snapshot(firstReviewer, 900L)));
        when(badgeThresholdService.badgeForScore(900L)).thenReturn(ReviewerBadge.GOLD);
        when(incomeRepository.findByIncomeDate(incomeDate)).thenReturn(List.of());

        incomeService.generateDailyIncome(incomeDate);

        List<ReviewerIncome> saved = captureSaved();
        assertThat(saved).hasSize(2);

        ReviewerIncome first = saved.stream()
                .filter(income -> income.getReviewer() == firstReviewer)
                .findFirst()
                .orElseThrow();
        assertThat(first.getIncomeDate()).isEqualTo(incomeDate);
        assertThat(first.getLikeCount()).isEqualTo(10L);
        assertThat(first.getShareCount()).isEqualTo(2L);
        assertThat(first.getCommentCount()).isEqualTo(3L);
        assertThat(first.getBadge()).isEqualTo(ReviewerBadge.GOLD);
        // 10*100 + 3*500 + 2*300 = 3100, nhân hệ số GOLD 2.00
        assertThat(first.getBaseAmount()).isEqualTo(3_100L);
        assertThat(first.getFinalAmount()).isEqualTo(6_200L);
        assertThat(first.getFormula()).isSameAs(formula);

        ReviewerIncome second = saved.stream()
                .filter(income -> income.getReviewer() == secondReviewer)
                .findFirst()
                .orElseThrow();
        assertThat(second.getBadge()).isEqualTo(ReviewerBadge.IRON);
        assertThat(second.getBaseAmount()).isZero();
        assertThat(second.getFinalAmount()).isZero();
    }

    @Test
    void generateDailyIncome_success_skipsReviewerExpiredBeforeThatDate_TC002() {
        Reviewer expired = reviewer("cuong", incomeDate.minusDays(1).atStartOfDay());
        when(formulaService.getActiveFormula()).thenReturn(formula);
        when(reviewerRepository.findAllWithUser()).thenReturn(List.of(firstReviewer, expired));
        when(blogLikeRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(blogShareRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(commentRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(any(), any()))
                .thenReturn(List.of());
        when(incomeRepository.findByIncomeDate(incomeDate)).thenReturn(List.of());

        incomeService.generateDailyIncome(incomeDate);

        assertThat(captureSaved())
                .extracting(ReviewerIncome::getReviewer)
                .containsExactly(firstReviewer);
    }

    @Test
    void generateDailyIncome_success_keepsReviewerExpiringOnThatDate_TC003() {
        Reviewer expiringToday = reviewer("dung", incomeDate.atTime(23, 59));
        when(formulaService.getActiveFormula()).thenReturn(formula);
        when(reviewerRepository.findAllWithUser()).thenReturn(List.of(expiringToday));
        when(blogLikeRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(blogShareRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(commentRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(any(), any()))
                .thenReturn(List.of());
        when(incomeRepository.findByIncomeDate(incomeDate)).thenReturn(List.of());

        incomeService.generateDailyIncome(incomeDate);

        assertThat(captureSaved()).hasSize(1);
    }

    @Test
    void generateDailyIncome_success_updatesExistingRowInsteadOfDuplicating_TC004() {
        ReviewerIncome existing = new ReviewerIncome();
        existing.setId(UUID.randomUUID());
        existing.setReviewer(firstReviewer);
        existing.setIncomeDate(incomeDate);
        existing.setBaseAmount(1L);
        when(formulaService.getActiveFormula()).thenReturn(formula);
        when(reviewerRepository.findAllWithUser()).thenReturn(List.of(firstReviewer));
        when(blogLikeRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstReviewer.getUser().getUserId(), 4L)));
        when(blogShareRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(commentRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(snapshotRepository.findByPeriodAndPeriodTypeOrderByRankPositionAsc(any(), any()))
                .thenReturn(List.of());
        when(incomeRepository.findByIncomeDate(incomeDate)).thenReturn(List.of(existing));

        incomeService.generateDailyIncome(incomeDate);

        List<ReviewerIncome> saved = captureSaved();
        assertThat(saved).containsExactly(existing);
        assertThat(existing.getBaseAmount()).isEqualTo(400L);
        assertThat(existing.getFinalAmount()).isEqualTo(400L);
    }

    @Test
    void getIncomeByReviewer_success_mapsRowsWithinMonth_TC005() {
        UUID reviewerId = firstReviewer.getReviewerId();
        PageRequest pageable = PageRequest.of(0, 10);
        ReviewerIncome income = income(firstReviewer);
        when(incomeRepository.findByReviewerReviewerIdAndIncomeDateGreaterThanEqualAndIncomeDateLessThan(
                eq(reviewerId), eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 8, 1)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(income)));

        Page<ReviewerIncomeResponseDTO> result =
                incomeService.getIncomeByReviewer(reviewerId, "2026-07", pageable);

        assertThat(result.getContent()).hasSize(1);
        ReviewerIncomeResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getReviewerId()).isEqualTo(reviewerId);
        assertThat(dto.getReviewerUserName()).isEqualTo("an");
        assertThat(dto.getReviewerUserAvatar()).isEqualTo("https://cdn.example.com/an.png");
        assertThat(dto.getBadge()).isEqualTo(ReviewerBadge.SILVER);
        assertThat(dto.getBaseAmount()).isEqualTo(1_000L);
        assertThat(dto.getFinalAmount()).isEqualTo(1_500L);
        assertThat(dto.getFormulaId()).isEqualTo(formula.getId());
    }

    @Test
    void getIncomeByReviewer_fail_invalidMonthFormat_TC006() {
        PageRequest pageable = PageRequest.of(0, 10);
        UUID reviewerId = firstReviewer.getReviewerId();

        assertThatThrownBy(() -> incomeService.getIncomeByReviewer(reviewerId, "07-2026", pageable))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
    }

    @Test
    void getAllIncome_success_mapsRowsWithinMonth_TC007() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(incomeRepository.findByIncomeDateGreaterThanEqualAndIncomeDateLessThan(
                eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 8, 1)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(income(firstReviewer))));

        Page<ReviewerIncomeResponseDTO> result = incomeService.getAllIncome("2026-07", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIncomeDate()).isEqualTo(incomeDate);
    }

    // ------------------------------------------------------------- Helpers

    private List<ReviewerIncome> captureSaved() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReviewerIncome>> captor = ArgumentCaptor.forClass(List.class);
        verify(incomeRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    private ReviewerFormula formula() {
        ReviewerFormula newFormula = new ReviewerFormula();
        newFormula.setId(UUID.randomUUID());
        newFormula.setLikePayoutAmount(100L);
        newFormula.setSharePayoutAmount(300L);
        newFormula.setCommentPayoutAmount(500L);
        newFormula.setIronMultiplier(BigDecimal.ONE);
        newFormula.setGoldMultiplier(new BigDecimal("2.00"));
        return newFormula;
    }

    private Reviewer reviewer(String userName, LocalDateTime expiresAt) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(userName);
        user.setUserAvatar("https://cdn.example.com/" + userName + ".png");
        Reviewer newReviewer = new Reviewer();
        newReviewer.setReviewerId(UUID.randomUUID());
        newReviewer.setUser(user);
        newReviewer.setReviewerActive(true);
        newReviewer.setReviewerExpiresAt(expiresAt);
        return newReviewer;
    }

    private ReviewerIncome income(Reviewer owner) {
        ReviewerIncome newIncome = new ReviewerIncome();
        newIncome.setId(UUID.randomUUID());
        newIncome.setReviewer(owner);
        newIncome.setIncomeDate(incomeDate);
        newIncome.setLikeCount(10L);
        newIncome.setShareCount(2L);
        newIncome.setCommentCount(1L);
        newIncome.setBadge(ReviewerBadge.SILVER);
        newIncome.setBadgeMultiplier(new BigDecimal("1.50"));
        newIncome.setBaseAmount(1_000L);
        newIncome.setFinalAmount(1_500L);
        newIncome.setFormula(formula);
        return newIncome;
    }

    private ReviewerRankingSnapshot snapshot(Reviewer owner, long score) {
        ReviewerRankingSnapshot newSnapshot = new ReviewerRankingSnapshot();
        newSnapshot.setId(UUID.randomUUID());
        newSnapshot.setReviewer(owner);
        newSnapshot.setPeriod("2026-07-15");
        newSnapshot.setPeriodType(RankingPeriodType.DAILY);
        newSnapshot.setScore(score);
        return newSnapshot;
    }

    private AuthorInteractionCountRow interaction(UUID authorUserId, long eventCount) {
        return new AuthorInteractionCountRow() {
            @Override
            public UUID getAuthorUserId() {
                return authorUserId;
            }

            @Override
            public Long getEventCount() {
                return eventCount;
            }
        };
    }
}
