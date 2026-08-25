package com.cafestory.controller;

import com.cafestory.dto.responseDTO.FollowTargetResponseDTO;
import com.cafestory.dto.responseDTO.UserFollowResponseDTO;
import com.cafestory.entity.enums.FollowTargetFilter;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.service.serviceInterface.UserFollowService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFollowControllerTest {

    @Mock
    private UserFollowService userFollowService;

    @InjectMocks
    private UserFollowController userFollowController;

    @Test
    void followUser_success_TC001() {
        UUID followingUserId = UUID.randomUUID();
        UUID followerUserId = UUID.randomUUID();
        UserFollowResponseDTO response = response();

        when(userFollowService.followUser(followingUserId, followerUserId)).thenReturn(response);

        UserFollowResponseDTO result = userFollowController.followUser(followingUserId, principal(followerUserId));

        assertThat(result).isEqualTo(response);
        verify(userFollowService).followUser(followingUserId, followerUserId);
    }

    @Test
    void unfollowUser_success_TC002() {
        UUID followingUserId = UUID.randomUUID();
        UUID followerUserId = UUID.randomUUID();

        userFollowController.unfollowUser(followingUserId, principal(followerUserId));

        verify(userFollowService).unfollowUser(followingUserId, followerUserId);
    }

    @Test
    void getFollowersByUserId_success_TC003() {
        UUID userId = UUID.randomUUID();
        List<UserFollowResponseDTO> response = List.of(response());

        when(userFollowService.getFollowersByUserId(userId)).thenReturn(response);

        List<UserFollowResponseDTO> result = userFollowController.getFollowersByUserId(userId);

        assertThat(result).isEqualTo(response);
        verify(userFollowService).getFollowersByUserId(userId);
    }

    @Test
    void getFollowingByUserId_success_TC004() {
        UUID userId = UUID.randomUUID();
        List<UserFollowResponseDTO> response = List.of(response());

        when(userFollowService.getFollowingByUserId(userId)).thenReturn(response);

        List<UserFollowResponseDTO> result = userFollowController.getFollowingByUserId(userId);

        assertThat(result).isEqualTo(response);
        verify(userFollowService).getFollowingByUserId(userId);
    }

    @Test
    void getFollowingTargetsByUserId_success_TC005() {
        UUID userId = UUID.randomUUID();
        List<FollowTargetResponseDTO> response = List.of(followTargetResponse());

        when(userFollowService.getFollowingTargetsByUserId(userId, FollowTargetFilter.ALL)).thenReturn(response);

        List<FollowTargetResponseDTO> result = userFollowController.getFollowingTargetsByUserId(
                userId,
                FollowTargetFilter.ALL);

        assertThat(result).isEqualTo(response);
        verify(userFollowService).getFollowingTargetsByUserId(userId, FollowTargetFilter.ALL);
    }

    private UserFollowResponseDTO response() {
        UserFollowResponseDTO response = new UserFollowResponseDTO();
        response.setId(UUID.randomUUID());
        response.setFollowerUserId(UUID.randomUUID());
        response.setFollowingUserId(UUID.randomUUID());
        return response;
    }

    private FollowTargetResponseDTO followTargetResponse() {
        FollowTargetResponseDTO response = new FollowTargetResponseDTO();
        response.setFollowId(UUID.randomUUID());
        response.setTargetId(UUID.randomUUID());
        response.setTargetType(FollowTargetType.CAFE_PAGE);
        response.setCafePageId(response.getTargetId());
        response.setDisplayName("Cafe Story");
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
