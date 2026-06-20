package com.cafestory.repository;

import com.cafestory.entity.ReviewerIncome;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReviewerIncomeRepository extends JpaRepository<ReviewerIncome, UUID> {

    List<ReviewerIncome> findByIncomeDate(LocalDate incomeDate);

    List<ReviewerIncome> findByIncomeDateGreaterThanEqualAndIncomeDateLessThan(LocalDate startInclusive, LocalDate endExclusive);

    Page<ReviewerIncome> findByReviewerReviewerIdAndIncomeDateGreaterThanEqualAndIncomeDateLessThan(
            UUID reviewerId, LocalDate startInclusive, LocalDate endExclusive, Pageable pageable);

    Page<ReviewerIncome> findByIncomeDateGreaterThanEqualAndIncomeDateLessThan(
            LocalDate startInclusive, LocalDate endExclusive, Pageable pageable);
}
