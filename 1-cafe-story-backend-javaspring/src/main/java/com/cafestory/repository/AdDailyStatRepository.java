package com.cafestory.repository;

import com.cafestory.entity.AdDailyStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdDailyStatRepository extends JpaRepository<AdDailyStat, UUID> {

    Optional<AdDailyStat> findByAdCampaignAdCampaignIdAndStatDate(UUID adCampaignId, LocalDate statDate);

    List<AdDailyStat> findByAdCampaignAdCampaignIdOrderByStatDateAsc(UUID adCampaignId);
}
