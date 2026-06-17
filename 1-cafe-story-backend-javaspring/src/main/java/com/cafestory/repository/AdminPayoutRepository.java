package com.cafestory.repository;

import com.cafestory.entity.AdminPayout;
import com.cafestory.entity.enums.AdminPayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminPayoutRepository extends JpaRepository<AdminPayout, UUID> {

    List<AdminPayout> findByPayoutMonth(String payoutMonth);

    Page<AdminPayout> findByPayoutMonth(String payoutMonth, Pageable pageable);

    Page<AdminPayout> findByPayoutMonthAndStatus(String payoutMonth, AdminPayoutStatus status, Pageable pageable);

    Page<AdminPayout> findByStatus(AdminPayoutStatus status, Pageable pageable);

    Page<AdminPayout> findByReviewerReviewerId(UUID reviewerId, Pageable pageable);
}
