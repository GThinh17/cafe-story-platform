package com.cafestory.repository;

import com.cafestory.entity.ReviewerPayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewerPayoutRepository extends JpaRepository<ReviewerPayout, UUID> {

    boolean existsByReviewerReviewerIdAndPayoutMonth(UUID reviewerId, String payoutMonth);

    Optional<ReviewerPayout> findByReviewerReviewerIdAndPayoutMonth(UUID reviewerId, String payoutMonth);

    List<ReviewerPayout> findByReviewerReviewerIdOrderByPayoutMonthDesc(UUID reviewerId);

    List<ReviewerPayout> findByPayoutMonth(String payoutMonth);
}
