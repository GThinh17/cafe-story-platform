package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
import com.cafestory.dto.responseDTO.AdCampaignStatsResponseDTO;
import com.cafestory.service.serviceInterface.AdCampaignService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/ad-campaigns")
public class AdCampaignController {

    private final AdCampaignService adCampaignService;

    public AdCampaignController(AdCampaignService adCampaignService) {
        this.adCampaignService = adCampaignService;
    }

    @PostMapping
    public AdCampaignResponseDTO createCampaign(
            @Valid @RequestBody CreateAdCampaignRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return adCampaignService.createCampaign(requireUserId(principal), request);
    }

    @GetMapping("/{adCampaignId}")
    public AdCampaignResponseDTO getCampaign(
            @PathVariable UUID adCampaignId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return adCampaignService.getCampaign(requireUserId(principal), adCampaignId);
    }

    @GetMapping
    public List<AdCampaignResponseDTO> getCampaignsByCafePage(
            @RequestParam UUID cafePageId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return adCampaignService.getCampaignsByCafePage(requireUserId(principal), cafePageId);
    }

    @PostMapping("/{adCampaignId}/pause")
    public AdCampaignResponseDTO pauseCampaign(
            @PathVariable UUID adCampaignId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return adCampaignService.pauseCampaign(requireUserId(principal), adCampaignId);
    }

    @PostMapping("/{adCampaignId}/activate")
    public AdCampaignResponseDTO activateCampaign(
            @PathVariable UUID adCampaignId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return adCampaignService.activateCampaign(requireUserId(principal), adCampaignId);
    }

    @PostMapping("/{adCampaignId}/clicks")
    public AdCampaignResponseDTO recordClick(
            @PathVariable UUID adCampaignId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return adCampaignService.recordClick(requireUserId(principal), adCampaignId);
    }

    @GetMapping("/{adCampaignId}/stats")
    public AdCampaignStatsResponseDTO getCampaignStats(
            @PathVariable UUID adCampaignId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return adCampaignService.getCampaignStats(requireUserId(principal), adCampaignId);
    }
}
