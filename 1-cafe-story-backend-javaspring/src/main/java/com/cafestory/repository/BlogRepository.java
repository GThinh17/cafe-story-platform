package com.cafestory.repository;

import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blog, UUID> {
    List<Blog> findByAuthorUserId(UUID authorUserId);

    List<Blog> findByPageId(UUID pageId);

    List<Blog> findByStatus(PostStatus status);
}
