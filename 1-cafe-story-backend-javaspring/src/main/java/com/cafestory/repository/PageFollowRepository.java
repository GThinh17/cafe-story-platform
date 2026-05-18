package com.cafestory.repository;

import com.cafestory.entity.PageFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageFollowRepository extends JpaRepository<PageFollow, UUID> {
    boolean existsByUserUserIdAndCafePageId(UUID userId, UUID cafePageId);

    Optional<PageFollow> findByUserUserIdAndCafePageId(UUID userId, UUID cafePageId);

    List<PageFollow> findByCafePageId(UUID cafePageId);

    List<PageFollow> findByUserUserId(UUID userId);
}
