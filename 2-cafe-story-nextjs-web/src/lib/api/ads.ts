import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  AdCampaignResponse,
  AdCampaignStatsResponse,
  AdFeeResponse,
  CreateAdCampaignRequest,
} from "@/types/ads";

export function getAdFees() {
  return apiFetch<AdFeeResponse[]>(apiEndpoints.adFees.list, { method: "GET" });
}

export function getAdCampaigns(cafePageId: string) {
  const query = new URLSearchParams({ cafePageId });
  return apiFetch<AdCampaignResponse[]>(`${apiEndpoints.adCampaigns.list}?${query}`, {
    method: "GET",
  });
}

export function createAdCampaign(request: CreateAdCampaignRequest) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.list, {
    body: request,
    method: "POST",
  });
}

export function pauseAdCampaign(campaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.pause(campaignId), {
    method: "POST",
  });
}

export function activateAdCampaign(campaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.activate(campaignId), {
    method: "POST",
  });
}

export function getAdCampaignStats(campaignId: string) {
  return apiFetch<AdCampaignStatsResponse>(apiEndpoints.adCampaigns.stats(campaignId), {
    method: "GET",
  });
}

export function recordAdClick(campaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.clicks(campaignId), {
    method: "POST",
  });
}
