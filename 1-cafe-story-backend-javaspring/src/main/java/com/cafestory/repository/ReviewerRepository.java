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
            select distinct r
            from Reviewer r
            left join fetch r.user u
            left join fetch u.region region
            where r.reviewerActive = true
            and u.accountStatus = true
            and u.userId <> :currentUserId
            and not exists (
                select uf
                from UserFollow uf
                where uf.follower.userId = :currentUserId
                and uf.following.userId = u.userId
            )
            """)
    List<Reviewer> findRecommendationCandidates(
            @Param("currentUserId") UUID currentUserId,
            Pageable pageable);
}
