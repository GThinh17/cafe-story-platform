package com.cafestory.repository;

import com.cafestory.entity.ReviewerIncome;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select distinct i.incomeDate
            from ReviewerIncome i
            where i.incomeDate >= :startInclusive
            and i.incomeDate < :endExclusive
            """)
    List<LocalDate> findCoveredDatesBetween(
            @Param("startInclusive") LocalDate startInclusive,
            @Param("endExclusive") LocalDate endExclusive);

    @Query("""
            select i.reviewer.reviewerId as reviewerId, sum(i.baseAmount) as totalBase
            from ReviewerIncome i
            where i.incomeDate >= :startInclusive
            and i.incomeDate < :endExclusive
            group by i.reviewer.reviewerId
            """)
    List<ReviewerBaseRow> sumBaseAmountByReviewerBetween(
            @Param("startInclusive") LocalDate startInclusive,
            @Param("endExclusive") LocalDate endExclusive);

    interface ReviewerBaseRow {
        UUID getReviewerId();

        Long getTotalBase();
    }
}
