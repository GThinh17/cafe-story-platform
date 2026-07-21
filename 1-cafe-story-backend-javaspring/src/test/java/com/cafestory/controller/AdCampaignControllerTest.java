package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
import com.cafestory.dto.responseDTO.AdCampaignStatsResponseDTO;
import com.cafestory.service.serviceInterface.AdCampaignService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdCampaignControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CAMPAIGN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CAFE_PAGE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock private AdCampaignService adCampaignService;
    @InjectMocks private AdCampaignController controller;

    @Test
    void campaignEndpoints_success_delegateWithAuthenticatedPrincipal_TC001() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(USER_ID, "owner", List.of("USER"));
        CreateAdCampaignRequestDTO request = new CreateAdCampaignRequestDTO();
        AdCampaignResponseDTO campaign = new AdCampaignResponseDTO();
        campaign.setAdCampaignId(CAMPAIGN_ID);
        AdCampaignStatsResponseDTO stats = new AdCampaignStatsResponseDTO();
        stats.setCampaignId(CAMPAIGN_ID);

        when(adCampaignService.createCampaign(USER_ID, request)).thenReturn(campaign);
        when(adCampaignService.getCampaign(USER_ID, CAMPAIGN_ID)).thenReturn(campaign);
        when(adCampaignService.getCampaignsByCafePage(USER_ID, CAFE_PAGE_ID)).thenReturn(List.of(campaign));
        when(adCampaignService.pauseCampaign(USER_ID, CAMPAIGN_ID)).thenReturn(campaign);
        when(adCampaignService.activateCampaign(USER_ID, CAMPAIGN_ID)).thenReturn(campaign);
        when(adCampaignService.recordClick(USER_ID, CAMPAIGN_ID)).thenReturn(campaign);
        when(adCampaignService.getCampaignStats(USER_ID, CAMPAIGN_ID)).thenReturn(stats);

        assertThat(controller.createCampaign(request, principal)).isEqualTo(campaign);
        assertThat(controller.getCampaign(CAMPAIGN_ID, principal)).isEqualTo(campaign);
        assertThat(controller.getCampaignsByCafePage(CAFE_PAGE_ID, principal)).containsExactly(campaign);
        assertThat(controller.pauseCampaign(CAMPAIGN_ID, principal)).isEqualTo(campaign);
        assertThat(controller.activateCampaign(CAMPAIGN_ID, principal)).isEqualTo(campaign);
        assertThat(controller.recordClick(CAMPAIGN_ID, principal)).isEqualTo(campaign);
        assertThat(controller.getCampaignStats(CAMPAIGN_ID, principal)).isEqualTo(stats);

        verify(adCampaignService).createCampaign(USER_ID, request);
        verify(adCampaignService).recordClick(USER_ID, CAMPAIGN_ID);
        verify(adCampaignService).getCampaignStats(USER_ID, CAMPAIGN_ID);
    }
}
