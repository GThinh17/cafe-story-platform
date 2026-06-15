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
import com.cafestory.repository.ContentReportRepository;
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
    private UserValidator userValidator;

    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationServiceImpl(
                userRepository,
                reviewerRepository,
                cafePageRepository,
                contentReportRepository,
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
        when(userRepository.findRecommendationCandidates(eq(currentUserId), any(Pageable.class)))
                .thenReturn(List.of(otherCity, sameCity));
        when(contentReportRepository.countByReportedUserUserIdAndStatusIn(any(UUID.class), eq(activeStatuses())))
                .thenReturn(0L);

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
        when(reviewerRepository.findRecommendationCandidates(eq(currentUserId), any(Pageable.class)))
                .thenReturn(List.of(reviewer));
        when(contentReportRepository.countByReportedUserUserIdAndStatusIn(reviewerUser.getUserId(), activeStatuses()))
                .thenReturn(0L);

        List<RecommendationCardResponseDTO> result =
                recommendationService.getReviewerRecommendations(currentUserId, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetType()).isEqualTo(RecommendationTargetType.REVIEWER);
        assertThat(result.get(0).getTargetId()).isEqualTo(reviewer.getReviewerId());
        assertThat(result.get(0).getUsername()).isEqualTo("coffeehunter");
        assertThat(result.get(0).getFullName()).isEqualTo("Coffee Hunter");
        assertThat(result.get(0).getReason()).isEqualTo("Reviewer near you");
    }

    @Test
    void getCafePageRecommendations_success_mapsCafePageCard_TC003() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = user(currentUserId, "current", "Current User", "Ho Chi Minh");
        CafePage cafePage = cafePage(UUID.randomUUID(), "Cafe Story Nguyen Hue", "Ho Chi Minh");
        cafePage.setFollowerCount(100);
        cafePage.setLikeCount(50);

        when(userValidator.validateUserExists(currentUserId)).thenReturn(currentUser);
        when(cafePageRepository.findRecommendationCandidates(eq(currentUserId), any(Pageable.class)))
                .thenReturn(List.of(cafePage));
        when(contentReportRepository.countByCafePageIdAndStatusIn(cafePage.getId(), activeStatuses()))
                .thenReturn(0L);

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
        when(userRepository.findRecommendationCandidates(eq(currentUserId), any(Pageable.class)))
                .thenReturn(List.of(suggestedUser));
        when(reviewerRepository.findRecommendationCandidates(eq(currentUserId), any(Pageable.class)))
                .thenReturn(List.of(reviewer));
        when(cafePageRepository.findRecommendationCandidates(eq(currentUserId), any(Pageable.class)))
                .thenReturn(List.of(cafePage));
        when(contentReportRepository.countByReportedUserUserIdAndStatusIn(any(UUID.class), eq(activeStatuses())))
                .thenReturn(0L);
        when(contentReportRepository.countByCafePageIdAndStatusIn(any(UUID.class), eq(activeStatuses())))
                .thenReturn(0L);

        List<RecommendationCardResponseDTO> result =
                recommendationService.getMixedRecommendations(currentUserId, 0, 10);

        assertThat(result).extracting(RecommendationCardResponseDTO::getTargetType)
                .containsExactly(
                        RecommendationTargetType.CAFE_PAGE,
                        RecommendationTargetType.USER,
                        RecommendationTargetType.REVIEWER);
    }

    private List<ReportStatus> activeStatuses() {
        return List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);
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
