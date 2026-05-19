package com.cafestory.repository;

import com.cafestory.entity.CafePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CafePageRepository extends JpaRepository<CafePage, UUID> {
    List<CafePage> findByOwnerUserId(UUID ownerUserId);

    boolean existsByOwnerUserId(UUID ownerUserId);
}
