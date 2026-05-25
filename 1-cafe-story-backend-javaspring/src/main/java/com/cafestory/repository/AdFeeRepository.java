package com.cafestory.repository;

import com.cafestory.entity.AdFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdFeeRepository extends JpaRepository<AdFee, UUID> {

    List<AdFee> findAllByOrderByCreatedAtDesc();
}
