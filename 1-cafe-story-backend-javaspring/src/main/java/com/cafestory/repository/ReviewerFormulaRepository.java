package com.cafestory.repository;

import com.cafestory.entity.ReviewerFormula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewerFormulaRepository extends JpaRepository<ReviewerFormula, UUID> {

    Optional<ReviewerFormula> findByActiveTrue();

    List<ReviewerFormula> findAllByOrderByCreatedAtDesc();
}
