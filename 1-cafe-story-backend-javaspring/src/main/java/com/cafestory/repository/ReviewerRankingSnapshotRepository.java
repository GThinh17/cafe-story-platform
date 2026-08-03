package com.cafestory.repository;

import com.cafestory.entity.ReviewerRankingSnapshot;
import com.cafestory.entity.enums.RankingPeriodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewerRankingSnapshotRepository extends JpaRepository<ReviewerRankingSnapshot, UUID> {

    @EntityGraph(attributePaths = {"reviewer", "reviewer.user"})
    List<ReviewerRankingSnapshot> findByPeriodAndPeriodTypeOrderByRankPositionAsc(String period, RankingPeriodType periodType);

    @EntityGraph(attributePaths = {"reviewer", "reviewer.user"})
    Page<ReviewerRankingSnapshot> findByPeriodAndPeriodType(String period, RankingPeriodType periodType, Pageable pageable);

    Optional<ReviewerRankingSnapshot> findByReviewerReviewerIdAndPeriodAndPeriodType(UUID reviewerId, String period, RankingPeriodType periodType);

    void deleteByPeriodAndPeriodType(String period, RankingPeriodType periodType);
}
