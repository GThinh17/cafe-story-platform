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
            and (:city is null or lower(r.city) = lower(:city))
            """)
    List<CafePage> findActiveCafePagesForRegionalRanking(
            @Param("regionId") UUID regionId,
            @Param("city") String city);

    @Query("""
            select p
            from CafePage p
            left join fetch p.region r
            left join fetch p.owner o
            where p.pageActive = true
            and p.status <> com.cafestory.entity.enums.PageStatus.SUSPENDED
            and o.userId <> :currentUserId
            order by
                case
                    when r.regionId = :currentRegionId then 0
                    when lower(r.city) = :currentCity then 1
                    else 2
                end,
                coalesce(p.followerCount, 0) desc,
                coalesce(p.likeCount, 0) desc,
                p.createdAt desc
            """)
    List<CafePage> findRecommendationCandidates(
            @Param("currentUserId") UUID currentUserId,
            @Param("currentRegionId") UUID currentRegionId,
            @Param("currentCity") String currentCity,
            Pageable pageable);
}
