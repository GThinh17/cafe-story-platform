package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
import com.cafestory.dto.responseDTO.AdCampaignStatsResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AdCampaignService {

    AdCampaignResponseDTO createCampaign(UUID requesterUserId, CreateAdCampaignRequestDTO request);

    AdCampaignResponseDTO getCampaign(UUID requesterUserId, UUID adCampaignId);

    List<AdCampaignResponseDTO> getCampaignsByCafePage(UUID requesterUserId, UUID cafePageId);

    AdCampaignResponseDTO pauseCampaign(UUID requesterUserId, UUID adCampaignId);

    AdCampaignResponseDTO activateCampaign(UUID requesterUserId, UUID adCampaignId);

    AdCampaignResponseDTO recordClick(UUID requesterUserId, UUID adCampaignId);

    AdCampaignStatsResponseDTO getCampaignStats(UUID requesterUserId, UUID adCampaignId);

    void expireActiveCampaigns();
}
