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

    /**
     * Tổng like/share/comment theo từng tháng của một reviewer.
     *
     * <p>admin_payout chỉ lưu tổng tiền, không lưu chi tiết từng loại tương tác.
     * Trang thu nhập của reviewer cần phần bóc tách nên phải gom lại từ đây.
     * Gom theo year/month thay vì to_char để giữ JPQL thuần, không phụ thuộc
     * phương ngữ SQL.
     */
    @Query("""
            select year(i.incomeDate) as year,
                   month(i.incomeDate) as month,
                   sum(i.likeCount) as likeCount,
                   sum(i.shareCount) as shareCount,
                   sum(i.commentCount) as commentCount
            from ReviewerIncome i
            where i.reviewer.reviewerId = :reviewerId
            group by year(i.incomeDate), month(i.incomeDate)
            """)
    List<ReviewerMonthlyCountRow> sumCountsByReviewerGroupByMonth(@Param("reviewerId") UUID reviewerId);

    interface ReviewerMonthlyCountRow {
        Integer getYear();

        Integer getMonth();

        Long getLikeCount();

        Long getShareCount();

        Long getCommentCount();
    }
}
