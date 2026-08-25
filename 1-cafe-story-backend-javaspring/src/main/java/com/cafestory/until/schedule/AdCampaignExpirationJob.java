package com.cafestory.until.schedule;

import com.cafestory.service.serviceInterface.AdCampaignService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class AdCampaignExpirationJob {

    private final AdCampaignService adCampaignService;

    public AdCampaignExpirationJob(AdCampaignService adCampaignService) {
        this.adCampaignService = adCampaignService;
    }

    @Scheduled(fixedRate = 900000, initialDelay = 60000)
    public void expireActiveCampaigns() {
        adCampaignService.expireActiveCampaigns();
    }
}
