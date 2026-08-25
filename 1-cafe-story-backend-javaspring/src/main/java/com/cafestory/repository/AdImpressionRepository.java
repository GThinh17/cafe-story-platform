package com.cafestory.repository;

import com.cafestory.entity.AdImpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AdImpressionRepository extends JpaRepository<AdImpression, UUID> {

    long countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
            UUID userId,
            UUID adCampaignId,
            LocalDateTime startAt,
            LocalDateTime endAt);

    @Query("""
            select impression.adCampaign.adCampaignId as campaignId, count(impression) as impressionCount
            from AdImpression impression
            where impression.user.userId = :userId
            and impression.adCampaign.adCampaignId in :campaignIds
            and impression.shownAt >= :startAt
            and impression.shownAt < :endAt
            group by impression.adCampaign.adCampaignId
            """)
    List<CampaignImpressionCountRow> countByUserAndCampaignIdsBetween(
            @Param("userId") UUID userId,
            @Param("campaignIds") Collection<UUID> campaignIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    interface CampaignImpressionCountRow {
        UUID getCampaignId();

        long getImpressionCount();
    }
}
