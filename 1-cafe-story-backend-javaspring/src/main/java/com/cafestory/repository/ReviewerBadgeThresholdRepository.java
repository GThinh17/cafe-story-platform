package com.cafestory.repository;

import com.cafestory.entity.ReviewerBadgeThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewerBadgeThresholdRepository extends JpaRepository<ReviewerBadgeThreshold, UUID> {

    List<ReviewerBadgeThreshold> findByFormulaIdOrderByMinScoreAsc(UUID formulaId);

    void deleteByFormulaId(UUID formulaId);
}
