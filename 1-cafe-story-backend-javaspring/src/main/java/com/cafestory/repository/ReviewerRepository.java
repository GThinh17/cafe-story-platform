package com.cafestory.repository;

import com.cafestory.entity.Reviewer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;

public interface ReviewerRepository extends JpaRepository<Reviewer, UUID> {

    Optional<Reviewer> findByUserUserId(UUID userId);

    @Query("select r from Reviewer r left join fetch r.user")
    List<Reviewer> findAllWithUser();

    /**
     * Như findAllWithUser nhưng fetch luôn region.
     *
     * <p>Bảng xếp hạng reviewer đọc user.region khi chấm điểm vùng, nên thiếu
     * fetch join này sẽ sinh thêm N query lazy. Cố ý KHÔNG lọc reviewerActive
     * để giữ đúng tập reviewer mà bảng xếp hạng vẫn dùng.
     */
    @Query("select r from Reviewer r left join fetch r.user u left join fetch u.region region")
    List<Reviewer> findAllWithUserAndRegion();

    long countByReviewerActive(Boolean reviewerActive);

    /** Reviewer còn bật cờ active nhưng gói đã quá hạn — job hằng ngày hạ cờ. */
    List<Reviewer> findByReviewerActiveTrueAndReviewerExpiresAtBefore(LocalDateTime cutoff);

    @Query("""
            select r
            from Reviewer r
            left join fetch r.user u
            left join fetch u.region region
            where r.reviewerActive = true
            and u.accountStatus = true
            and u.userId <> :currentUserId
            order by
                case
                    when region.regionId = :currentRegionId then 0
                    when lower(region.city) = :currentCity then 1
                    else 2
                end,
                coalesce(u.userFollower, 0) desc,
                coalesce(u.userLike, 0) desc,
                r.createdAt desc
            """)
    List<Reviewer> findRecommendationCandidates(
            @Param("currentUserId") UUID currentUserId,
            @Param("currentRegionId") UUID currentRegionId,
            @Param("currentCity") String currentCity,
            Pageable pageable);

    @Query("""
            select r.user.userId
            from Reviewer r
            where r.user.userId in :userIds
            and r.reviewerActive = true
            """)
    List<UUID> findActiveReviewerUserIdsByUserIds(@Param("userIds") Collection<UUID> userIds);

    @Query("""
            select r from Reviewer r
            left join fetch r.user u
            left join fetch u.region region
            where u.accountStatus = true
            and r.reviewerActive = true
            """)
    List<Reviewer> findAllActiveReviewers();

    @Query("""
            select r
            from Reviewer r
            left join fetch r.user u
            left join fetch u.region region
            where r.reviewerActive = true
            and u.accountStatus = true
            and (
                (:cursorId is null and coalesce(r.updatedAt, r.createdAt) >= :since)
                or (:cursorId is not null and (
                    coalesce(r.updatedAt, r.createdAt) > :since
                    or (coalesce(r.updatedAt, r.createdAt) = :since and r.reviewerId > :cursorId)
                ))
            )
            order by coalesce(r.updatedAt, r.createdAt) asc, r.reviewerId asc
            """)
    List<Reviewer> findRagSnapshotReviewers(
            @Param("since") LocalDateTime since,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    @Query("""
            select r.reviewerId
            from Reviewer r
            left join r.user u
            where (r.reviewerActive = false or u.accountStatus = false)
            and coalesce(r.updatedAt, r.createdAt) >= :since
            """)
    List<UUID> findRagTombstoneReviewerIds(@Param("since") LocalDateTime since);

    @Query("""
            select r from Reviewer r
            left join fetch r.user u
            where r.reviewerActive = true
            and u.accountStatus = true
            order by coalesce(u.userFollower, 0) desc, coalesce(u.userLike, 0) desc
            """)
    List<Reviewer> findRagTopReviewers(Pageable pageable);

    @Query("""
            select r from Reviewer r
            left join fetch r.user u
            left join fetch u.region region
            where u.accountStatus = true
            and r.reviewerActive = true
            and (lower(u.userName) like lower(concat('%', :query, '%'))
                or lower(u.userFullName) like lower(concat('%', :query, '%')))
            """)
    List<Reviewer> searchActiveReviewers(@Param("query") String query);

    @Query("""
            select p.provinceCode, count(rv)
            from Reviewer rv
            join rv.user u
            join u.region r
            join r.provinceRef p
            group by p.provinceCode
            """)
    List<Object[]> countReviewersByProvince();
}
