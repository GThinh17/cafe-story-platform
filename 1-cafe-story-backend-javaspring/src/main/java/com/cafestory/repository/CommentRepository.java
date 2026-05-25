package com.cafestory.repository;

import com.cafestory.entity.Comment;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByBlogId(UUID blogId);

    List<Comment> findByUserUserId(UUID userId);

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
    Page<Comment> findAdminComments(
            @Param("status") PostStatus status,
            @Param("blogId") UUID blogId,
            @Param("userId") UUID userId,
            Pageable pageable);
}
