package com.cafestory.repository;

import com.cafestory.entity.Reviewer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewerRepository extends JpaRepository<Reviewer, UUID> {

    Optional<Reviewer> findByUserUserId(UUID userId);

    long countByReviewerActive(Boolean reviewerActive);

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
            select r from Reviewer r
            left join fetch r.user u
            left join fetch u.region region
            where u.accountStatus = true
            and r.reviewerActive = true
            """)
    List<Reviewer> findAllActiveReviewers();

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
}
