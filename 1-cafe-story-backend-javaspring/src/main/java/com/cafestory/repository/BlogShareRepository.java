package com.cafestory.repository;

import com.cafestory.entity.BlogShare;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Collection;

public interface BlogShareRepository extends JpaRepository<BlogShare, UUID> {
    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<BlogShare> findByBlogId(UUID blogId);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<BlogShare> findByUserUserId(UUID userId);

    @Query("""
            select distinct share.blog.id
            from BlogShare share
            where share.user.userId = :userId
            and share.blog.id in :blogIds
            and share.createdAt >= :startAt
            and share.createdAt < :endAt
            """)
    List<UUID> findRecentlySharedBlogIds(
            @Param("userId") UUID userId,
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    long countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    List<BlogShare> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);

    long countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID blogId, LocalDateTime startDate, LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("""
            select share.blog.id as blogId, count(share) as eventCount
            from BlogShare share
            where share.blog.id in :blogIds
            and share.createdAt >= :startAt
            and share.createdAt < :endAt
            group by share.blog.id
            """)
    List<BlogShareCountRow> countByBlogIdsAndCreatedAtBetween(
            @org.springframework.data.repository.query.Param("blogIds") Collection<UUID> blogIds,
            @org.springframework.data.repository.query.Param("startAt") LocalDateTime startAt,
            @org.springframework.data.repository.query.Param("endAt") LocalDateTime endAt);

    interface BlogShareCountRow {
        UUID getBlogId();

        Long getEventCount();
    }

    @org.springframework.data.jpa.repository.Query("""
            select share.user.userId as userId, count(share) as eventCount
            from BlogShare share
            where share.createdAt >= :startAt
            and share.createdAt < :endAt
            group by share.user.userId
            """)
    List<UserShareCountRow> countByUserAndCreatedAtBetween(
            @org.springframework.data.repository.query.Param("startAt") LocalDateTime startAt,
            @org.springframework.data.repository.query.Param("endAt") LocalDateTime endAt);

    interface UserShareCountRow {
        UUID getUserId();

        Long getEventCount();
    }
}
