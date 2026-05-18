package com.cafestory.repository;

import com.cafestory.entity.ReviewerBadgeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewerBadgeHistoryRepository extends JpaRepository<ReviewerBadgeHistory, UUID> {

    boolean existsByReviewerUserIdAndMonth(UUID reviewerId, String month);

    Optional<ReviewerBadgeHistory> findByReviewerUserIdAndMonth(UUID reviewerId, String month);

    List<ReviewerBadgeHistory> findByReviewerUserIdOrderByMonthDesc(UUID reviewerId);
}
