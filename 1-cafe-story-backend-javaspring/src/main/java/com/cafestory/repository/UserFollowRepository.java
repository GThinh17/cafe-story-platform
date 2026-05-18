package com.cafestory.repository;

import com.cafestory.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    boolean existsByFollowerUserIdAndFollowingUserId(UUID followerUserId, UUID followingUserId);

    Optional<UserFollow> findByFollowerUserIdAndFollowingUserId(UUID followerUserId, UUID followingUserId);

    List<UserFollow> findByFollowingUserId(UUID followingUserId);

    List<UserFollow> findByFollowerUserId(UUID followerUserId);
}
