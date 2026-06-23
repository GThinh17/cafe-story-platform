package com.cafestory.service;

import com.cafestory.dto.responseDTO.FollowTargetResponseDTO;
import com.cafestory.dto.responseDTO.UserFollowResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageFollow;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.UserFollow;
import com.cafestory.entity.enums.FollowTargetFilter;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.mapper.UserFollowMapper;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.service.serviceImplement.UserFollowServiceImpl;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceImplTest {

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private PageFollowRepository pageFollowRepository;

    @Mock
    private UserFollowMapper userFollowMapper;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserFollowServiceImpl userFollowService;

    @Test
    void followUser_success_TC001() {
        UUID followingUserId = UUID.randomUUID();
        UUID followerUserId = UUID.randomUUID();
        User followingUser = user(followingUserId);
        followingUser.setUserFollower(2);
        User followerUser = user(followerUserId);
        UserFollow savedFollow = userFollow(UUID.randomUUID(), followerUser, followingUser);
        UserFollowResponseDTO response = response(savedFollow.getId(), followerUserId, followingUserId);

        when(userValidator.validateUserExists(followingUserId)).thenReturn(followingUser);
        when(userValidator.validateUserExists(followerUserId)).thenReturn(followerUser);
        when(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId))
                .thenReturn(false);
        when(userFollowRepository.save(any(UserFollow.class))).thenReturn(savedFollow);
        when(userFollowMapper.toUserFollowResponseDTO(savedFollow)).thenReturn(response);

        UserFollowResponseDTO result = userFollowService.followUser(followingUserId, followerUserId);

        assertThat(result).isEqualTo(response);
        assertThat(followingUser.getUserFollower()).isEqualTo(3);
    }

    @Test
    void followUser_fail_duplicateFollow_TC002() {
        UUID followingUserId = UUID.randomUUID();
        UUID followerUserId = UUID.randomUUID();

        when(userValidator.validateUserExists(followingUserId)).thenReturn(user(followingUserId));
        when(userValidator.validateUserExists(followerUserId)).thenReturn(user(followerUserId));
        when(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId))
                .thenReturn(true);

        assertThatThrownBy(() -> userFollowService.followUser(followingUserId, followerUserId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User already followed"));

        verify(userFollowRepository, never()).save(any(UserFollow.class));
    }

    @Test
    void followUser_fail_selfFollow_TC003() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> userFollowService.followUser(userId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User cannot follow themself"));

        verify(userValidator, never()).validateUserExists(userId);
    }

    @Test
    void unfollowUser_success_TC004() {
        UUID followingUserId = UUID.randomUUID();
        UUID followerUserId = UUID.randomUUID();
        User followingUser = user(followingUserId);
        followingUser.setUserFollower(4);
        UserFollow userFollow = userFollow(UUID.randomUUID(), user(followerUserId), followingUser);

        when(userFollowRepository.findByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId))
                .thenReturn(Optional.of(userFollow));

        userFollowService.unfollowUser(followingUserId, followerUserId);

        assertThat(followingUser.getUserFollower()).isEqualTo(3);
        verify(userValidator).validateUserExists(followingUserId);
        verify(userValidator).validateUserExists(followerUserId);
        verify(userFollowRepository).delete(userFollow);
    }

    @Test
    void unfollowUser_fail_followNotFound_TC005() {
        UUID followingUserId = UUID.randomUUID();
        UUID followerUserId = UUID.randomUUID();

        when(userFollowRepository.findByFollowerUserIdAndFollowingUserId(followerUserId, followingUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userFollowService.unfollowUser(followingUserId, followerUserId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User follow not found"));
    }

    @Test
    void getFollowersByUserId_success_TC006() {
        UUID followingUserId = UUID.randomUUID();
        UserFollow userFollow = userFollow(UUID.randomUUID(), user(UUID.randomUUID()), user(followingUserId));
        UserFollowResponseDTO response = response(
                userFollow.getId(),
                userFollow.getFollower().getUserId(),
                followingUserId);

        when(userFollowRepository.findByFollowingUserId(followingUserId)).thenReturn(List.of(userFollow));
        when(userFollowMapper.toUserFollowResponseDTO(userFollow)).thenReturn(response);

        List<UserFollowResponseDTO> result = userFollowService.getFollowersByUserId(followingUserId);

        assertThat(result).containsExactly(response);
        verify(userValidator).validateUserExists(followingUserId);
    }

    @Test
    void getFollowingByUserId_success_TC007() {
        UUID followerUserId = UUID.randomUUID();
        UserFollow userFollow = userFollow(UUID.randomUUID(), user(followerUserId), user(UUID.randomUUID()));
        UserFollowResponseDTO response = response(
                userFollow.getId(),
                followerUserId,
                userFollow.getFollowing().getUserId());

        when(userFollowRepository.findByFollowerUserId(followerUserId)).thenReturn(List.of(userFollow));
        when(userFollowMapper.toUserFollowResponseDTO(userFollow)).thenReturn(response);

        List<UserFollowResponseDTO> result = userFollowService.getFollowingByUserId(followerUserId);

        assertThat(result).containsExactly(response);
        verify(userValidator).validateUserExists(followerUserId);
    }

    @Test
    void getFollowingTargetsByUserId_success_returnsUserAndCafePageSeparately_TC008() {
        UUID followerUserId = UUID.randomUUID();
        User follower = user(followerUserId);
        User followingUser = user(UUID.randomUUID());
        followingUser.setUserName("friend");
        followingUser.setUserFullName("Friend User");
        followingUser.setUserAvatar("https://cdn.example.com/friend.png");
        followingUser.setRegion(region("Ho Chi Minh"));
        UserFollow userFollow = userFollow(UUID.randomUUID(), follower, followingUser);
        userFollow.setCreatedAt(LocalDateTime.parse("2026-06-20T09:00:00"));

        CafePage cafePage = cafePage(UUID.randomUUID(), "Cafe Story Nguyen Hue");
        cafePage.setAvatarUrl("https://cdn.example.com/page.png");
        cafePage.setRegion(region("Da Nang"));
        cafePage.setOwner(user(UUID.randomUUID()));
        PageFollow pageFollow = pageFollow(UUID.randomUUID(), cafePage, follower);
        pageFollow.setCreatedAt(LocalDateTime.parse("2026-06-20T10:00:00"));

        when(userFollowRepository.findByFollowerUserId(followerUserId)).thenReturn(List.of(userFollow));
        when(pageFollowRepository.findByUserUserId(followerUserId)).thenReturn(List.of(pageFollow));

        List<FollowTargetResponseDTO> result = userFollowService.getFollowingTargetsByUserId(
                followerUserId,
                FollowTargetFilter.ALL);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTargetType()).isEqualTo(FollowTargetType.CAFE_PAGE);
        assertThat(result.get(0).getTargetId()).isEqualTo(cafePage.getId());
        assertThat(result.get(0).getCafePageId()).isEqualTo(cafePage.getId());
        assertThat(result.get(0).getUserId()).isNull();
        assertThat(result.get(0).getDisplayName()).isEqualTo("Cafe Story Nguyen Hue");
        assertThat(result.get(0).getCity()).isEqualTo("Da Nang");
        assertThat(result.get(0).getOwnerUserId()).isEqualTo(cafePage.getOwner().getUserId());

        assertThat(result.get(1).getTargetType()).isEqualTo(FollowTargetType.USER);
        assertThat(result.get(1).getTargetId()).isEqualTo(followingUser.getUserId());
        assertThat(result.get(1).getUserId()).isEqualTo(followingUser.getUserId());
        assertThat(result.get(1).getCafePageId()).isNull();
        assertThat(result.get(1).getDisplayName()).isEqualTo("Friend User");
        assertThat(result.get(1).getCity()).isEqualTo("Ho Chi Minh");
        verify(userValidator).validateUserExists(followerUserId);
    }

    @Test
    void getFollowingTargetsByUserId_success_filterCafePageOnly_TC009() {
        UUID followerUserId = UUID.randomUUID();
        CafePage cafePage = cafePage(UUID.randomUUID(), "Cafe Story");
        PageFollow pageFollow = pageFollow(UUID.randomUUID(), cafePage, user(followerUserId));

        when(pageFollowRepository.findByUserUserId(followerUserId)).thenReturn(List.of(pageFollow));

        List<FollowTargetResponseDTO> result = userFollowService.getFollowingTargetsByUserId(
                followerUserId,
                FollowTargetFilter.CAFE_PAGE);

        assertThat(result)
                .singleElement()
                .satisfies(target -> {
                    assertThat(target.getTargetType()).isEqualTo(FollowTargetType.CAFE_PAGE);
                    assertThat(target.getCafePageId()).isEqualTo(cafePage.getId());
                });
        verify(userFollowRepository, never()).findByFollowerUserId(followerUserId);
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("luan123");
        user.setUserPassword("123456");
        user.setUserEmail("luan@example.com");
        user.setAccountStatus(true);
        user.setUserFollower(0);
        return user;
    }

    private UserFollow userFollow(UUID id, User follower, User following) {
        UserFollow userFollow = new UserFollow();
        userFollow.setId(id);
        userFollow.setFollower(follower);
        userFollow.setFollowing(following);
        return userFollow;
    }

    private PageFollow pageFollow(UUID id, CafePage cafePage, User user) {
        PageFollow pageFollow = new PageFollow();
        pageFollow.setId(id);
        pageFollow.setCafePage(cafePage);
        pageFollow.setUser(user);
        return pageFollow;
    }

    private CafePage cafePage(UUID cafePageId, String name) {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setName(name);
        cafePage.setStatus(PageStatus.ACTIVE);
        cafePage.setPageActive(true);
        cafePage.setFollowerCount(0);
        cafePage.setLikeCount(0);
        return cafePage;
    }

    private Region region(String city) {
        Region region = new Region();
        region.setRegionId(UUID.randomUUID());
        region.setCity(city);
        return region;
    }

    private UserFollowResponseDTO response(UUID id, UUID followerUserId, UUID followingUserId) {
        UserFollowResponseDTO response = new UserFollowResponseDTO();
        response.setId(id);
        response.setFollowerUserId(followerUserId);
        response.setFollowingUserId(followingUserId);
        return response;
    }
}
