package com.cafestory.repository;

import com.cafestory.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    boolean existsByFollowerUserIdAndFollowingUserId(UUID followerUserId, UUID followingUserId);

    @Query("""
            select follow.following.userId
            from UserFollow follow
            where follow.follower.userId = :followerUserId
            and follow.following.userId in :followingUserIds
            """)
    List<UUID> findFollowedUserIds(
            @Param("followerUserId") UUID followerUserId,
            @Param("followingUserIds") List<UUID> followingUserIds);

    Optional<UserFollow> findByFollowerUserIdAndFollowingUserId(UUID followerUserId, UUID followingUserId);

    List<UserFollow> findByFollowingUserId(UUID followingUserId);

    List<UserFollow> findByFollowerUserId(UUID followerUserId);

    long countByFollowerUserId(UUID followerUserId);
}
