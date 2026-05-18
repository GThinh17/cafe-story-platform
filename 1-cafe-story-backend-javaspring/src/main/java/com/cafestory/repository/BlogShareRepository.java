package com.cafestory.repository;

import com.cafestory.entity.BlogShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlogShareRepository extends JpaRepository<BlogShare, UUID> {
    List<BlogShare> findByBlogId(UUID blogId);

    List<BlogShare> findByUserUserId(UUID userId);
}
