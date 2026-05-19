package com.cafestory.repository;

import com.cafestory.entity.PageLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageLikeRepository extends JpaRepository<PageLike, UUID> {
    boolean existsByUserUserIdAndCafePageId(UUID userId, UUID cafePageId);

    Optional<PageLike> findByUserUserIdAndCafePageId(UUID userId, UUID cafePageId);

    List<PageLike> findByCafePageId(UUID cafePageId);

    List<PageLike> findByUserUserId(UUID userId);
}
