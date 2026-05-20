package com.cafestory.repository;

import com.cafestory.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

public interface BlogLikeRepository extends JpaRepository<BlogLike, UUID> {
    boolean existsByUserUserIdAndBlogId(UUID userId, UUID blogId);

    Optional<BlogLike> findByUserUserIdAndBlogId(UUID userId, UUID blogId);

    List<BlogLike> findByBlogId(UUID blogId);

    List<BlogLike> findByUserUserId(UUID userId);

    long countByUserUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    List<BlogLike> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);

    long countByBlogIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID blogId, LocalDateTime startDate, LocalDateTime endDate);
}
