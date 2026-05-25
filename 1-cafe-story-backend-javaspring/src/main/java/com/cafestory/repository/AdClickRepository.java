package com.cafestory.repository;

import com.cafestory.entity.AdClick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdClickRepository extends JpaRepository<AdClick, UUID> {
}
