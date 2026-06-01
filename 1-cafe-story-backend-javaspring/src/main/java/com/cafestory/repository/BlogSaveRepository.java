package com.cafestory.repository;

import com.cafestory.entity.BlogSave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogSaveRepository extends JpaRepository<BlogSave, UUID> {
    Optional<BlogSave> findByUserUserIdAndBlogId(UUID userId, UUID blogId);

    List<BlogSave> findByBlogId(UUID blogId);

    List<BlogSave> findByUserUserId(UUID userId);

    long countByBlogId(UUID blogId);
}
