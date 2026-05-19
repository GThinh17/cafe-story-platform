package com.cafestory.repository;

import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.enums.ModerationDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiModerationResultRepository extends JpaRepository<AiModerationResult, UUID> {
    boolean existsByBlogIdAndDecision(UUID blogId, ModerationDecision decision);
}
