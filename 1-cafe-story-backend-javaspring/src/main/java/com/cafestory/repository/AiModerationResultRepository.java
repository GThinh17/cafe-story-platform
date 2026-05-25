package com.cafestory.repository;

import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.enums.ModerationDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface AiModerationResultRepository extends JpaRepository<AiModerationResult, UUID> {
    boolean existsByBlogIdAndDecision(UUID blogId, ModerationDecision decision);

    Page<AiModerationResult> findByDecisionInAndResolvedFalse(Collection<ModerationDecision> decisions, Pageable pageable);

    long countByDecisionInAndResolvedFalse(Collection<ModerationDecision> decisions);
}
