package com.cafestory.repository;

import com.cafestory.entity.AdTargetRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdTargetRegionRepository extends JpaRepository<AdTargetRegion, UUID> {
}
