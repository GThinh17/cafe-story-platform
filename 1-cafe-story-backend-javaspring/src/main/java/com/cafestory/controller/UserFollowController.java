package com.cafestory.controller;

import com.cafestory.dto.responseDTO.FollowTargetResponseDTO;
import com.cafestory.dto.responseDTO.UserFollowResponseDTO;
import com.cafestory.entity.enums.FollowTargetFilter;
import com.cafestory.service.serviceInterface.UserFollowService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/users")
public class UserFollowController {

    private final UserFollowService userFollowService;

    public UserFollowController(UserFollowService userFollowService) {
        this.userFollowService = userFollowService;
    }

    @PostMapping("/{followingUserId}/followers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserFollowResponseDTO followUser(
            @PathVariable UUID followingUserId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return userFollowService.followUser(followingUserId, requireUserId(principal));
    }

    @DeleteMapping("/{followingUserId}/followers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowUser(
            @PathVariable UUID followingUserId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        userFollowService.unfollowUser(followingUserId, requireUserId(principal));
    }

    @GetMapping("/{userId}/followers")
    public List<UserFollowResponseDTO> getFollowersByUserId(@PathVariable UUID userId) {
        return userFollowService.getFollowersByUserId(userId);
    }

    @GetMapping("/{userId}/following")
    public List<UserFollowResponseDTO> getFollowingByUserId(@PathVariable UUID userId) {
        return userFollowService.getFollowingByUserId(userId);
    }

    @GetMapping("/{userId}/following-targets")
    public List<FollowTargetResponseDTO> getFollowingTargetsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "ALL") FollowTargetFilter type) {
        return userFollowService.getFollowingTargetsByUserId(userId, type);
    }
}
