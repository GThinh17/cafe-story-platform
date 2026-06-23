package com.cafestory.repository;

import com.cafestory.entity.Comment;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Collection;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Override
    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<Comment> findAll();

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<Comment> findByBlogId(UUID blogId);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<Comment> findByUserUserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    List<Comment> findByParentCommentId(UUID parentCommentId);

    long countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    List<Comment> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);

    long countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID blogId, LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(PostStatus status);

    @Query("""
            select c
            from Comment c
            where (:status is null or c.status = :status)
            and (:blogId is null or c.blog.id = :blogId)
            and (:userId is null or c.user.userId = :userId)
            """)
    @EntityGraph(attributePaths = {"user", "actorCafePage"})
    Page<Comment> findAdminComments(
            @Param("status") PostStatus status,
            @Param("blogId") UUID blogId,
            @Param("userId") UUID userId,
            Pageable pageable);

    @Query("""
            select c.blog.id as blogId, count(c) as eventCount
            from Comment c
            where c.blog.id in :blogIds
            and c.parentComment is null
            and c.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and c.createdAt >= :startAt
            and c.createdAt < :endAt
            group by c.blog.id
            """)
    List<CommentCountRow> countRootCommentsByBlogIdsAndCreatedAtBetween(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    @Query("""
            select c.blog.id as blogId, count(c) as eventCount
            from Comment c
            where c.blog.id in :blogIds
            and c.parentComment is not null
            and c.status = com.cafestory.entity.enums.PostStatus.PUBLISHED
            and c.createdAt >= :startAt
            and c.createdAt < :endAt
            group by c.blog.id
            """)
    List<CommentCountRow> countRepliesByBlogIdsAndCreatedAtBetween(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    interface CommentCountRow {
        UUID getBlogId();

        Long getEventCount();
    }
}
