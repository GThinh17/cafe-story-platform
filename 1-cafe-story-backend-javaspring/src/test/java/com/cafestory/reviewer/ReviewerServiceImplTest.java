package com.cafestory.reviewer;

import com.cafestory.entity.BlogLike;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerPayout;
import com.cafestory.entity.User;
import com.cafestory.dto.responseDTO.reviewer.ReviewerRankingResponseDTO;
import com.cafestory.entity.enums.PayoutStatus;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.ReviewerPayoutRepository;
import com.cafestory.repository.UserRepository;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerServiceImplTest {

    private final UUID reviewerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID secondReviewerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID thirdReviewerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
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
                userRepository,
                userValidator);
    }

    @Test
    void countReviewerStats_success_allPeriods_TC001() {
        User reviewer = user(reviewerId, UserRole.USER, "HCM", "HCM", "D1");
        when(userValidator.validateUserExists(reviewerId)).thenReturn(reviewer);
        when(blogLikeRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerId), any(), any()))
                .thenReturn(1L, 2L, 3L, 4L);
        when(blogShareRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerId), any(), any()))
                .thenReturn(5L, 6L, 7L, 8L);
        when(commentRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerId), any(), any()))
                .thenReturn(9L, 10L, 11L, 12L);

        assertStats("day", 1, 5, 9);
        assertStats("week", 2, 6, 10);
        assertStats("month", 3, 7, 11);
        assertStats("3months", 4, 8, 12);
    }

    @Test
    void countReviewerStatsByDateRange_success_countsAndFormulaMethods_TC009() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 1, 0, 0);
        when(userValidator.validateUserExists(reviewerId)).thenReturn(user(reviewerId, UserRole.USER, null, null, null));
        when(blogLikeRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(reviewerId, start, end))
                .thenReturn(7L);
        when(blogShareRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(reviewerId, start, end))
                .thenReturn(3L);
        when(commentRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(reviewerId, start, end))
                .thenReturn(2L);

        var stats = reviewerService.countReviewerStatsByDateRange(reviewerId, start, end);

        assertThat(stats.getPeriod()).isEqualTo("custom");
        assertThat(reviewerService.calculateReviewerScore(stats)).isEqualTo(26);
        assertThat(reviewerService.calculateReviewerPayout(stats)).isEqualTo(2600);
    }

    @Test
    void payout_success_formulaGenerateDuplicateAndOverwrite_TC002() {
        User admin = user(adminId, UserRole.ADMIN, null, null, null);
        User reviewer = user(reviewerId, UserRole.USER, null, null, null);
        when(userValidator.validateUserExists(adminId)).thenReturn(admin);
        when(userRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(like(reviewer), like(reviewer)));
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(share(reviewer)));
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(comment(reviewer), comment(reviewer), comment(reviewer)));
        when(reviewerPayoutRepository.existsByReviewerUserIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(false);
        when(reviewerPayoutRepository.findByReviewerUserIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(Optional.empty());
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

        when(reviewerPayoutRepository.existsByReviewerUserIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(true);
        assertThatThrownBy(() -> reviewerService.generateMonthlyPayouts(adminId, "2026-05", false))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        ReviewerPayout existing = new ReviewerPayout();
        existing.setId(UUID.randomUUID());
        when(reviewerPayoutRepository.findByReviewerUserIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(Optional.of(existing));

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

        User admin = user(adminId, UserRole.ADMIN, null, null, null);
        User reviewer = user(reviewerId, UserRole.USER, null, null, null);
        when(userValidator.validateUserExists(adminId)).thenReturn(admin);
        when(userRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of());
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of());
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(comment(reviewer), comment(reviewer), comment(reviewer), comment(reviewer), comment(reviewer),
                        comment(reviewer), comment(reviewer), comment(reviewer), comment(reviewer), comment(reviewer),
                        comment(reviewer), comment(reviewer), comment(reviewer), comment(reviewer), comment(reviewer),
                        comment(reviewer), comment(reviewer), comment(reviewer), comment(reviewer), comment(reviewer)));
        when(reviewerBadgeHistoryRepository.existsByReviewerUserIdAndMonth(reviewerId, "2026-05")).thenReturn(false);
        when(reviewerBadgeHistoryRepository.findByReviewerUserIdAndMonth(reviewerId, "2026-05")).thenReturn(Optional.empty());
        mockBadgeSave();

        var generated = reviewerService.generateMonthlyBadges(adminId, "2026-05", false);

        assertThat(generated).hasSize(1);
        assertThat(generated.get(0).getScore()).isEqualTo(100);
        assertThat(generated.get(0).getBadge()).isEqualTo(ReviewerBadge.BRONZE);

        when(reviewerBadgeHistoryRepository.existsByReviewerUserIdAndMonth(reviewerId, "2026-05")).thenReturn(true);
        assertThatThrownBy(() -> reviewerService.generateMonthlyBadges(adminId, "2026-05", false))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        ReviewerBadgeHistory existing = new ReviewerBadgeHistory();
        existing.setId(UUID.randomUUID());
        when(reviewerBadgeHistoryRepository.findByReviewerUserIdAndMonth(reviewerId, "2026-05")).thenReturn(Optional.of(existing));

        var overwritten = reviewerService.generateMonthlyBadges(adminId, "2026-05", true);

        assertThat(overwritten.get(0).getBadge()).isEqualTo(ReviewerBadge.BRONZE);
    }

    @Test
    void ranking_success_sortTieBreakerPaginationAndPeriods_TC004() {
        User first = user(reviewerId, UserRole.USER, "HCM", "HCM", "D1");
        User second = user(secondReviewerId, UserRole.USER, "HCM", "HCM", "D2");
        User third = user(thirdReviewerId, UserRole.USER, "HN", "HN", "Ba Dinh");
        mockRankingData(first, second, third);

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
        User first = user(reviewerId, UserRole.USER, "HCM", "HCM", "D1");
        User second = user(secondReviewerId, UserRole.USER, "HCM", "HCM", "D2");
        User third = user(thirdReviewerId, UserRole.USER, "HN", "HN", "Ba Dinh");
        User fourth = user(fourthReviewerId, UserRole.USER, "DN", "DN", "Hai Chau");
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(like(first), like(first), like(first), like(first), like(first),
                        like(second), like(second), like(second), like(second), like(second),
                        like(third), like(third), like(third), like(third), like(third),
                        like(fourth), like(fourth), like(fourth), like(fourth), like(fourth)));
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(share(second)));
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(comment(first), comment(third)));
        when(userRepository.findAll()).thenReturn(List.of(first, second, third, fourth));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(first));
        when(userRepository.findById(secondReviewerId)).thenReturn(Optional.of(second));
        when(userRepository.findById(thirdReviewerId)).thenReturn(Optional.of(third));
        when(userRepository.findById(fourthReviewerId)).thenReturn(Optional.of(fourth));

        var sanitized = reviewerService.getReviewerRanking("day", 0, 0, null, null, null);

        assertThat(sanitized).hasSize(1);
        assertThat(sanitized.get(0).getReviewerId()).isEqualTo(reviewerId);

        var capped = reviewerService.getReviewerRanking("day", 1, 500, null, null, null);

        assertThat(capped).extracting(ReviewerRankingResponseDTO::getReviewerId)
                .containsExactly(reviewerId, thirdReviewerId, secondReviewerId, fourthReviewerId);
    }

    @Test
    void geo_success_groupingUnknownFilteringAndTotals_TC005() {
        User first = user(reviewerId, UserRole.USER, "HCM", "HCM", "D1");
        User second = user(secondReviewerId, UserRole.USER, "HCM", "HCM", "D2");
        User unknown = user(thirdReviewerId, UserRole.USER, null, null, null);
        mockRankingData(first, second, unknown);

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

        User reviewer = user(reviewerId, UserRole.USER, null, null, null);
        when(userRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of(like(reviewer)));
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());

        var result = reviewerService.getReviewersBySegment("2026-05", "new");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSegment()).isEqualTo("new");
    }

    @Test
    void permission_successAndFailure_TC007() {
        User reviewer = user(reviewerId, UserRole.USER, null, null, null);
        User other = user(secondReviewerId, UserRole.USER, null, null, null);
        User admin = user(adminId, UserRole.ADMIN, null, null, null);
        when(userValidator.validateUserExists(reviewerId)).thenReturn(reviewer);
        when(userValidator.validateUserExists(secondReviewerId)).thenReturn(other);
        when(userValidator.validateUserExists(adminId)).thenReturn(admin);

        when(blogLikeRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerId), any(), any())).thenReturn(0L);
        when(blogShareRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerId), any(), any())).thenReturn(0L);
        when(commentRepository.countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(reviewerId), any(), any())).thenReturn(0L);

        assertThat(reviewerService.countReviewerStats(reviewerId, reviewerId, "day").getReviewerId()).isEqualTo(reviewerId);

        assertThatThrownBy(() -> reviewerService.getReviewerPayoutHistory(secondReviewerId, reviewerId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        ReviewerPayout payout = payout(reviewer);
        ReviewerBadgeHistory badge = badge(reviewer);
        when(reviewerPayoutRepository.findByReviewerUserIdOrderByPayoutMonthDesc(reviewerId)).thenReturn(List.of(payout));
        when(reviewerBadgeHistoryRepository.findByReviewerUserIdOrderByMonthDesc(reviewerId)).thenReturn(List.of(badge));
        assertThat(reviewerService.getReviewerPayoutHistory(reviewerId, reviewerId)).hasSize(1);
        assertThat(reviewerService.getReviewerPayoutHistory(adminId, reviewerId)).hasSize(1);
        assertThat(reviewerService.getReviewerBadgeHistory(reviewerId, reviewerId)).hasSize(1);
        assertThat(reviewerService.getReviewerBadgeHistory(adminId, reviewerId)).hasSize(1);
        assertThatThrownBy(() -> reviewerService.getReviewerBadgeHistory(secondReviewerId, reviewerId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        when(userRepository.findAll()).thenReturn(List.of(reviewer));
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(reviewerPayoutRepository.existsByReviewerUserIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(false);
        when(reviewerPayoutRepository.findByReviewerUserIdAndPayoutMonth(reviewerId, "2026-05")).thenReturn(Optional.empty());
        mockPayoutSave();
        when(reviewerBadgeHistoryRepository.existsByReviewerUserIdAndMonth(reviewerId, "2026-05")).thenReturn(false);
        when(reviewerBadgeHistoryRepository.findByReviewerUserIdAndMonth(reviewerId, "2026-05")).thenReturn(Optional.empty());
        mockBadgeSave();

        assertThat(reviewerService.generateMonthlyPayouts(adminId, "2026-05", false)).hasSize(1);
        assertThat(reviewerService.generateMonthlyBadges(adminId, "2026-05", false)).hasSize(1);

        assertThatThrownBy(() -> reviewerService.generateMonthlyPayouts(reviewerId, "2026-05", true))
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
        var result = reviewerService.countReviewerStats(reviewerId, reviewerId, period);
        assertThat(result.getPeriod()).isEqualTo(period);
        assertThat(result.getLikeCount()).isEqualTo(likes);
        assertThat(result.getShareCount()).isEqualTo(shares);
        assertThat(result.getCommentCount()).isEqualTo(comments);
        assertThat(result.getScore()).isEqualTo(likes + shares * 3 + comments * 5);
    }

    private void mockRankingData(User first, User second, User third) {
        when(blogLikeRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(like(first), like(first), like(first), like(first), like(first),
                        like(second), like(second),
                        like(third)));
        when(blogShareRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(share(first),
                        share(second), share(second), share(second)));
        when(commentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any()))
                .thenReturn(List.of(comment(first), comment(first),
                        comment(second), comment(second),
                        comment(third)));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(first));
        when(userRepository.findById(secondReviewerId)).thenReturn(Optional.of(second));
        when(userRepository.findById(thirdReviewerId)).thenReturn(Optional.of(third));
        when(userRepository.findAll()).thenReturn(List.of(first, second, third));
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

    private User user(UUID userId, UserRole role, String city, String province, String district) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("user-" + userId);
        user.setUserEmail(userId + "@example.com");
        user.setUserPassword("secret");
        user.setAccountStatus(true);
        user.setUserRole(role);
        user.setCity(city);
        user.setProvince(province);
        user.setDistrict(district);
        return user;
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

    private ReviewerPayout payout(User reviewer) {
        ReviewerPayout payout = new ReviewerPayout();
        payout.setId(UUID.randomUUID());
        payout.setReviewer(reviewer);
        payout.setPayoutMonth("2026-05");
        payout.setPayoutStatus(PayoutStatus.CALCULATED);
        return payout;
    }

    private ReviewerBadgeHistory badge(User reviewer) {
        ReviewerBadgeHistory badge = new ReviewerBadgeHistory();
        badge.setId(UUID.randomUUID());
        badge.setReviewer(reviewer);
        badge.setMonth("2026-05");
        badge.setBadge(ReviewerBadge.IRON);
        return badge;
    }
}
