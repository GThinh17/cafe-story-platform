package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.FollowTargetResponseDTO;
import com.cafestory.dto.responseDTO.UserFollowResponseDTO;
import com.cafestory.entity.enums.FollowTargetFilter;

import java.util.List;
import java.util.UUID;

public interface UserFollowService {
    UserFollowResponseDTO followUser(UUID followingUserId, UUID followerUserId);

    void unfollowUser(UUID followingUserId, UUID followerUserId);

    List<UserFollowResponseDTO> getFollowersByUserId(UUID followingUserId);

    List<UserFollowResponseDTO> getFollowingByUserId(UUID followerUserId);

    List<FollowTargetResponseDTO> getFollowingTargetsByUserId(UUID followerUserId, FollowTargetFilter targetType);
}
