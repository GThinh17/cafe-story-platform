package com.cafestory.repository;

import com.cafestory.entity.Reviewer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewerRepository extends JpaRepository<Reviewer, UUID> {

    Optional<Reviewer> findByUserUserId(UUID userId);

    long countByReviewerActive(Boolean reviewerActive);
}
