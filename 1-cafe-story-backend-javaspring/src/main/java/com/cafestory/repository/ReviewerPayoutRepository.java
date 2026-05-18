package com.cafestory.repository;

import com.cafestory.entity.ReviewerPayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewerPayoutRepository extends JpaRepository<ReviewerPayout, UUID> {

    boolean existsByReviewerUserIdAndPayoutMonth(UUID reviewerId, String payoutMonth);

    Optional<ReviewerPayout> findByReviewerUserIdAndPayoutMonth(UUID reviewerId, String payoutMonth);

    List<ReviewerPayout> findByReviewerUserIdOrderByPayoutMonthDesc(UUID reviewerId);
}
