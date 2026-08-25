package com.cafestory.service;

import com.cafestory.dto.responseDTO.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerDiscoveryResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerEarningsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerGeoAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerIncomeResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerRankingResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerSegmentResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStatsResponseDTO;
import com.cafestory.entity.AdminPayout;
import com.cafestory.entity.Region;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserFollow;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.AdminPayoutStatus;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.AdminPayoutRepository;
import com.cafestory.repository.AuthorEngagementCountRow;
import com.cafestory.repository.AuthorInteractionCountRow;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerIncomeRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.service.serviceImplement.ReviewerServiceImpl;
import com.cafestory.service.serviceInterface.ReviewerBadgeThresholdService;
import com.cafestory.service.serviceInterface.ReviewerFormulaService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link ReviewerServiceImpl} — lớp nghiệp vụ lớn nhất của module
 * reviewer: xếp hạng, khám phá theo vùng, huy hiệu tháng, thu nhập và phân khúc.
 *
 * <p>Toàn bộ repository và service cộng tác đều được mô phỏng bằng Mockito nên
 * bộ kiểm thử không cần cơ sở dữ liệu. Các mốc thời gian do lớp tự tính từ
 * {@code LocalDate.now()} nên đối số thời gian được so khớp bằng matcher.
 */
@ExtendWith(MockitoExtension.class)
class ReviewerServiceImplTest {

    @Mock
    private BlogLikeRepository blogLikeRepository;
    @Mock
    private BlogRepository blogRepository;
    @Mock
    private BlogSaveRepository blogSaveRepository;
    @Mock
    private BlogShareRepository blogShareRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AdminPayoutRepository adminPayoutRepository;
    @Mock
    private ReviewerIncomeRepository incomeRepository;
    @Mock
    private ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;
    @Mock
    private ReviewerRepository reviewerRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserFollowRepository userFollowRepository;
    @Mock
    private UserValidator userValidator;
    @Mock
    private ReviewerFormulaService formulaService;
    @Mock
    private ReviewerBadgeThresholdService badgeThresholdService;
    @Mock
    private ReviewerRankingSnapshotService snapshotService;
    @Mock
    private ReviewerIncomeService incomeService;

    private ReviewerServiceImpl reviewerService;

    private User firstUser;
    private User secondUser;
    private Reviewer firstReviewer;
    private Reviewer secondReviewer;

    @BeforeEach
    void setUp() {
        reviewerService = new ReviewerServiceImpl(
                blogLikeRepository,
                blogRepository,
                blogSaveRepository,
                blogShareRepository,
                commentRepository,
                adminPayoutRepository,
                incomeRepository,
                reviewerBadgeHistoryRepository,
                reviewerRepository,
                roleRepository,
                userRoleAssignmentRepository,
                userRepository,
                userFollowRepository,
                userValidator,
                formulaService,
                badgeThresholdService,
                snapshotService,
                incomeService);

        firstUser = user("an", "Nguyen Van An", region("Can Tho", "Can Tho", "An Khanh", "Ninh Kieu", "30/4"));
        firstUser.setUserFollower(40);
        secondUser = user("binh", "Tran Thi Binh", null);
        secondUser.setUserFollower(5);
        firstReviewer = reviewer(firstUser);
        secondReviewer = reviewer(secondUser);
    }

    // ---------------------------------------------------------------- CRUD

