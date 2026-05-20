package com.cafestory.repository;

import com.cafestory.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
