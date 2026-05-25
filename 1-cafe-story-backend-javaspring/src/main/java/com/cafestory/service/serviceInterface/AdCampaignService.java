package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.requestDTO.RecordAdClickRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AdCampaignService {

    AdCampaignResponseDTO createCampaign(CreateAdCampaignRequestDTO request);

    AdCampaignResponseDTO getCampaign(UUID adCampaignId);

    List<AdCampaignResponseDTO> getCampaignsByCafePage(UUID cafePageId);

    AdCampaignResponseDTO pauseCampaign(UUID adCampaignId);

    AdCampaignResponseDTO activateCampaign(UUID adCampaignId);

    AdCampaignResponseDTO recordClick(UUID adCampaignId, RecordAdClickRequestDTO request);

    void expireActiveCampaigns();
}
