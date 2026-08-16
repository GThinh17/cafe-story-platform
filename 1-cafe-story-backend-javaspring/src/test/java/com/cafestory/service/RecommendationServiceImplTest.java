package com.cafestory.service;

import com.cafestory.dto.responseDTO.RecommendationCardResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.RecommendationTargetType;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.ReviewerBadgeHistoryRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.RecommendationServiceImpl;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewerRepository reviewerRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private ContentReportRepository contentReportRepository;

    @Mock
    private ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private PageFollowRepository pageFollowRepository;

    @Mock
    private UserValidator userValidator;

    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationServiceImpl(
                userRepository,
                reviewerRepository,
                cafePageRepository,
                contentReportRepository,
                reviewerBadgeHistoryRepository,
                userFollowRepository,
                pageFollowRepository,
                userValidator);
    }

    @Test
    void getUserRecommendations_success_prioritizesSameCityAndMapsCard_TC001() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        User sameCity = user(UUID.randomUUID(), "minhanh", "Nguyen Minh Anh", "Ho Chi Minh");
        sameCity.setUserFollower(8);
        User otherCity = user(UUID.randomUUID(), "danang", "Da Nang User", "Da Nang");
        otherCity.setUserFollower(50);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(userRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(otherCity, sameCity));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getUserRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTargetType()).isEqualTo(RecommendationTargetType.USER);
        assertThat(result.get(0).getTargetId()).isEqualTo(sameCity.getUserId());
        assertThat(result.get(0).getAvatar()).isEqualTo(sameCity.getUserAvatar());
        assertThat(result.get(0).getUsername()).isEqualTo("minhanh");
        assertThat(result.get(0).getFullName()).isEqualTo("Nguyen Minh Anh");
        assertThat(result.get(0).getCity()).isEqualTo("Ho Chi Minh");
        assertThat(result.get(0).getReason()).isEqualTo("In your area");
    }

    @Test
    void getReviewerRecommendations_success_mapsReviewerCard_TC002() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        User reviewerUser = user(UUID.randomUUID(), "coffeehunter", "Coffee Hunter", "Ho Chi Minh");
        Reviewer reviewer = reviewer(UUID.randomUUID(), reviewerUser);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(reviewerRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(reviewer));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getReviewerRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetType()).isEqualTo(RecommendationTargetType.REVIEWER);
        assertThat(result.get(0).getTargetId()).isEqualTo(reviewer.getReviewerId());
        assertThat(result.get(0).getUsername()).isEqualTo("coffeehunter");
        assertThat(result.get(0).getFullName()).isEqualTo("Coffee Hunter");
        assertThat(result.get(0).getReason()).isEqualTo("Reviewer near you");
    }

    /**
     * Badge và trạng thái follow được gắn bằng query batch trên đúng trang kết
     * quả. Trước đây DTO không mang hai trường này nên client phải tự đi hỏi
     * thêm; còn các endpoint explore cũ thì hỏi một lần cho MỖI bản ghi.
     */
    @Test
    void getReviewerRecommendations_success_attachesBadgeAndFollowState_TC015() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        User reviewerUser = user(UUID.randomUUID(), "coffeehunter", "Coffee Hunter", "Ho Chi Minh");
        Reviewer reviewer = reviewer(UUID.randomUUID(), reviewerUser);

        ReviewerBadgeHistory latest = new ReviewerBadgeHistory();
        latest.setReviewer(reviewer);
        latest.setMonth("2026-08");
        latest.setBadge(ReviewerBadge.GOLD);
        ReviewerBadgeHistory older = new ReviewerBadgeHistory();
        older.setReviewer(reviewer);
        older.setMonth("2026-07");
        older.setBadge(ReviewerBadge.IRON);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(reviewerRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(reviewer));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());
        // Repository trả theo (reviewerId, month desc) nên bản ghi đầu là mới nhất.
        when(reviewerBadgeHistoryRepository
                .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(List.of(reviewer.getReviewerId())))
                .thenReturn(List.of(latest, older));
        when(userFollowRepository.findFollowedUserIds(currentUserId, List.of(reviewerUser.getUserId())))
                .thenReturn(List.of(reviewerUser.getUserId()));

        List<RecommendationCardResponseDTO> result =
                recommendationService.getReviewerRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBadge()).isEqualTo(ReviewerBadge.GOLD);
        assertThat(result.get(0).isFollowing()).isTrue();
    }

    @Test
    void getCafePageRecommendations_success_attachesFollowStateWithoutBadge_TC016() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        CafePage cafePage = cafePage(UUID.randomUUID(), "Cafe Story Nguyen Hue", "Ho Chi Minh");

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(cafePageRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(cafePage));
        when(contentReportRepository.countByCafePageIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());
        when(pageFollowRepository.findFollowedCafePageIds(currentUserId, List.of(cafePage.getId())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getCafePageRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isFollowing()).isFalse();
        // Cafe page không có badge — trường này chỉ dành cho REVIEWER.
        assertThat(result.get(0).getBadge()).isNull();
    }

    @Test
    void getCafePageRecommendations_success_mapsCafePageCard_TC003() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        CafePage cafePage = cafePage(UUID.randomUUID(), "Cafe Story Nguyen Hue", "Ho Chi Minh");
        cafePage.setFollowerCount(100);
        cafePage.setLikeCount(50);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(cafePageRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(cafePage));
        when(contentReportRepository.countByCafePageIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getCafePageRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetType()).isEqualTo(RecommendationTargetType.CAFE_PAGE);
        assertThat(result.get(0).getTargetId()).isEqualTo(cafePage.getId());
        assertThat(result.get(0).getUsername()).isEqualTo("Cafe Story Nguyen Hue");
        assertThat(result.get(0).getFullName()).isEqualTo("Cafe Story Nguyen Hue");
        assertThat(result.get(0).getCity()).isEqualTo("Ho Chi Minh");
        assertThat(result.get(0).getReason()).isEqualTo("Near your area");
    }

    @Test
    void getMixedRecommendations_success_interleavesThreeTargetTypes_TC004() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        User suggestedUser = user(UUID.randomUUID(), "minhanh", "Nguyen Minh Anh", "Ho Chi Minh");
        Reviewer reviewer = reviewer(UUID.randomUUID(), user(UUID.randomUUID(), "reviewer", "Reviewer User", "Ho Chi Minh"));
        CafePage cafePage = cafePage(UUID.randomUUID(), "Cafe Story", "Ho Chi Minh");

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(userRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(suggestedUser));
        when(reviewerRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(reviewer));
        when(cafePageRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(cafePage));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());
        when(contentReportRepository.countByCafePageIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getMixedRecommendations(currentUserId, 0, 10);

        assertThat(result).extracting(RecommendationCardResponseDTO::getTargetType)
                .containsExactly(
                        RecommendationTargetType.CAFE_PAGE,
                        RecommendationTargetType.USER,
                RecommendationTargetType.REVIEWER);
    }

    @Test
    void getReviewerRecommendations_success_appliesBatchReportPenalty_TC005() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        User cleanReviewerUser = user(UUID.randomUUID(), "cleanreviewer", "Clean Reviewer", "Da Nang");
        cleanReviewerUser.setUserFollower(5);
        User reportedReviewerUser = user(UUID.randomUUID(), "reportedreviewer", "Reported Reviewer", "Da Nang");
        reportedReviewerUser.setUserFollower(50);
        Reviewer cleanReviewer = reviewer(UUID.randomUUID(), cleanReviewerUser);
        Reviewer reportedReviewer = reviewer(UUID.randomUUID(), reportedReviewerUser);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(reviewerRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(reportedReviewer, cleanReviewer));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of(reportCount(reportedReviewerUser.getUserId(), 5)));

        List<RecommendationCardResponseDTO> result =
                recommendationService.getReviewerRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTargetId()).isEqualTo(cleanReviewer.getReviewerId());
        assertThat(result.get(1).getTargetId()).isEqualTo(reportedReviewer.getReviewerId());
    }

    @Test
    void getCafePageRecommendations_success_appliesBatchReportPenalty_TC006() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        CafePage cleanPage = cafePage(UUID.randomUUID(), "Clean Cafe", "Da Nang");
        cleanPage.setFollowerCount(20);
        CafePage reportedPage = cafePage(UUID.randomUUID(), "Reported Cafe", "Da Nang");
        reportedPage.setFollowerCount(100);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(cafePageRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(reportedPage, cleanPage));
        when(contentReportRepository.countByCafePageIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of(reportCount(reportedPage.getId(), 5)));

        List<RecommendationCardResponseDTO> result =
                recommendationService.getCafePageRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTargetId()).isEqualTo(cleanPage.getId());
        assertThat(result.get(1).getTargetId()).isEqualTo(reportedPage.getId());
    }

    @Test
    void getCafePageRecommendations_success_includesActiveDraftPageForExplore_TC007() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        CafePage draftPage = cafePage(UUID.randomUUID(), "Draft Paid Cafe", "Ho Chi Minh");
        draftPage.setStatus(PageStatus.DRAFT);
        draftPage.setPageActive(true);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(cafePageRepository.findRecommendationCandidates(eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(draftPage));
        when(contentReportRepository.countByCafePageIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getCafePageRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetType()).isEqualTo(RecommendationTargetType.CAFE_PAGE);
        assertThat(result.get(0).getTargetId()).isEqualTo(draftPage.getId());
        assertThat(result.get(0).getUsername()).isEqualTo("Draft Paid Cafe");
    }

    @Test
    void getUserRecommendations_success_noCandidateSkipsReportQuery_TC008() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(userRepository.findRecommendationCandidates(
                eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of());

        assertThat(recommendationService.getUserRecommendations(currentUserId, 0, 10)).isEmpty();

        verify(contentReportRepository, never()).countByReportedUserIdsAndStatusIn(any(), any());
    }

    @Test
    void getUserRecommendations_success_userWithoutRegionScoresZeroLocation_TC009() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        currentUser.setRegion(null);
        User candidate = user(UUID.randomUUID(), "khac", "Nguoi Khac", "Da Nang");
        candidate.setUserFollower(40);
        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(userRepository.findRecommendationCandidates(
                eq(currentUserId), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(), any(Pageable.class)))
                .thenReturn(List.of(candidate));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getUserRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReason()).isEqualTo("Popular in the CafeStory community");
    }

    @Test
    void getUserRecommendations_success_candidateWithoutRegionFallsBackToGenericReason_TC010() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        User candidate = user(UUID.randomUUID(), "khongvung", "Khong Vung", "Ha Noi");
        candidate.setRegion(null);
        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(userRepository.findRecommendationCandidates(
                eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(candidate));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getUserRecommendations(currentUserId, 0, 10);

        assertThat(result.get(0).getCity()).isNull();
        assertThat(result.get(0).getReason()).isEqualTo("Suggested for your CafeStory circle");
    }

    @Test
    void getReviewerRecommendations_success_inactiveReviewerLosesActiveBonus_TC011() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        User activeUser = user(UUID.randomUUID(), "active", "Active Reviewer", "Da Nang");
        Reviewer active = reviewer(UUID.randomUUID(), activeUser);
        User inactiveUser = user(UUID.randomUUID(), "inactive", "Inactive Reviewer", "Da Nang");
        Reviewer inactive = reviewer(UUID.randomUUID(), inactiveUser);
        inactive.setReviewerActive(false);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(reviewerRepository.findRecommendationCandidates(
                eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(inactive, active));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getReviewerRecommendations(currentUserId, 0, 10);

        assertThat(result).extracting(RecommendationCardResponseDTO::getTargetId)
                .containsExactly(active.getReviewerId(), inactive.getReviewerId());
        assertThat(result.get(0).getReason()).isEqualTo("Active reviewer on CafeStory");
    }

    @Test
    void getCafePageRecommendations_success_inactivePageLosesActiveBonus_TC012() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        CafePage activePage = cafePage(UUID.randomUUID(), "Quan dang hoat dong", "Da Nang");
        CafePage inactivePage = cafePage(UUID.randomUUID(), "Quan tam dung", "Da Nang");
        inactivePage.setPageActive(false);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(cafePageRepository.findRecommendationCandidates(
                eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(inactivePage, activePage));
        when(contentReportRepository.countByCafePageIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        List<RecommendationCardResponseDTO> result =
                recommendationService.getCafePageRecommendations(currentUserId, 0, 10);

        assertThat(result).extracting(RecommendationCardResponseDTO::getTargetId)
                .containsExactly(activePage.getId(), inactivePage.getId());
        assertThat(result.get(0).getReason()).isEqualTo("Active cafe page on CafeStory");
    }

    @Test
    void getUserRecommendations_success_sameRegionIdScoresHigherThanSameCity_TC013() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        User sameRegion = user(UUID.randomUUID(), "cungvung", "Cung Vung", "Ho Chi Minh");
        sameRegion.setRegion(currentUser.getRegion());
        User sameCityOnly = user(UUID.randomUUID(), "cungthanh", "Cung Thanh Pho", "Ho Chi Minh");

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(userRepository.findRecommendationCandidates(
                eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(sameCityOnly, sameRegion));
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        assertThat(recommendationService.getUserRecommendations(currentUserId, 0, 10))
                .extracting(RecommendationCardResponseDTO::getTargetId)
                .containsExactly(sameRegion.getUserId(), sameCityOnly.getUserId());
    }

    @Test
    void getMixedRecommendations_success_pageBeyondResultSetIsEmpty_TC014() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(userRepository.findRecommendationCandidates(
                eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of(user(UUID.randomUUID(), "a", "A", "Ho Chi Minh")));
        when(reviewerRepository.findRecommendationCandidates(
                eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of());
        when(cafePageRepository.findRecommendationCandidates(
                eq(currentUserId), any(UUID.class), eq("ho chi minh"), any(Pageable.class)))
                .thenReturn(List.of());
        when(contentReportRepository.countByReportedUserIdsAndStatusIn(any(), eq(activeStatuses())))
                .thenReturn(List.of());

        assertThat(recommendationService.getMixedRecommendations(currentUserId, 5, 10)).isEmpty();
        assertThat(recommendationService.getMixedRecommendations(currentUserId, -1, 0)).hasSize(1);
    }

    private List<ReportStatus> activeStatuses() {
        return List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);
    }

    private ContentReportRepository.ReportCountRow reportCount(UUID targetId, long reportCount) {
        return new ContentReportRepository.ReportCountRow() {
            @Override
            public UUID getTargetId() {
                return targetId;
            }

            @Override
            public long getReportCount() {
                return reportCount;
            }
        };
    }

    private User user(UUID userId, String username, String fullName, String city) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName(username);
        user.setUserFullName(fullName);
        user.setUserEmail(username + "@example.com");
        user.setUserPassword("password");
        user.setUserAvatar("https://example.com/" + username + ".png");
        user.setUserLike(0);
        user.setUserFollower(0);
        user.setAccountStatus(true);
        user.setRegion(region(city));
        return user;
    }

    private Reviewer reviewer(UUID reviewerId, User user) {
        Reviewer reviewer = new Reviewer();
        reviewer.setReviewerId(reviewerId);
        reviewer.setUser(user);
        reviewer.setReviewerActive(true);
        return reviewer;
    }

    private CafePage cafePage(UUID pageId, String name, String city) {
        CafePage cafePage = new CafePage();
        cafePage.setId(pageId);
        cafePage.setName(name);
        cafePage.setAvatarUrl("https://example.com/page.png");
        cafePage.setStatus(PageStatus.ACTIVE);
        cafePage.setPageActive(true);
        cafePage.setFollowerCount(0);
        cafePage.setLikeCount(0);
        cafePage.setRegion(region(city));
        cafePage.setOwner(user(UUID.randomUUID(), "owner", "Owner User", city));
        return cafePage;
    }

    private Region region(String city) {
        Region region = new Region();
        region.setRegionId(UUID.randomUUID());
        region.setCity(city);
        return region;
    }
}
