package com.cafestory.repository;

import com.cafestory.entity.AdminReportAiResolution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminReportAiResolutionRepository extends JpaRepository<AdminReportAiResolution, UUID> {

    Page<AdminReportAiResolution> findByContentReportId(UUID contentReportId, Pageable pageable);
}
