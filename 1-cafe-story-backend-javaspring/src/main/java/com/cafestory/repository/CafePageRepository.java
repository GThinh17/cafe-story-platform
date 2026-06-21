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

    @Query("""
            select p
            from CafePage p
            left join fetch p.region r
            left join fetch p.owner o
            where p.status = com.cafestory.entity.enums.PageStatus.ACTIVE
            and p.pageActive = true
            and (:regionId is null or r.regionId = :regionId)
            and (cast(:city as string) is null or lower(r.city) = lower(cast(:city as string)))
            """)
    List<CafePage> findActiveCafePagesForRegionalRanking(
            @Param("regionId") UUID regionId,
            @Param("city") String city);

    @Query("""
            select distinct p
            from CafePage p
            left join fetch p.region r
            left join fetch p.owner o
            where p.status = com.cafestory.entity.enums.PageStatus.ACTIVE
            and p.pageActive = true
            and not exists (
                select pf
                from PageFollow pf
                where pf.user.userId = :currentUserId
                and pf.cafePage.id = p.id
            )
            """)
    List<CafePage> findRecommendationCandidates(
            @Param("currentUserId") UUID currentUserId,
            Pageable pageable);
}
