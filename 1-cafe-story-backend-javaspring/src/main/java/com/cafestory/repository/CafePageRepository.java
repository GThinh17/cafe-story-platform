package com.cafestory.repository;

import com.cafestory.entity.CafePage;
import com.cafestory.entity.enums.PageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CafePageRepository extends JpaRepository<CafePage, UUID> {
    List<CafePage> findByOwnerUserId(UUID ownerUserId);

    boolean existsByOwnerUserId(UUID ownerUserId);

    long countByStatus(PageStatus status);

    @Query("""
            select p
            from CafePage p
            where (:status is null or p.status = :status)
            and (:ownerUserId is null or p.owner.userId = :ownerUserId)
            """)
    Page<CafePage> findAdminCafePages(
            @Param("status") PageStatus status,
            @Param("ownerUserId") UUID ownerUserId,
            Pageable pageable);
}
