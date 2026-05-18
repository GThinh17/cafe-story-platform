package com.cafestory.controller;

import com.cafestory.dto.requestDTO.UserFollowRequestDTO;
import com.cafestory.dto.responseDTO.UserFollowResponseDTO;
import com.cafestory.service.serviceInterface.UserFollowService;
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
        UserFollowRequestDTO request = request();
        UserFollowResponseDTO response = response();

        when(userFollowService.followUser(followingUserId, request.getFollowerUserId())).thenReturn(response);

        UserFollowResponseDTO result = userFollowController.followUser(followingUserId, request);

        assertThat(result).isEqualTo(response);
        verify(userFollowService).followUser(followingUserId, request.getFollowerUserId());
    }

    @Test
    void unfollowUser_success_TC002() {
        UUID followingUserId = UUID.randomUUID();
        UUID followerUserId = UUID.randomUUID();

        userFollowController.unfollowUser(followingUserId, followerUserId);

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

    private UserFollowRequestDTO request() {
        UserFollowRequestDTO request = new UserFollowRequestDTO();
        request.setFollowerUserId(UUID.randomUUID());
        return request;
    }

    private UserFollowResponseDTO response() {
        UserFollowResponseDTO response = new UserFollowResponseDTO();
        response.setId(UUID.randomUUID());
        response.setFollowerUserId(UUID.randomUUID());
        response.setFollowingUserId(UUID.randomUUID());
        return response;
    }
}
