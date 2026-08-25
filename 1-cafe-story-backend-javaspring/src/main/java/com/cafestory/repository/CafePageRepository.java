package com.cafestory.repository;

import com.cafestory.entity.CafePage;
import com.cafestory.entity.enums.PageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CafePageRepository extends JpaRepository<CafePage, UUID> {
    List<CafePage> findByOwnerUserId(UUID ownerUserId);

    boolean existsByOwnerUserId(UUID ownerUserId);

    long countByStatus(PageStatus status);

    @Query("""
            select pr.provinceCode, count(p)
            from CafePage p
            join p.region r
            join r.provinceRef pr
            group by pr.provinceCode
            """)
    List<Object[]> countCafePagesByProvince();

    @Query("""
            select p
            from CafePage p
            where (:status is null or p.status = :status)
            and (:ownerUserId is null or p.owner.userId = :ownerUserId)
            """)
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {
            "owner", "region", "region.cityRef", "region.provinceRef", "region.wardRef"})
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
            and (cast(:area as string) is null or lower(r.area) = lower(cast(:area as string)))
            and (cast(:province as string) is null or lower(r.province) = lower(cast(:province as string)))
            """)
    List<CafePage> findActiveCafePagesForRegionalRanking(
            @Param("regionId") UUID regionId,
            @Param("city") String city,
            @Param("area") String area,
            @Param("province") String province);

    @Query("select p from CafePage p where p.status = com.cafestory.entity.enums.PageStatus.ACTIVE and lower(p.name) like lower(concat('%', :query, '%'))")
    List<CafePage> searchActiveCafePagesByName(@Param("query") String query);

    @Query("""
            select p
            from CafePage p
            left join fetch p.region r
            left join fetch p.owner o
            where p.status = com.cafestory.entity.enums.PageStatus.ACTIVE
            and p.pageActive = true
            and (
                lower(p.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(p.description, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(p.address, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(r.city, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(r.province, '')) like lower(concat('%', :query, '%'))
            )
            order by coalesce(p.followerCount, 0) desc, coalesce(p.likeCount, 0) desc, p.name asc
            """)
    List<CafePage> searchActiveCafePages(@Param("query") String query, Pageable pageable);

    @Query("select p from CafePage p where p.status = com.cafestory.entity.enums.PageStatus.ACTIVE")
    List<CafePage> findAllActiveCafePages();

    @Query("""
            select p
            from CafePage p
            left join fetch p.region r
            where p.status = com.cafestory.entity.enums.PageStatus.ACTIVE
            and (
                (:cursorId is null and coalesce(p.updatedAt, p.createdAt) >= :since)
                or (:cursorId is not null and (
                    coalesce(p.updatedAt, p.createdAt) > :since
                    or (coalesce(p.updatedAt, p.createdAt) = :since and p.id > :cursorId)
                ))
            )
            order by coalesce(p.updatedAt, p.createdAt) asc, p.id asc
            """)
    List<CafePage> findRagSnapshotCafePages(
            @Param("since") LocalDateTime since,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    @Query("""
            select p.id
            from CafePage p
            where p.status <> com.cafestory.entity.enums.PageStatus.ACTIVE
            and coalesce(p.updatedAt, p.createdAt) >= :since
            """)
    List<UUID> findRagTombstoneCafePageIds(@Param("since") LocalDateTime since);

    @Query("""
            select p
            from CafePage p
            left join fetch p.region r
            where p.status = com.cafestory.entity.enums.PageStatus.ACTIVE
            and (cast(:province as string) is null
                or lower(r.province) like lower(concat('%', cast(:province as string), '%')))
            order by coalesce(p.followerCount, 0) desc, coalesce(p.likeCount, 0) desc
            """)
    List<CafePage> findRagTrendingCafePages(@Param("province") String province, Pageable pageable);

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
