package com.cafestory.repository;

import com.cafestory.entity.AdImpression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AdImpressionRepository extends JpaRepository<AdImpression, UUID> {

    long countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
            UUID userId,
            UUID adCampaignId,
            LocalDateTime startAt,
            LocalDateTime endAt);
}
