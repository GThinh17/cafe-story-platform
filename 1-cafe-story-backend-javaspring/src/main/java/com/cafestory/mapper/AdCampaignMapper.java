package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
import com.cafestory.dto.responseDTO.AdTargetRegionResponseDTO;
import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.AdTargetRegion;
import org.springframework.stereotype.Component;

@Component
public class AdCampaignMapper {

    public AdCampaignResponseDTO toResponse(AdCampaign campaign) {
        AdCampaignResponseDTO response = new AdCampaignResponseDTO();
        response.setAdCampaignId(campaign.getAdCampaignId());
        response.setCafePageId(campaign.getCafePage().getId());
        response.setPaymentId(campaign.getPayment() == null ? null : campaign.getPayment().getPaymentId());
        response.setTitle(campaign.getTitle());
        response.setDescription(campaign.getDescription());
        response.setImageUrl(campaign.getImageUrl());
        response.setTargetUrl(campaign.getTargetUrl());
        response.setStatus(campaign.getStatus());
        response.setStartAt(campaign.getStartAt());
        response.setEndAt(campaign.getEndAt());
        response.setPriority(campaign.getPriority());
        response.setMaxImpressions(campaign.getMaxImpressions());
        response.setServedImpressions(campaign.getServedImpressions());
        response.setMaxDurationDays(campaign.getMaxDurationDays());
        response.setCreatedAt(campaign.getCreatedAt());
        response.setUpdatedAt(campaign.getUpdatedAt());
        response.setTargetRegions(campaign.getTargetRegions().stream().map(this::toTargetRegionResponse).toList());
        return response;
    }

    private AdTargetRegionResponseDTO toTargetRegionResponse(AdTargetRegion targetRegion) {
        AdTargetRegionResponseDTO response = new AdTargetRegionResponseDTO();
        response.setAdTargetRegionId(targetRegion.getAdTargetRegionId());
        response.setProvince(targetRegion.getProvince());
        response.setCity(targetRegion.getCity());
        response.setArea(targetRegion.getArea());
        response.setWard(targetRegion.getWard());
        return response;
    }
}
