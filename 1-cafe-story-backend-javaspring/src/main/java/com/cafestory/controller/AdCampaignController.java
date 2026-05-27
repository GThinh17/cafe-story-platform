package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.requestDTO.RecordAdClickRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
import com.cafestory.service.serviceInterface.AdCampaignService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ad-campaigns")
public class AdCampaignController {

    private final AdCampaignService adCampaignService;

    public AdCampaignController(AdCampaignService adCampaignService) {
        this.adCampaignService = adCampaignService;
    }

    @PostMapping
    public AdCampaignResponseDTO createCampaign(@Valid @RequestBody CreateAdCampaignRequestDTO request) {
        return adCampaignService.createCampaign(request);
    }

    @GetMapping("/{adCampaignId}")
    public AdCampaignResponseDTO getCampaign(@PathVariable UUID adCampaignId) {
        return adCampaignService.getCampaign(adCampaignId);
    }

    @GetMapping
    public List<AdCampaignResponseDTO> getCampaignsByCafePage(@RequestParam UUID cafePageId) {
        return adCampaignService.getCampaignsByCafePage(cafePageId);
    }

    @PostMapping("/{adCampaignId}/pause")
    public AdCampaignResponseDTO pauseCampaign(@PathVariable UUID adCampaignId) {
        return adCampaignService.pauseCampaign(adCampaignId);
    }

    @PostMapping("/{adCampaignId}/activate")
    public AdCampaignResponseDTO activateCampaign(@PathVariable UUID adCampaignId) {
        return adCampaignService.activateCampaign(adCampaignId);
    }

    @PostMapping("/{adCampaignId}/clicks")
    public AdCampaignResponseDTO recordClick(
            @PathVariable UUID adCampaignId,
            @RequestBody(required = false) RecordAdClickRequestDTO request) {
        return adCampaignService.recordClick(
                adCampaignId,
                request == null ? new RecordAdClickRequestDTO() : request);
    }
}
