package com.cafestory.repository;

import com.cafestory.entity.PayoutFormula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayoutFormulaRepository extends JpaRepository<PayoutFormula, UUID> {

    Optional<PayoutFormula> findByActiveTrue();

    List<PayoutFormula> findAllByOrderByCreatedAtDesc();
}