    @Test
    void createReviewer_success_reusesExistingRoleAndReviewer_TC001() {
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.of(role("REVIEWER")));
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(firstUser.getUserId(), "REVIEWER"))
                .thenReturn(true);
        when(reviewerRepository.findByUserUserId(firstUser.getUserId())).thenReturn(Optional.of(firstReviewer));
        when(reviewerRepository.save(firstReviewer)).thenReturn(firstReviewer);
        when(reviewerBadgeHistoryRepository.findTopByReviewerReviewerIdOrderByMonthDesc(firstReviewer.getReviewerId()))
                .thenReturn(Optional.empty());
        when(userFollowRepository.findByFollowerUserId(firstUser.getUserId())).thenReturn(List.of());

        ReviewerResponseDTO result = reviewerService.createReviewer(firstUser.getUserId());

        assertThat(result.getReviewerId()).isEqualTo(firstReviewer.getReviewerId());
        assertThat(result.getRole()).isEqualTo("REVIEWER");
        assertThat(result.getBadge()).isEqualTo(ReviewerBadge.IRON);
        assertThat(result.getScore()).isZero();
        assertThat(result.getIsFollowing()).isFalse();
        verify(userRoleAssignmentRepository, never()).save(any(UserRoleAssignment.class));
        verify(snapshotService).initSnapshotForNewReviewer(firstReviewer);
    }

    @Test
    void createReviewer_success_createsRoleAndAssignmentWhenMissing_TC002() {
        Role created = role("REVIEWER");
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(created);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(firstUser.getUserId(), "REVIEWER"))
                .thenReturn(false);
        when(reviewerRepository.findByUserUserId(firstUser.getUserId())).thenReturn(Optional.empty());
        when(reviewerRepository.save(any(Reviewer.class))).thenAnswer(invocation -> {
            Reviewer saved = invocation.getArgument(0);
            saved.setReviewerId(UUID.randomUUID());
            return saved;
        });
        when(reviewerBadgeHistoryRepository.findTopByReviewerReviewerIdOrderByMonthDesc(any(UUID.class)))
                .thenReturn(Optional.empty());
        when(userFollowRepository.findByFollowerUserId(firstUser.getUserId())).thenReturn(List.of());

        ReviewerResponseDTO result = reviewerService.createReviewer(firstUser.getUserId());

        assertThat(result.getUserId()).isEqualTo(firstUser.getUserId());
        assertThat(result.getRegion()).isNotNull();
        assertThat(result.getRegion().getCity()).isEqualTo("Can Tho");
        verify(roleRepository).save(any(Role.class));
        verify(userRoleAssignmentRepository).save(any(UserRoleAssignment.class));
    }

    @Test
    void getReviewer_success_mapsLatestBadgeAndFollowCounts_TC003() {
        ReviewerBadgeHistory latest = badgeHistory(firstReviewer, "2026-07", 900, ReviewerBadge.GOLD);
        when(reviewerRepository.findByUserUserId(firstUser.getUserId())).thenReturn(Optional.of(firstReviewer));
        when(reviewerBadgeHistoryRepository.findTopByReviewerReviewerIdOrderByMonthDesc(firstReviewer.getReviewerId()))
                .thenReturn(Optional.of(latest));
        when(userFollowRepository.findByFollowerUserId(firstUser.getUserId()))
                .thenReturn(List.of(new UserFollow(), new UserFollow()));

        ReviewerResponseDTO result = reviewerService.getReviewer(firstUser.getUserId());

        assertThat(result.getBadge()).isEqualTo(ReviewerBadge.GOLD);
        assertThat(result.getScore()).isEqualTo(900);
        assertThat(result.getFollow()).isEqualTo(2);
        assertThat(result.getFollower()).isEqualTo(40);
        assertThat(result.getName()).isEqualTo("Nguyen Van An");
    }

    @Test
    void getReviewer_fail_notFound_TC004() {
        UUID userId = UUID.randomUUID();
        when(reviewerRepository.findByUserUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewerService.getReviewer(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer not found");
    }

    @Test
    void getAllReviewer_success_fallsBackToUserNameAndNullRegion_TC005() {
        secondUser.setUserFullName("   ");
        secondUser.setUserLike(null);
        secondUser.setUserFollower(null);
        when(reviewerRepository.findAll()).thenReturn(List.of(secondReviewer));
        when(reviewerBadgeHistoryRepository.findTopByReviewerReviewerIdOrderByMonthDesc(secondReviewer.getReviewerId()))
                .thenReturn(Optional.empty());
        when(userFollowRepository.findByFollowerUserId(secondUser.getUserId())).thenReturn(List.of());

        List<ReviewerResponseDTO> result = reviewerService.getAllReviewer(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("binh");
        assertThat(result.get(0).getRegion()).isNull();
        assertThat(result.get(0).getLike()).isZero();
        assertThat(result.get(0).getFollower()).isZero();
    }

    @Test
    void getAllActiveReviewers_success_marksViewerFollowingState_TC006() {
        UUID viewerId = UUID.randomUUID();
        when(reviewerRepository.findAllActiveReviewers()).thenReturn(List.of(firstReviewer));
        when(reviewerBadgeHistoryRepository.findTopByReviewerReviewerIdOrderByMonthDesc(firstReviewer.getReviewerId()))
                .thenReturn(Optional.empty());
        when(userFollowRepository.findByFollowerUserId(firstUser.getUserId())).thenReturn(List.of());
        when(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(viewerId, firstUser.getUserId()))
                .thenReturn(true);

        List<ReviewerResponseDTO> result = reviewerService.getAllActiveReviewers(viewerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsFollowing()).isTrue();
    }

    @Test
    void searchReviewers_success_trimsQuery_TC007() {
        when(reviewerRepository.searchActiveReviewers("an")).thenReturn(List.of(firstReviewer));
        when(reviewerBadgeHistoryRepository.findTopByReviewerReviewerIdOrderByMonthDesc(firstReviewer.getReviewerId()))
                .thenReturn(Optional.empty());
        when(userFollowRepository.findByFollowerUserId(firstUser.getUserId())).thenReturn(List.of());

        List<ReviewerResponseDTO> result = reviewerService.searchReviewers("  an  ", null);

        assertThat(result).hasSize(1);
        verify(reviewerRepository).searchActiveReviewers("an");
    }

    @Test
    void searchReviewers_success_blankOrNullQueryReturnsEmpty_TC008() {
        assertThat(reviewerService.searchReviewers(null, null)).isEmpty();
        assertThat(reviewerService.searchReviewers("   ", null)).isEmpty();

        verify(reviewerRepository, never()).searchActiveReviewers(any());
    }

    // --------------------------------------------------------------- Stats

    @Test
    void countReviewerStats_success_coversEveryPeriod_TC009() {
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(reviewerRepository.findById(firstReviewer.getReviewerId())).thenReturn(Optional.of(firstReviewer));
        when(blogLikeRepository.countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any())).thenReturn(10L);
        when(blogShareRepository.countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any())).thenReturn(2L);
        when(commentRepository.countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any())).thenReturn(3L);
        when(formulaService.calculateScore(10L, 2L, 3L)).thenReturn(31L);

        for (String period : List.of("day", "WEEK", "month", "3months")) {
            ReviewerStatsResponseDTO stats = reviewerService.countReviewerStats(
                    firstUser.getUserId(), firstReviewer.getReviewerId(), period);

            assertThat(stats.getPeriod()).isEqualTo(period.toLowerCase());
            assertThat(stats.getScore()).isEqualTo(31L);
            assertThat(stats.getLikeCount()).isEqualTo(10L);
        }
    }

    @Test
    void countReviewerStats_success_noReceivedEngagementReturnsZero_TC009A() {
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(reviewerRepository.findById(firstReviewer.getReviewerId())).thenReturn(Optional.of(firstReviewer));
        when(formulaService.calculateScore(0L, 0L, 0L)).thenReturn(0L);

        ReviewerStatsResponseDTO stats = reviewerService.countReviewerStats(
                firstUser.getUserId(), firstReviewer.getReviewerId(), "month");

        assertThat(stats.getLikeCount()).isZero();
        assertThat(stats.getShareCount()).isZero();
        assertThat(stats.getCommentCount()).isZero();
        assertThat(stats.getScore()).isZero();
        verify(blogLikeRepository).countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any());
        verify(blogShareRepository).countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any());
        verify(commentRepository).countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any());
        verify(blogLikeRepository, never())
                .countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any(), any());
    }

    @Test
    void countReviewerStats_fail_invalidPeriod_TC010() {
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(reviewerRepository.findById(firstReviewer.getReviewerId())).thenReturn(Optional.of(firstReviewer));

        assertThatThrownBy(() -> reviewerService.countReviewerStats(
                firstUser.getUserId(), firstReviewer.getReviewerId(), "decade"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid period");
    }

    @Test
    void countReviewerStats_fail_nullPeriod_TC011() {
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(reviewerRepository.findById(firstReviewer.getReviewerId())).thenReturn(Optional.of(firstReviewer));

        assertThatThrownBy(() -> reviewerService.countReviewerStats(
                firstUser.getUserId(), firstReviewer.getReviewerId(), null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid period");
    }

    @Test
    void countReviewerStatsByDateRange_fail_reviewerNotFound_TC012() {
        UUID reviewerId = UUID.randomUUID();
        when(reviewerRepository.findById(reviewerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewerService.countReviewerStatsByDateRange(
                reviewerId, LocalDateTime.now().minusDays(1), LocalDateTime.now()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer not found");
    }

    @Test
    void countReviewerStatsByDateRange_success_marksPeriodCustom_TC013() {
        when(reviewerRepository.findById(firstReviewer.getReviewerId())).thenReturn(Optional.of(firstReviewer));
        when(blogLikeRepository.countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any())).thenReturn(1L);
        when(blogShareRepository.countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any())).thenReturn(0L);
        when(commentRepository.countByBlogAuthorUserIdBetween(
                eq(firstUser.getUserId()), any(), any())).thenReturn(0L);
        when(formulaService.calculateScore(1L, 0L, 0L)).thenReturn(1L);

        ReviewerStatsResponseDTO stats = reviewerService.countReviewerStatsByDateRange(
                firstReviewer.getReviewerId(), LocalDateTime.now().minusDays(1), LocalDateTime.now());

        assertThat(stats.getPeriod()).isEqualTo("custom");
        assertThat(stats.getScore()).isEqualTo(1L);
    }

    // ------------------------------------------------------------- Ranking

    @Test
    void getReviewerRanking_success_sortsByScoreAndAssignsRank_TC014() {
        mockEngagement();

        List<ReviewerRankingResponseDTO> result =
                reviewerService.getReviewerRanking("month", 1, 20, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReviewerId()).isEqualTo(firstReviewer.getReviewerId());
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(0).getScore()).isEqualTo(23L);
        assertThat(result.get(0).getLocation()).isEqualTo("Can Tho");
        assertThat(result.get(1).getReviewerId()).isEqualTo(secondReviewer.getReviewerId());
        assertThat(result.get(1).getRank()).isEqualTo(2);
        assertThat(result.get(1).getScore()).isEqualTo(17L);
        assertThat(result.get(1).getLocation()).isEqualTo("unknown");
    }

    @Test
    void getReviewerRanking_success_sanitizesPageAndLimit_TC015() {
        mockEngagement();

        List<ReviewerRankingResponseDTO> firstPage =
                reviewerService.getReviewerRanking("day", 0, 1, null, null, null);
        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).getReviewerId()).isEqualTo(firstReviewer.getReviewerId());

        List<ReviewerRankingResponseDTO> secondPage =
                reviewerService.getReviewerRanking("day", 2, 1, null, null, null);
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getReviewerId()).isEqualTo(secondReviewer.getReviewerId());

        List<ReviewerRankingResponseDTO> beyondEnd =
                reviewerService.getReviewerRanking("day", 9, 1, null, null, null);
        assertThat(beyondEnd).isEmpty();

        List<ReviewerRankingResponseDTO> cappedLimit =
                reviewerService.getReviewerRanking("day", 1, 500, null, null, null);
        assertThat(cappedLimit).hasSize(2);
    }

    @Test
    void getReviewerRanking_success_filtersByLocation_TC016() {
        mockEngagement();

        List<ReviewerRankingResponseDTO> byCity =
                reviewerService.getReviewerRanking("week", 1, 20, "can tho", null, null);
        assertThat(byCity).hasSize(1);
        assertThat(byCity.get(0).getReviewerId()).isEqualTo(firstReviewer.getReviewerId());

        List<ReviewerRankingResponseDTO> byUnknownArea =
                reviewerService.getReviewerRanking("week", 1, 20, null, null, "unknown");
        assertThat(byUnknownArea).hasSize(1);
        assertThat(byUnknownArea.get(0).getReviewerId()).isEqualTo(secondReviewer.getReviewerId());

        List<ReviewerRankingResponseDTO> blankFilter =
                reviewerService.getReviewerRanking("week", 1, 20, "  ", "  ", "  ");
        assertThat(blankFilter).hasSize(2);
    }

    @Test
    void getReviewerRanking_success_breaksTiesByCommentShareLikeThenId_TC017() {
        // Cùng điểm 15: reviewer đầu hơn ở số bình luận nên phải đứng trước.
        when(formulaService.getActiveFormula()).thenReturn(formula());
        when(reviewerRepository.findAllWithUserAndRegion()).thenReturn(List.of(firstReviewer, secondReviewer));
        when(blogLikeRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstUser.getUserId(), 0L),
                interaction(secondUser.getUserId(), 15L)));
        when(blogShareRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of());
        when(commentRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstUser.getUserId(), 3L)));

        List<ReviewerRankingResponseDTO> result =
                reviewerService.getReviewerRanking("3months", 1, 20, null, null, null);

        assertThat(result).extracting(ReviewerRankingResponseDTO::getReviewerId)
                .containsExactly(firstReviewer.getReviewerId(), secondReviewer.getReviewerId());
    }

    // ------------------------------------------------------- Score & badge

    @Test
    void calculateReviewerScore_success_delegatesToFormula_TC018() {
        ReviewerStatsResponseDTO stats = new ReviewerStatsResponseDTO(
                firstReviewer.getReviewerId(), "day", 4L, 2L, 1L, 0L);
        when(formulaService.calculateScore(4L, 2L, 1L)).thenReturn(15L);

        assertThat(reviewerService.calculateReviewerScore(stats)).isEqualTo(15L);
    }

    @Test
    void calculateReviewerPayout_success_delegatesToFormula_TC019() {
        ReviewerStatsResponseDTO stats = new ReviewerStatsResponseDTO(
                firstReviewer.getReviewerId(), "day", 4L, 2L, 1L, 0L);
        when(formulaService.calculatePayout(4L, 2L, 1L)).thenReturn(1500L);

        assertThat(reviewerService.calculateReviewerPayout(stats)).isEqualTo(1500L);
    }

    @Test
    void calculateReviewerBadge_success_delegatesToThresholdService_TC020() {
        when(badgeThresholdService.badgeForScore(800L)).thenReturn(ReviewerBadge.GOLD);

        assertThat(reviewerService.calculateReviewerBadge(800L)).isEqualTo("GOLD");
    }

    @Test
    void calculateReviewerSegment_success_coversEveryBoundary_TC021() {
        assertThat(reviewerService.calculateReviewerSegment(0)).isEqualTo("inactive");
        assertThat(reviewerService.calculateReviewerSegment(99)).isEqualTo("new");
        assertThat(reviewerService.calculateReviewerSegment(100)).isEqualTo("active");
        assertThat(reviewerService.calculateReviewerSegment(299)).isEqualTo("active");
        assertThat(reviewerService.calculateReviewerSegment(300)).isEqualTo("strong");
        assertThat(reviewerService.calculateReviewerSegment(699)).isEqualTo("strong");
        assertThat(reviewerService.calculateReviewerSegment(700)).isEqualTo("top");
        assertThat(reviewerService.calculateReviewerSegment(1499)).isEqualTo("top");
        assertThat(reviewerService.calculateReviewerSegment(1500)).isEqualTo("elite");
    }

    // ------------------------------------------------------ Monthly badges

    @Test
    void generateMonthlyBadges_success_savesBadgeForEveryReviewer_TC022() {
        mockAdmin(firstUser.getUserId());
        mockEngagement();
        when(reviewerBadgeHistoryRepository.findByMonth("2026-07")).thenReturn(List.of());
        when(badgeThresholdService.badgeForScore(anyLong())).thenReturn(ReviewerBadge.BRONZE);
        when(reviewerBadgeHistoryRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<ReviewerBadgeHistory> saved = invocation.getArgument(0);
                    saved.forEach(history -> history.setId(UUID.randomUUID()));
                    return saved;
                });

        List<ReviewerBadgeResponseDTO> result =
                reviewerService.generateMonthlyBadges(firstUser.getUserId(), "2026-07", false);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(badge -> {
            assertThat(badge.getMonth()).isEqualTo("2026-07");
            assertThat(badge.getBadge()).isEqualTo(ReviewerBadge.BRONZE);
        });
    }

    @Test
    void generateMonthlyBadges_fail_duplicateWithoutOverwrite_TC023() {
        mockAdmin(firstUser.getUserId());
        mockEngagement();
        when(reviewerBadgeHistoryRepository.findByMonth("2026-07"))
                .thenReturn(List.of(badgeHistory(firstReviewer, "2026-07", 10, ReviewerBadge.IRON)));

        assertThatThrownBy(() -> reviewerService.generateMonthlyBadges(firstUser.getUserId(), "2026-07", false))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Duplicate badge generation");

        verify(reviewerBadgeHistoryRepository, never()).saveAll(anyList());
    }

    @Test
    void generateMonthlyBadges_success_overwriteReusesExistingRow_TC024() {
        ReviewerBadgeHistory existing = badgeHistory(firstReviewer, "2026-07", 10, ReviewerBadge.IRON);
        existing.setId(UUID.randomUUID());
        mockAdmin(firstUser.getUserId());
        mockEngagement();
        when(reviewerBadgeHistoryRepository.findByMonth("2026-07")).thenReturn(List.of(existing));
        when(badgeThresholdService.badgeForScore(anyLong())).thenReturn(ReviewerBadge.SILVER);
        when(reviewerBadgeHistoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ReviewerBadgeResponseDTO> result =
                reviewerService.generateMonthlyBadges(firstUser.getUserId(), "2026-07", true);

        assertThat(result).hasSize(2);
        assertThat(existing.getBadge()).isEqualTo(ReviewerBadge.SILVER);
        assertThat(existing.getScore()).isEqualTo(23L);
    }

    @Test
    void generateMonthlyBadges_fail_requesterIsNotAdmin_TC025() {
        when(userValidator.validateUserExists(firstUser.getUserId())).thenReturn(firstUser);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(firstUser.getUserId(), "ADMIN"))
                .thenReturn(false);

        assertThatThrownBy(() -> reviewerService.generateMonthlyBadges(firstUser.getUserId(), "2026-07", false))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unauthorized access");
    }

    @Test
    void generateMonthlyBadges_fail_invalidMonth_TC026() {
        mockAdmin(firstUser.getUserId());

        assertThatThrownBy(() -> reviewerService.generateMonthlyBadges(firstUser.getUserId(), "07-2026", false))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid month");

        assertThatThrownBy(() -> reviewerService.generateMonthlyBadges(firstUser.getUserId(), null, false))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid month");
    }

    // ---------------------------------------------------- Earnings và income

    @Test
    void getReviewerEarnings_success_splitsCountsByMonth_TC027() {
        AdminPayout july = payout("2026-07", 1_000L, 1_200L);
        AdminPayout june = payout("2026-06", 500L, 500L);
        mockSelfAccess(firstUser, firstReviewer);
        when(incomeRepository.sumCountsByReviewerGroupByMonth(firstReviewer.getReviewerId()))
                .thenReturn(List.of(monthlyCounts(2026, 7, 10L, 2L, 3L)));
        when(formulaService.getActiveFormula()).thenReturn(formula());
        when(adminPayoutRepository.findByReviewerReviewerId(eq(firstReviewer.getReviewerId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(june, july)));

        List<ReviewerEarningsResponseDTO> result =
                reviewerService.getReviewerEarnings(firstUser.getUserId(), firstReviewer.getReviewerId());

        assertThat(result).hasSize(2);
        ReviewerEarningsResponseDTO newest = result.get(0);
        assertThat(newest.getPayoutMonth()).isEqualTo("2026-07");
        assertThat(newest.getLikeCount()).isEqualTo(10L);
        assertThat(newest.getLikeAmount()).isEqualTo(1_000L);
        assertThat(newest.getShareAmount()).isEqualTo(600L);
        assertThat(newest.getCommentAmount()).isEqualTo(1_500L);
        assertThat(newest.getTotalFinalAmount()).isEqualTo(1_200L);
        // Tháng 6 không có dòng reviewer_income nào nên phần bóc tách bằng 0.
        assertThat(result.get(1).getLikeCount()).isZero();
        assertThat(result.get(1).getCommentAmount()).isZero();
        assertThat(result.get(1).getTotalBaseAmount()).isEqualTo(500L);
    }

    @Test
    void getReviewerIncome_success_defaultsToCurrentMonth_TC028() {
        Page<ReviewerIncomeResponseDTO> page = new PageImpl<>(List.of());
        PageRequest pageable = PageRequest.of(0, 10);
        mockSelfAccess(firstUser, firstReviewer);
        when(incomeService.getIncomeByReviewer(
                firstReviewer.getReviewerId(), YearMonth.now().toString(), pageable)).thenReturn(page);

        assertThat(reviewerService.getReviewerIncome(
                firstUser.getUserId(), firstReviewer.getReviewerId(), "  ", pageable)).isSameAs(page);
        verify(incomeService).getIncomeByReviewer(
                firstReviewer.getReviewerId(), YearMonth.now().toString(), pageable);
    }

    @Test
    void getReviewerIncome_success_usesRequestedMonth_TC029() {
        Page<ReviewerIncomeResponseDTO> page = new PageImpl<>(List.of());
        PageRequest pageable = PageRequest.of(0, 10);
        mockSelfAccess(firstUser, firstReviewer);
        when(incomeService.getIncomeByReviewer(firstReviewer.getReviewerId(), "2026-05", pageable))
                .thenReturn(page);

        assertThat(reviewerService.getReviewerIncome(
                firstUser.getUserId(), firstReviewer.getReviewerId(), "2026-05", pageable)).isSameAs(page);
    }

    @Test
    void getReviewerBadgeHistory_success_mapsEveryRow_TC030() {
        mockSelfAccess(firstUser, firstReviewer);
        when(reviewerBadgeHistoryRepository.findByReviewerReviewerIdOrderByMonthDesc(firstReviewer.getReviewerId()))
                .thenReturn(List.of(badgeHistory(firstReviewer, "2026-07", 320, ReviewerBadge.SILVER)));

        List<ReviewerBadgeResponseDTO> result =
                reviewerService.getReviewerBadgeHistory(firstUser.getUserId(), firstReviewer.getReviewerId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScore()).isEqualTo(320L);
        assertThat(result.get(0).getBadge()).isEqualTo(ReviewerBadge.SILVER);
    }

    // ----------------------------------------------------------- Quyền hạn

    @Test
    void validateSelfOrAdmin_success_adminReadsOtherReviewer_TC031() {
        User admin = user("admin", "Quan tri", null);
        when(userValidator.validateUserExists(admin.getUserId())).thenReturn(admin);
        when(reviewerRepository.findById(firstReviewer.getReviewerId())).thenReturn(Optional.of(firstReviewer));
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(admin.getUserId(), "ADMIN"))
                .thenReturn(true);
        when(reviewerBadgeHistoryRepository.findByReviewerReviewerIdOrderByMonthDesc(firstReviewer.getReviewerId()))
                .thenReturn(List.of());

        assertThat(reviewerService.getReviewerBadgeHistory(admin.getUserId(), firstReviewer.getReviewerId()))
                .isEmpty();
    }

    @Test
    void validateSelfOrAdmin_fail_otherUserWithoutAdminRole_TC032() {
        User stranger = user("khach", "Nguoi la", null);
        when(userValidator.validateUserExists(stranger.getUserId())).thenReturn(stranger);
        when(reviewerRepository.findById(firstReviewer.getReviewerId())).thenReturn(Optional.of(firstReviewer));
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(stranger.getUserId(), "ADMIN"))
                .thenReturn(false);

        assertThatThrownBy(() -> reviewerService.getReviewerBadgeHistory(
                stranger.getUserId(), firstReviewer.getReviewerId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unauthorized access");
    }

    // ---------------------------------------------------- Segment và geo

    @Test
    void getReviewersBySegment_success_filtersBySegment_TC033() {
        mockEngagement();

        List<ReviewerSegmentResponseDTO> result = reviewerService.getReviewersBySegment("2026-07", "NEW");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(response -> response.getSegment().equals("new"));
    }

    @Test
    void getReviewersBySegment_fail_invalidSegment_TC034() {
        assertThatThrownBy(() -> reviewerService.getReviewersBySegment("2026-07", "legend"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid segment");

        assertThatThrownBy(() -> reviewerService.getReviewersBySegment("2026-07", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid segment");
    }

    @Test
    void getGeoAnalytics_success_groupsByCityWithUnknownBucket_TC035() {
        mockEngagement();

        List<ReviewerGeoAnalyticsResponseDTO> result = reviewerService.getGeoAnalytics("month", "CITY");

        assertThat(result).hasSize(2);
        ReviewerGeoAnalyticsResponseDTO canTho = result.stream()
                .filter(response -> response.getLocationName().equals("Can Tho"))
                .findFirst()
                .orElseThrow();
        assertThat(canTho.getGroupBy()).isEqualTo("city");
        assertThat(canTho.getReviewerCount()).isEqualTo(1);
        assertThat(canTho.getTotalLikes()).isEqualTo(10L);
        assertThat(canTho.getTotalScore()).isEqualTo(23L);
        assertThat(canTho.getAverageScore()).isEqualTo(23.0);
        assertThat(canTho.getTopReviewer()).isEqualTo(firstReviewer.getReviewerId());
        assertThat(result).anyMatch(response -> response.getLocationName().equals("unknown"));
    }

    @Test
    void getGeoAnalytics_success_groupsByProvinceAndArea_TC036() {
        mockEngagement();
        assertThat(reviewerService.getGeoAnalytics("month", "province"))
                .extracting(ReviewerGeoAnalyticsResponseDTO::getLocationName)
                .containsExactlyInAnyOrder("Can Tho", "unknown");

        mockEngagement();
        assertThat(reviewerService.getGeoAnalytics("month", "area"))
                .extracting(ReviewerGeoAnalyticsResponseDTO::getLocationName)
                .containsExactlyInAnyOrder("Ninh Kieu", "unknown");
    }

    @Test
    void getGeoAnalytics_fail_invalidGroupBy_TC037() {
        assertThatThrownBy(() -> reviewerService.getGeoAnalytics("month", "district"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid groupBy");

        assertThatThrownBy(() -> reviewerService.getGeoAnalytics("month", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid groupBy");
    }

    // ------------------------------------------------------------ Discovery

    @Test
    void getTopReviewers_success_ranksByLifetimeReputation_TC038() {
        mockScoreCards();

        List<ReviewerDiscoveryResponseDTO> result = reviewerService.getTopReviewers(null, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReviewerId()).isEqualTo(firstReviewer.getReviewerId());
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(0).getBadge()).isEqualTo(ReviewerBadge.GOLD);
        assertThat(result.get(0).getBadgeLevel()).isEqualTo(4);
        assertThat(result.get(0).getBadgeScore()).isEqualTo(45.0);
        assertThat(result.get(0).getFollowerCount()).isEqualTo(40L);
        assertThat(result.get(0).getCity()).isEqualTo("Can Tho");
        assertThat(result.get(0).getRankingScore()).isGreaterThan(result.get(1).getRankingScore());
        assertThat(result.get(1).getCity()).isNull();
        assertThat(result.get(1).getBadge()).isEqualTo(ReviewerBadge.IRON);
        assertThat(result.get(1).isMe()).isFalse();
        assertThat(result.get(1).isFollowing()).isFalse();
    }

    @Test
    void getTopReviewers_success_sanitizesPagingAndMarksViewer_TC039() {
        mockScoreCards();
        when(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(
                firstUser.getUserId(), secondUser.getUserId())).thenReturn(true);

        List<ReviewerDiscoveryResponseDTO> result =
                reviewerService.getTopReviewers(firstUser.getUserId(), -3, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isMe()).isTrue();

        List<ReviewerDiscoveryResponseDTO> secondPage =
                reviewerService.getTopReviewers(firstUser.getUserId(), 1, 1);
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getRank()).isEqualTo(2);
        assertThat(secondPage.get(0).isFollowing()).isTrue();
    }

    @Test
    void getReviewersInRegion_success_keepsOnlyMatchingRegion_TC040() {
        mockScoreCards();

        List<ReviewerDiscoveryResponseDTO> result = reviewerService.getReviewersInRegion(
                null, "can tho", "can tho", "an khanh", "ninh kieu", "30/4", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReviewerId()).isEqualTo(firstReviewer.getReviewerId());
        assertThat(result.get(0).getWard()).isEqualTo("An Khanh");
        assertThat(result.get(0).getStreet()).isEqualTo("30/4");
        assertThat(result.get(0).getRankingScore()).isPositive();
    }

    @Test
    void getReviewersInRegion_success_noRegionMatchReturnsEmpty_TC041() {
        mockScoreCards();

        assertThat(reviewerService.getReviewersInRegion(
                null, "Ha Noi", null, null, null, null, 0, 10)).isEmpty();
    }

    @Test
    void getTrendingReviewers_success_coversEveryWindow_TC042() {
        for (String window : new String[] {null, "  ", "DAY_7", "week", "DAY_30", "month"}) {
            mockScoreCards();

            List<ReviewerDiscoveryResponseDTO> result =
                    reviewerService.getTrendingReviewers(null, window, 0, 10);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReviewerId()).isEqualTo(firstReviewer.getReviewerId());
        }
    }

    @Test
    void getTrendingReviewers_fail_invalidWindow_TC043() {
        assertThatThrownBy(() -> reviewerService.getTrendingReviewers(null, "DAY_90", 0, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid reviewer trending window");
    }

    @Test
    void getTopReviewers_success_noReviewerReturnsEmpty_TC044() {
        when(reviewerRepository.findAllWithUserAndRegion()).thenReturn(List.of());

        assertThat(reviewerService.getTopReviewers(null, 0, 10)).isEmpty();
    }

    @Test
    void getTopReviewers_success_skipsInactiveAndUserlessReviewers_TC045() {
        Reviewer withoutUser = new Reviewer();
        withoutUser.setReviewerId(UUID.randomUUID());
        secondUser.setAccountStatus(false);
        when(reviewerRepository.findAllWithUserAndRegion()).thenReturn(List.of(withoutUser, secondReviewer));
        when(reviewerBadgeHistoryRepository
                .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(anyList()))
                .thenReturn(List.of());

        assertThat(reviewerService.getTopReviewers(null, 0, 10)).isEmpty();
    }

    @Test
    void getTopReviewers_success_ignoresBadgeRowWithoutReviewer_TC046() {
        ReviewerBadgeHistory orphan = new ReviewerBadgeHistory();
        orphan.setBadge(ReviewerBadge.DIAMOND);
        when(reviewerRepository.findAllWithUserAndRegion()).thenReturn(List.of(firstReviewer));
        when(reviewerBadgeHistoryRepository
                .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(anyList()))
                .thenReturn(List.of(orphan));
        when(blogRepository.countPublishedByAuthor(any(), any()))
                .thenReturn(List.of(engagement(firstUser.getUserId(), 4L, 2L)));
        when(blogLikeRepository.countByBlogAuthor(any(), any())).thenReturn(List.of());
        when(blogShareRepository.countByBlogAuthor(any(), any())).thenReturn(List.of());
        when(commentRepository.countByBlogAuthor(any(), any())).thenReturn(List.of());
        when(blogSaveRepository.countByBlogAuthor(any(), any())).thenReturn(List.of());
        when(blogRepository.findPublishedActiveMonthsByAuthor()).thenReturn(List.of());

        List<ReviewerDiscoveryResponseDTO> result = reviewerService.getTopReviewers(null, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBadge()).isEqualTo(ReviewerBadge.IRON);
        assertThat(result.get(0).getBadgeLevel()).isEqualTo(1);
    }

    // ------------------------------------------------------------- Helpers

    private void mockEngagement() {
        when(formulaService.getActiveFormula()).thenReturn(formula());
        when(reviewerRepository.findAllWithUserAndRegion()).thenReturn(List.of(firstReviewer, secondReviewer));
        when(blogLikeRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstUser.getUserId(), 10L),
                interaction(secondUser.getUserId(), 2L),
                // Tác giả không phải reviewer: phải bị bỏ qua, không làm hỏng phép gom.
                interaction(UUID.randomUUID(), 99L)));
        when(blogShareRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstUser.getUserId(), 1L),
                interaction(secondUser.getUserId(), 5L)));
        when(commentRepository.countByBlogAuthorBetween(any(), any())).thenReturn(List.of(
                interaction(firstUser.getUserId(), 2L)));
    }

    private void mockScoreCards() {
        when(reviewerRepository.findAllWithUserAndRegion())
                .thenReturn(List.of(firstReviewer, secondReviewer));
        when(reviewerBadgeHistoryRepository
                .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(anyList()))
                .thenReturn(List.of(
                        badgeHistory(firstReviewer, "2026-07", 900, ReviewerBadge.GOLD),
                        badgeHistory(firstReviewer, "2026-06", 500, ReviewerBadge.SILVER)));
        when(blogRepository.countPublishedByAuthor(any(), any())).thenReturn(List.of(
                engagement(firstUser.getUserId(), 20L, 6L),
                engagement(secondUser.getUserId(), 3L, 1L)));
        when(blogLikeRepository.countByBlogAuthor(any(), any())).thenReturn(List.of(
                engagement(firstUser.getUserId(), 120L, 30L),
                engagement(secondUser.getUserId(), 4L, 2L)));
        when(blogShareRepository.countByBlogAuthor(any(), any())).thenReturn(List.of(
                engagement(firstUser.getUserId(), 15L, 4L)));
        when(commentRepository.countByBlogAuthor(any(), any())).thenReturn(List.of(
                engagement(firstUser.getUserId(), 30L, 9L),
                // Tác giả lạ: không có accumulator nên phải bị bỏ qua.
                engagement(UUID.randomUUID(), 500L, 500L)));
        when(blogSaveRepository.countByBlogAuthor(any(), any())).thenReturn(List.of(
                engagement(firstUser.getUserId(), null, null)));
        when(blogRepository.findPublishedActiveMonthsByAuthor()).thenReturn(List.of(
                activeMonth(firstUser.getUserId(), 2026, 6),
                activeMonth(firstUser.getUserId(), 2026, 7),
                activeMonth(firstUser.getUserId(), null, 7),
                activeMonth(UUID.randomUUID(), 2026, 7)));
    }

    private void mockAdmin(UUID adminUserId) {
        when(userValidator.validateUserExists(adminUserId)).thenReturn(firstUser);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(adminUserId, "ADMIN")).thenReturn(true);
    }

    private void mockSelfAccess(User owner, Reviewer ownedReviewer) {
        when(userValidator.validateUserExists(owner.getUserId())).thenReturn(owner);
        when(reviewerRepository.findById(ownedReviewer.getReviewerId())).thenReturn(Optional.of(ownedReviewer));
    }

    private ReviewerFormula formula() {
        ReviewerFormula formula = new ReviewerFormula();
        formula.setLikeWeight(1);
        formula.setShareWeight(3);
        formula.setCommentWeight(5);
        formula.setLikePayoutAmount(100L);
        formula.setSharePayoutAmount(300L);
        formula.setCommentPayoutAmount(500L);
        return formula;
    }

    private User user(String userName, String fullName, Region region) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(userName);
        user.setUserFullName(fullName);
        user.setUserEmail(userName + "@example.com");
        user.setAccountStatus(true);
        user.setUserAvatar("https://cdn.example.com/" + userName + ".png");
        user.setUserLike(12);
        user.setUserFollower(0);
        user.setRegion(region);
        return user;
    }

    private Region region(String city, String province, String ward, String area, String street) {
        Region newRegion = new Region();
        newRegion.setRegionId(UUID.randomUUID());
        newRegion.setCity(city);
        newRegion.setProvince(province);
        newRegion.setWard(ward);
        newRegion.setArea(area);
        newRegion.setStreet(street);
        return newRegion;
    }

    private Reviewer reviewer(User owner) {
        Reviewer newReviewer = new Reviewer();
        newReviewer.setReviewerId(UUID.randomUUID());
        newReviewer.setUser(owner);
        newReviewer.setReviewerActive(true);
        newReviewer.setReviewerExpiresAt(LocalDateTime.now().plusMonths(1));
        return newReviewer;
    }

    private Role role(String name) {
        Role newRole = new Role();
        newRole.setId(1);
        newRole.setName(name);
        return newRole;
    }

    private ReviewerBadgeHistory badgeHistory(Reviewer owner, String month, long score, ReviewerBadge badge) {
        ReviewerBadgeHistory history = new ReviewerBadgeHistory();
        history.setReviewer(owner);
        history.setMonth(month);
        history.setScore(score);
        history.setBadge(badge);
        history.setLikeCount(1);
        history.setShareCount(2);
        history.setCommentCount(3);
        return history;
    }

    private AdminPayout payout(String month, long baseAmount, long finalAmount) {
        AdminPayout adminPayout = new AdminPayout();
        adminPayout.setId(UUID.randomUUID());
        adminPayout.setReviewer(firstReviewer);
        adminPayout.setPayoutMonth(month);
        adminPayout.setTotalBaseAmount(baseAmount);
        adminPayout.setTotalFinalAmount(finalAmount);
        adminPayout.setBadge(ReviewerBadge.BRONZE);
        adminPayout.setBadgeMultiplier(new BigDecimal("1.20"));
        adminPayout.setStatus(AdminPayoutStatus.PAID);
        adminPayout.setPaidAt(LocalDateTime.now());
        return adminPayout;
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

    private AuthorEngagementCountRow engagement(UUID authorUserId, Long total, Long recent) {
        return new AuthorEngagementCountRow() {
            @Override
            public UUID getAuthorUserId() {
                return authorUserId;
            }

            @Override
            public Long getTotalCount() {
                return total;
            }

            @Override
            public Long getRecentCount() {
                return recent;
            }
        };
    }

    private BlogRepository.AuthorActiveMonthRow activeMonth(UUID authorUserId, Integer year, Integer month) {
        return new BlogRepository.AuthorActiveMonthRow() {
            @Override
            public UUID getAuthorUserId() {
                return authorUserId;
            }

            @Override
            public Integer getActiveYear() {
                return year;
            }

            @Override
            public Integer getActiveMonth() {
                return month;
            }
        };
    }

    private ReviewerIncomeRepository.ReviewerMonthlyCountRow monthlyCounts(
            int year, int month, long likeCount, long shareCount, long commentCount) {
        return new ReviewerIncomeRepository.ReviewerMonthlyCountRow() {
            @Override
            public Integer getYear() {
                return year;
            }

            @Override
            public Integer getMonth() {
                return month;
            }

            @Override
            public Long getLikeCount() {
                return likeCount;
            }

            @Override
            public Long getShareCount() {
                return shareCount;
            }

            @Override
            public Long getCommentCount() {
                return commentCount;
            }
        };
    }
}
