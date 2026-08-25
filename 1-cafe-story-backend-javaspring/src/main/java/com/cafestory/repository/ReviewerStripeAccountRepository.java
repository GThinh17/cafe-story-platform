package com.cafestory.repository;

import com.cafestory.entity.ReviewerStripeAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewerStripeAccountRepository extends JpaRepository<ReviewerStripeAccount, UUID> {

    Optional<ReviewerStripeAccount> findByReviewerReviewerId(UUID reviewerId);

    Optional<ReviewerStripeAccount> findByStripeAccountId(String stripeAccountId);
}
