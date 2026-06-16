package com.cafestory.repository;

import com.cafestory.entity.ReviewerScoringFormula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewerScoringFormulaRepository extends JpaRepository<ReviewerScoringFormula, UUID> {

    Optional<ReviewerScoringFormula> findByActiveTrue();

    List<ReviewerScoringFormula> findAllByOrderByCreatedAtDesc();
}
