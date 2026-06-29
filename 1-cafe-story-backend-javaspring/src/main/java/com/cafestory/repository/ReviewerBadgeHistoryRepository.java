package com.cafestory.repository;

import com.cafestory.entity.ReviewerBadgeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewerBadgeHistoryRepository extends JpaRepository<ReviewerBadgeHistory, UUID> {

    boolean existsByReviewerReviewerIdAndMonth(UUID reviewerId, String month);

    Optional<ReviewerBadgeHistory> findByReviewerReviewerIdAndMonth(UUID reviewerId, String month);

    List<ReviewerBadgeHistory> findByReviewerReviewerIdOrderByMonthDesc(UUID reviewerId);

    Optional<ReviewerBadgeHistory> findTopByReviewerReviewerIdOrderByMonthDesc(UUID reviewerId);

    List<ReviewerBadgeHistory> findByMonthAndReviewerReviewerIdIn(String month, java.util.Collection<UUID> reviewerIds);
}
