package com.cafestory.repository;

import com.cafestory.entity.ExtraFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExtraFeeRepository extends JpaRepository<ExtraFee, UUID> {

    List<ExtraFee> findAllByOrderByCreatedAtDesc();
}
