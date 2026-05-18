package com.cafestory.repository;

import com.cafestory.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByBlogId(UUID blogId);

    List<Comment> findByUserUserId(UUID userId);

    List<Comment> findByParentCommentId(UUID parentCommentId);
}
