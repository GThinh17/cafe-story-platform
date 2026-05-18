package com.cafestory.reviewer;

import com.cafestory.entity.BlogLike;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.Comment;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerPayout;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.dto.responseDTO.reviewer.ReviewerRankingResponseDTO;
import com.cafestory.entity.enums.PayoutStatus;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerPayoutRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.service.serviceImplement.ReviewerServiceImpl;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerServiceImplTest {

    private final UUID reviewerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID secondReviewerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID thirdReviewerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID reviewerUserId = UUID.fromString("10101010-1010-1010-1010-101010101010");
    private final UUID secondReviewerUserId = UUID.fromString("20202020-2020-2020-2020-202020202020");
    private final UUID thirdReviewerUserId = UUID.fromString("30303030-3030-3030-3030-303030303030");
    private final UUID adminId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Mock
    private BlogLikeRepository blogLikeRepository;

    @Mock
    private BlogShareRepository blogShareRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewerPayoutRepository reviewerPayoutRepository;

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
    private UserValidator userValidator;

    private ReviewerServiceImpl reviewerService;

    @BeforeEach
    void setUp() {
        reviewerService = new ReviewerServiceImpl(
                blogLikeRepository,
                blogShareRepository,
                commentRepository,
                reviewerPayoutRepository,
                reviewerBadgeHistoryRepository,
                reviewerRepository,
                roleRepository,
                userRoleAssignmentRepository,
                userRepository,
                userValidator);
    }

    @Test
    void countReviewerStats_success_allPeriods_TC001() {
        User user = user(reviewerUserId, "HCM", "HCM", "D1");
        when(userValidator.validateUserExists(reviewerUserId)).thenReturn(user);
        when(reviewerRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer(reviewerId, user)));
        when(blogLikeRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerUserId), any(), any()))
                .thenReturn(1L, 2L, 3L, 4L);
        when(blogShareRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerUserId), any(), any()))
                .thenReturn(5L, 6L, 7L, 8L);
        when(commentRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerUserId), any(), any()))
                .thenReturn(9L, 10L, 11L, 12L);

        assertStats("day", 1, 5, 9);
        assertStats("week", 2, 6, 10);
        assertStats("month", 3, 7, 11);
        assertStats("3months", 4, 8, 12);
    }

    @Test
    void createReviewer_success_updatesUserRole_TC011() {
        User user = user(reviewerUserId, null, null, null);
        when(userValidator.validateUserExists(reviewerUserId)).thenReturn(user);
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.of(role(2, "REVIEWER")));
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(reviewerUserId, "REVIEWER")).thenReturn(false);
        when(reviewerRepository.findByUserUserId(reviewerUserId)).thenReturn(Optional.empty());
        mockReviewerSave();

        var result = reviewerService.createReviewer(reviewerUserId);

        assertThat(result.getReviewerId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(reviewerUserId);
        assertThat(result.getRole()).isEqualTo("REVIEWER");
        verify(userRoleAssignmentRepository).save(any());
        verify(reviewerRepository).save(any(Reviewer.class));
    }

    @Test
    void countReviewerStatsByDateRange_success_countsAndFormulaMethods_TC009() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 1, 0, 0);
        User user = user(reviewerUserId, null, null, null);
        when(reviewerRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer(reviewerId, user)));
        when(blogLikeRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(reviewerUserId, start, end))
                .thenReturn(7L);
        when(blogShareRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(reviewerUserId, start, end))
                .thenReturn(3L);
        when(commentRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(reviewerUserId, start, end))
                .thenReturn(2L);

        var stats = reviewerService.countReviewerStatsByDateRange(reviewerId, start, end);

        assertThat(stats.getPeriod()).isEqualTo("custom");
        assertThat(reviewerService.calculateReviewerScore(stats)).isEqualTo(26);
        assertThat(reviewerService.calculateReviewerPayout(stats)).isEqualTo(2600);
    }

    @Test
    void payout_success_formulaGenerateDuplicateAndOverwrite_TC002() {
        User admin = user(adminId, null, null, null);
        User reviewerUser = user(reviewerUserId, null, null, null);
        Reviewer reviewer = reviewer(reviewerId, reviewerUser);
        when(userValidator.validateUserExists(adminId)).thenReturn(admin);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(any(UUID.class), eq("ADMIN")))
                .thenAnswer(invocation -> adminId.equals(invocation.getArgument(0)));
        when(reviewerRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(like(reviewerUser), like(reviewerUser)));
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(share(reviewerUser)));
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(comment(reviewerUser), comment(reviewerUser), comment(reviewerUser)));
        when(reviewerPayoutRepository.existsByReviewerReviewerIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(false);
        when(reviewerPayoutRepository.findByReviewerReviewerIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(Optional.empty());
        mockPayoutSave();

        var generated = reviewerService.generateMonthlyPayouts(adminId, "2026-05", false);

        assertThat(generated).hasSize(1);
        assertThat(generated.get(0).getLikeCount()).isEqualTo(2);
        assertThat(generated.get(0).getShareCount()).isEqualTo(1);
        assertThat(generated.get(0).getCommentCount()).isEqualTo(3);
        assertThat(generated.get(0).getLikeAmount()).isEqualTo(200);
        assertThat(generated.get(0).getShareAmount()).isEqualTo(300);
        assertThat(generated.get(0).getCommentAmount()).isEqualTo(1500);
        assertThat(generated.get(0).getTotalAmount()).isEqualTo(2000);

        when(reviewerPayoutRepository.existsByReviewerReviewerIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(true);
        assertThatThrownBy(() -> reviewerService.generateMonthlyPayouts(adminId, "2026-05", false))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        ReviewerPayout existing = new ReviewerPayout();
        existing.setId(UUID.randomUUID());
        when(reviewerPayoutRepository.findByReviewerReviewerIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(Optional.of(existing));

        var overwritten = reviewerService.generateMonthlyPayouts(adminId, "2026-05", true);

        assertThat(overwritten).hasSize(1);
        assertThat(overwritten.get(0).getTotalAmount()).isEqualTo(2000);
    }

    @Test
    void badge_success_boundariesGenerateDuplicateAndOverwrite_TC003() {
        assertThat(reviewerService.calculateReviewerBadge(99)).isEqualTo("IRON");
        assertThat(reviewerService.calculateReviewerBadge(0)).isEqualTo("IRON");
        assertThat(reviewerService.calculateReviewerBadge(100)).isEqualTo("BRONZE");
        assertThat(reviewerService.calculateReviewerBadge(299)).isEqualTo("BRONZE");
        assertThat(reviewerService.calculateReviewerBadge(300)).isEqualTo("SILVER");
        assertThat(reviewerService.calculateReviewerBadge(699)).isEqualTo("SILVER");
        assertThat(reviewerService.calculateReviewerBadge(700)).isEqualTo("GOLD");
        assertThat(reviewerService.calculateReviewerBadge(1499)).isEqualTo("GOLD");
        assertThat(reviewerService.calculateReviewerBadge(1500)).isEqualTo("DIAMOND");

        User admin = user(adminId, null, null, null);
        User reviewerUser = user(reviewerUserId, null, null, null);
        Reviewer reviewer = reviewer(reviewerId, reviewerUser);
        when(userValidator.validateUserExists(adminId)).thenReturn(admin);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(any(UUID.class), eq("ADMIN")))
                .thenAnswer(invocation -> adminId.equals(invocation.getArgument(0)));
        when(reviewerRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of());
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of());
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(comment(reviewerUser), comment(reviewerUser), comment(reviewerUser), comment(reviewerUser), comment(reviewerUser),
                        comment(reviewerUser), comment(reviewerUser), comment(reviewerUser), comment(reviewerUser), comment(reviewerUser),
                        comment(reviewerUser), comment(reviewerUser), comment(reviewerUser), comment(reviewerUser), comment(reviewerUser),
                        comment(reviewerUser), comment(reviewerUser), comment(reviewerUser), comment(reviewerUser), comment(reviewerUser)));
        when(reviewerBadgeHistoryRepository.existsByReviewerReviewerIdAndMonth(reviewerId, "2026-05")).thenReturn(false);
        when(reviewerBadgeHistoryRepository.findByReviewerReviewerIdAndMonth(reviewerId, "2026-05")).thenReturn(Optional.empty());
        mockBadgeSave();

        var generated = reviewerService.generateMonthlyBadges(adminId, "2026-05", false);

        assertThat(generated).hasSize(1);
        assertThat(generated.get(0).getScore()).isEqualTo(100);
        assertThat(generated.get(0).getBadge()).isEqualTo(ReviewerBadge.BRONZE);

        when(reviewerBadgeHistoryRepository.existsByReviewerReviewerIdAndMonth(reviewerId, "2026-05")).thenReturn(true);
        assertThatThrownBy(() -> reviewerService.generateMonthlyBadges(adminId, "2026-05", false))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        ReviewerBadgeHistory existing = new ReviewerBadgeHistory();
        existing.setId(UUID.randomUUID());
        when(reviewerBadgeHistoryRepository.findByReviewerReviewerIdAndMonth(reviewerId, "2026-05")).thenReturn(Optional.of(existing));

        var overwritten = reviewerService.generateMonthlyBadges(adminId, "2026-05", true);

        assertThat(overwritten.get(0).getBadge()).isEqualTo(ReviewerBadge.BRONZE);
    }

    @Test
    void ranking_success_sortTieBreakerPaginationAndPeriods_TC004() {
        User first = user(reviewerUserId, "HCM", "HCM", "D1");
        User second = user(secondReviewerUserId, "HCM", "HCM", "D2");
        User third = user(thirdReviewerUserId, "HN", "HN", "Ba Dinh");
        mockRankingData(reviewer(reviewerId, first), reviewer(secondReviewerId, second), reviewer(thirdReviewerId, third));

        var ranking = reviewerService.getReviewerRanking("day", 1, 2, null, null, null);

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).getReviewerId()).isEqualTo(secondReviewerId);
        assertThat(ranking.get(0).getRank()).isEqualTo(1);
        assertThat(ranking.get(1).getReviewerId()).isEqualTo(reviewerId);

        var secondPage = reviewerService.getReviewerRanking("day", 2, 2, null, null, null);
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getReviewerId()).isEqualTo(thirdReviewerId);

        assertThat(reviewerService.getReviewerRanking("week", 1, 10, null, null, null)).hasSize(3);
        assertThat(reviewerService.getReviewerRanking("month", 1, 10, null, null, null)).hasSize(3);
        assertThat(reviewerService.getReviewerRanking("3months", 1, 10, null, null, null)).hasSize(3);
    }

    @Test
    void ranking_success_tieBreakersUuidAndPaginationSanitize_TC010() {
        UUID fourthReviewerId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID fourthReviewerUserId = UUID.fromString("40404040-4040-4040-4040-404040404040");
        User first = user(reviewerUserId, "HCM", "HCM", "D1");
        User second = user(secondReviewerUserId, "HCM", "HCM", "D2");
        User third = user(thirdReviewerUserId, "HN", "HN", "Ba Dinh");
        User fourth = user(fourthReviewerUserId, "DN", "DN", "Hai Chau");
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(like(first), like(first), like(first), like(first), like(first),
                        like(second), like(second), like(second), like(second), like(second),
                        like(third), like(third), like(third), like(third), like(third),
                        like(fourth), like(fourth), like(fourth), like(fourth), like(fourth)));
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(share(second)));
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(comment(first), comment(third)));
        when(reviewerRepository.findAll()).thenReturn(List.of(
                reviewer(reviewerId, first),
                reviewer(secondReviewerId, second),
                reviewer(thirdReviewerId, third),
                reviewer(fourthReviewerId, fourth)));

        var sanitized = reviewerService.getReviewerRanking("day", 0, 0, null, null, null);

        assertThat(sanitized).hasSize(1);
        assertThat(sanitized.get(0).getReviewerId()).isEqualTo(reviewerId);

        var capped = reviewerService.getReviewerRanking("day", 1, 500, null, null, null);

        assertThat(capped).extracting(ReviewerRankingResponseDTO::getReviewerId)
                .containsExactly(reviewerId, thirdReviewerId, secondReviewerId, fourthReviewerId);
    }

    @Test
    void geo_success_groupingUnknownFilteringAndTotals_TC005() {
        User first = user(reviewerUserId, "HCM", "HCM", "D1");
        User second = user(secondReviewerUserId, "HCM", "HCM", "D2");
        User unknown = user(thirdReviewerUserId, null, null, null);
        mockRankingData(reviewer(reviewerId, first), reviewer(secondReviewerId, second), reviewer(thirdReviewerId, unknown));

        var byCity = reviewerService.getGeoAnalytics("month", "city");

        assertThat(byCity).anySatisfy(group -> {
            assertThat(group.getLocationName()).isEqualTo("HCM");
            assertThat(group.getReviewerCount()).isEqualTo(2);
            assertThat(group.getTotalScore()).isEqualTo(39);
            assertThat(group.getAverageScore()).isEqualTo(19.5);
        });
        assertThat(byCity).anySatisfy(group -> assertThat(group.getLocationName()).isEqualTo("unknown"));

        assertThat(reviewerService.getGeoAnalytics("month", "province")).isNotEmpty();
        assertThat(reviewerService.getGeoAnalytics("month", "district")).isNotEmpty();

        var cityFiltered = reviewerService.getReviewerRanking("month", 1, 10, "hcm", null, null);
        assertThat(cityFiltered).hasSize(2);
        var provinceFiltered = reviewerService.getReviewerRanking("month", 1, 10, null, "hcm", null);
        assertThat(provinceFiltered).hasSize(2);
        var districtFiltered = reviewerService.getReviewerRanking("month", 1, 10, null, null, "d1");
        assertThat(districtFiltered).hasSize(1);
        var unknownFiltered = reviewerService.getReviewerRanking("month", 1, 10, "unknown", null, null);
        assertThat(unknownFiltered).hasSize(1);
        assertThat(unknownFiltered.get(0).getReviewerId()).isEqualTo(thirdReviewerId);
    }

    @Test
    void segments_success_allSegmentsAndGetBySegment_TC006() {
        assertThat(reviewerService.calculateReviewerSegment(0)).isEqualTo("inactive");
        assertThat(reviewerService.calculateReviewerSegment(1)).isEqualTo("new");
        assertThat(reviewerService.calculateReviewerSegment(99)).isEqualTo("new");
        assertThat(reviewerService.calculateReviewerSegment(100)).isEqualTo("active");
        assertThat(reviewerService.calculateReviewerSegment(299)).isEqualTo("active");
        assertThat(reviewerService.calculateReviewerSegment(300)).isEqualTo("strong");
        assertThat(reviewerService.calculateReviewerSegment(699)).isEqualTo("strong");
        assertThat(reviewerService.calculateReviewerSegment(700)).isEqualTo("top");
        assertThat(reviewerService.calculateReviewerSegment(1499)).isEqualTo("top");
        assertThat(reviewerService.calculateReviewerSegment(1500)).isEqualTo("elite");

        User reviewerUser = user(reviewerUserId, null, null, null);
        Reviewer reviewer = reviewer(reviewerId, reviewerUser);
        when(reviewerRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of(like(reviewerUser)));
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());

        var result = reviewerService.getReviewersBySegment("2026-05", "new");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSegment()).isEqualTo("new");
    }

    @Test
    void permission_successAndFailure_TC007() {
        User reviewerUser = user(reviewerUserId, null, null, null);
        Reviewer reviewer = reviewer(reviewerId, reviewerUser);
        User other = user(secondReviewerUserId, null, null, null);
        User admin = user(adminId, null, null, null);
        when(userValidator.validateUserExists(reviewerUserId)).thenReturn(reviewerUser);
        when(userValidator.validateUserExists(secondReviewerUserId)).thenReturn(other);
        when(userValidator.validateUserExists(adminId)).thenReturn(admin);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(any(UUID.class), eq("ADMIN")))
                .thenAnswer(invocation -> adminId.equals(invocation.getArgument(0)));
        when(reviewerRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));

        when(blogLikeRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerUserId), any(), any())).thenReturn(0L);
        when(blogShareRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerUserId), any(), any())).thenReturn(0L);
        when(commentRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerUserId), any(), any())).thenReturn(0L);

        assertThat(reviewerService.countReviewerStats(reviewerUserId, reviewerId, "day").getReviewerId()).isEqualTo(reviewerId);

        assertThatThrownBy(() -> reviewerService.getReviewerPayoutHistory(secondReviewerUserId, reviewerId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        ReviewerPayout payout = payout(reviewer);
        ReviewerBadgeHistory badge = badge(reviewer);
        when(reviewerPayoutRepository.findByReviewerReviewerIdOrderByPayoutMonthDesc(reviewerId)).thenReturn(List.of(payout));
        when(reviewerBadgeHistoryRepository.findByReviewerReviewerIdOrderByMonthDesc(reviewerId)).thenReturn(List.of(badge));
        assertThat(reviewerService.getReviewerPayoutHistory(reviewerUserId, reviewerId)).hasSize(1);
        assertThat(reviewerService.getReviewerPayoutHistory(adminId, reviewerId)).hasSize(1);
        assertThat(reviewerService.getReviewerBadgeHistory(reviewerUserId, reviewerId)).hasSize(1);
        assertThat(reviewerService.getReviewerBadgeHistory(adminId, reviewerId)).hasSize(1);
        assertThatThrownBy(() -> reviewerService.getReviewerBadgeHistory(secondReviewerUserId, reviewerId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        when(reviewerRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(reviewerPayoutRepository.existsByReviewerReviewerIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(false);
        when(reviewerPayoutRepository.findByReviewerReviewerIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(Optional.empty());
        mockPayoutSave();
        when(reviewerBadgeHistoryRepository.existsByReviewerReviewerIdAndMonth(reviewerId, "2026-05")).thenReturn(false);
        when(reviewerBadgeHistoryRepository.findByReviewerReviewerIdAndMonth(reviewerId, "2026-05")).thenReturn(Optional.empty());
        mockBadgeSave();

        assertThat(reviewerService.generateMonthlyPayouts(adminId, "2026-05", false)).hasSize(1);
        assertThat(reviewerService.generateMonthlyBadges(adminId, "2026-05", false)).hasSize(1);

        assertThatThrownBy(() -> reviewerService.generateMonthlyPayouts(reviewerUserId, "2026-05", true))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void validation_fail_invalidPeriodMonthGroupBy_TC008() {
        assertThatThrownBy(() -> reviewerService.getReviewerRanking("year", 1, 10, null, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> reviewerService.getReviewersBySegment("2026/05", "active"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> reviewerService.getReviewersBySegment("2026-05", "legend"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> reviewerService.getGeoAnalytics("month", "ward"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private void assertStats(String period, long likes, long shares, long comments) {
        var result = reviewerService.countReviewerStats(reviewerUserId, reviewerId, period);
        assertThat(result.getPeriod()).isEqualTo(period);
        assertThat(result.getLikeCount()).isEqualTo(likes);
        assertThat(result.getShareCount()).isEqualTo(shares);
        assertThat(result.getCommentCount()).isEqualTo(comments);
        assertThat(result.getScore()).isEqualTo(likes + shares * 3 + comments * 5);
    }

    private void mockRankingData(Reviewer first, Reviewer second, Reviewer third) {
        User firstUser = first.getUser();
        User secondUser = second.getUser();
        User thirdUser = third.getUser();
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(like(firstUser), like(firstUser), like(firstUser), like(firstUser), like(firstUser),
                        like(secondUser), like(secondUser),
                        like(thirdUser)));
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(share(firstUser),
                        share(secondUser), share(secondUser), share(secondUser)));
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(comment(firstUser), comment(firstUser),
                        comment(secondUser), comment(secondUser),
                        comment(thirdUser)));
        when(reviewerRepository.findAll()).thenReturn(List.of(first, second, third));
    }

    private BlogLike like(User user) {
        BlogLike like = new BlogLike();
        like.setId(UUID.randomUUID());
        like.setUser(user);
        like.setCreatedAt(LocalDateTime.now());
        return like;
    }

    private BlogShare share(User user) {
        BlogShare share = new BlogShare();
        share.setId(UUID.randomUUID());
        share.setUser(user);
        share.setCreatedAt(LocalDateTime.now());
        return share;
    }

    private Comment comment(User user) {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setContent("Review");
        return comment;
    }

    private User user(UUID userId, String city, String province, String district) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("user-" + userId);
        user.setUserEmail(userId + "@example.com");
        user.setUserPassword("secret");
        user.setAccountStatus(true);
        user.setCity(city);
        user.setProvince(province);
        user.setDistrict(district);
        return user;
    }

    private Reviewer reviewer(UUID reviewerId, User user) {
        Reviewer reviewer = new Reviewer();
        reviewer.setReviewerId(reviewerId);
        reviewer.setUser(user);
        return reviewer;
    }

    private Role role(Integer id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private void mockReviewerSave() {
        doAnswer(invocation -> {
            Reviewer reviewer = invocation.getArgument(0);
            if (reviewer.getReviewerId() == null) {
                reviewer.setReviewerId(UUID.randomUUID());
            }
            return reviewer;
        }).when(reviewerRepository).save(any(Reviewer.class));
    }

    private void mockPayoutSave() {
        doAnswer(invocation -> {
            ReviewerPayout payout = invocation.getArgument(0);
            if (payout.getId() == null) {
                payout.setId(UUID.randomUUID());
            }
            payout.setPayoutStatus(PayoutStatus.CALCULATED);
            return payout;
        }).when(reviewerPayoutRepository).save(any(ReviewerPayout.class));
    }

    private void mockBadgeSave() {
        doAnswer(invocation -> {
            ReviewerBadgeHistory badge = invocation.getArgument(0);
            if (badge.getId() == null) {
                badge.setId(UUID.randomUUID());
            }
            return badge;
        }).when(reviewerBadgeHistoryRepository).save(any(ReviewerBadgeHistory.class));
    }

    private ReviewerPayout payout(Reviewer reviewer) {
        ReviewerPayout payout = new ReviewerPayout();
        payout.setId(UUID.randomUUID());
        payout.setReviewer(reviewer);
        payout.setPayoutMonth("2026-05");
        payout.setPayoutStatus(PayoutStatus.CALCULATED);
        return payout;
    }

    private ReviewerBadgeHistory badge(Reviewer reviewer) {
        ReviewerBadgeHistory badge = new ReviewerBadgeHistory();
        badge.setId(UUID.randomUUID());
        badge.setReviewer(reviewer);
        badge.setMonth("2026-05");
        badge.setBadge(ReviewerBadge.IRON);
        return badge;
    }
}

