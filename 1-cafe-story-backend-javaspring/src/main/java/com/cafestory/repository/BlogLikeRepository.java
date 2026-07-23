package com.cafestory.repository;

import com.cafestory.entity.BlogLike;
import com.cafestory.entity.enums.ActorContextType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Collection;

public interface BlogLikeRepository extends JpaRepository<BlogLike, UUID> {
    boolean existsByUserUserIdAndBlogId(UUID userId, UUID blogId);

    Optional<BlogLike> findByUserUserIdAndBlogId(UUID userId, UUID blogId);

    @Query("""
            select count(like) > 0
            from BlogLike like
            where like.user.userId = :userId
            and like.blog.id = :blogId
            and like.actorContextType = :actorContextType
            and (
                (:actorCafePageId is null and like.actorCafePage is null)
                or like.actorCafePage.id = :actorCafePageId
            )
            """)
    boolean existsByActor(
            @Param("userId") UUID userId,
            @Param("blogId") UUID blogId,
            @Param("actorContextType") ActorContextType actorContextType,
            @Param("actorCafePageId") UUID actorCafePageId);

    @Query("""
            select like
            from BlogLike like
            where like.user.userId = :userId
            and like.blog.id = :blogId
            and like.actorContextType = :actorContextType
            and (
                (:actorCafePageId is null and like.actorCafePage is null)
                or like.actorCafePage.id = :actorCafePageId
            )
            """)
    Optional<BlogLike> findByActor(
            @Param("userId") UUID userId,
            @Param("blogId") UUID blogId,
            @Param("actorContextType") ActorContextType actorContextType,
            @Param("actorCafePageId") UUID actorCafePageId);

    @Query("""
            select like.blog.id
            from BlogLike like
            where like.user.userId = :userId
            and like.actorContextType = com.cafestory.entity.enums.ActorContextType.USER
            and like.blog.id in :blogIds
            """)
    List<UUID> findLikedBlogIdsByUserIdAndBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") List<UUID> blogIds);

    @Query("""
            select distinct like.blog.id
            from BlogLike like
            where like.user.userId = :userId
            and like.blog.id in :blogIds
            and like.createdAt >= :startAt
            and like.createdAt < :endAt
            """)
    List<UUID> findRecentlyLikedBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<BlogLike> findByBlogId(UUID blogId);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<BlogLike> findByUserUserId(UUID userId);

    long countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    List<BlogLike> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);

    long countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID blogId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
            select like.blog.id as blogId, count(like) as eventCount
            from BlogLike like
            where like.blog.id in :blogIds
            and like.createdAt >= :startAt
            and like.createdAt < :endAt
            group by like.blog.id
            """)
    List<BlogInteractionCountRow> countByBlogIdsAndCreatedAtBetween(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    interface BlogInteractionCountRow {
        UUID getBlogId();

        Long getEventCount();
    }

    @Query("""
            select like.user.userId as userId, count(like) as eventCount
            from BlogLike like
            where like.createdAt >= :startAt
            and like.createdAt < :endAt
            group by like.user.userId
            """)
    List<UserInteractionCountRow> countByUserAndCreatedAtBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    interface UserInteractionCountRow {
        UUID getUserId();

        Long getEventCount();
    }
}
