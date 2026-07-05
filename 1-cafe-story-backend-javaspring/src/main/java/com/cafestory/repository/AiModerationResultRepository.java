package com.cafestory.repository;

import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.enums.ModerationDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiModerationResultRepository
        extends JpaRepository<AiModerationResult, UUID>,
        JpaSpecificationExecutor<AiModerationResult> {
    boolean existsByBlogIdAndDecision(UUID blogId, ModerationDecision decision);

    boolean existsByContentReportId(UUID contentReportId);

    Optional<AiModerationResult> findTopByContentReportIdOrderByCreatedAtDesc(UUID contentReportId);

    @Query("""
            select distinct result.blog.id
            from AiModerationResult result
            where result.blog.id in :blogIds
            and result.decision = :decision
            """)
    List<UUID> findBlogIdsByBlogIdInAndDecision(
            @Param("blogIds") Collection<UUID> blogIds,
            @Param("decision") ModerationDecision decision);

    Page<AiModerationResult> findByDecisionInAndResolvedFalse(Collection<ModerationDecision> decisions, Pageable pageable);

    long countByDecisionInAndResolvedFalse(Collection<ModerationDecision> decisions);
}
