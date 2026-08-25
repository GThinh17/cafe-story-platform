package com.cafestory.repository;

import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.enums.AdStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface AdCampaignRepository extends JpaRepository<AdCampaign, UUID> {

    List<AdCampaign> findByCafePageIdOrderByCreatedAtDesc(UUID cafePageId);

    boolean existsByPaymentPaymentId(UUID paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AdCampaign c where c.adCampaignId = :id")
    Optional<AdCampaign> findByIdWithLock(@Param("id") UUID id);

    @Query("""
            select distinct c
            from AdCampaign c
            join fetch c.cafePage p
            left join fetch p.region
            left join fetch c.targetRegions
            where c.status = :status
              and (c.startAt is null or c.startAt <= :now)
              and (c.endAt is null or :now <= c.endAt)
              and c.servedImpressions < c.maxImpressions
            """)
    List<AdCampaign> findActiveCandidates(@Param("status") AdStatus status, @Param("now") LocalDateTime now);

    List<AdCampaign> findByStatus(AdStatus status);
}
