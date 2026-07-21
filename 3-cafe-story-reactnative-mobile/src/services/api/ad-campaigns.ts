import type {
  AdCampaignResponse,
  AdCampaignStatsResponse,
  CreateAdCampaignRequest,
} from "../../types";
import { invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getAdCampaigns(cafePageId: string) {
  const query = new URLSearchParams({ cafePageId });
  return apiFetch<AdCampaignResponse[]>(`${apiEndpoints.adCampaigns.list}?${query}`, {
    method: "GET",
  });
}

export function getAdCampaign(adCampaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.byId(adCampaignId), {
    method: "GET",
  });
}

export async function createAdCampaign(request: CreateAdCampaignRequest) {
  const response = await apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.list, {
    body: request,
    method: "POST",
  });
  invalidateApiCache("feed:mixed:");
  return response;
}

export function pauseAdCampaign(adCampaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.pause(adCampaignId), {
    method: "POST",
  });
}

export function activateAdCampaign(adCampaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.activate(adCampaignId), {
    method: "POST",
  });
}

export function getAdCampaignStats(adCampaignId: string) {
  return apiFetch<AdCampaignStatsResponse>(apiEndpoints.adCampaigns.stats(adCampaignId), {
    method: "GET",
  });
}

export function recordAdClick(adCampaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.clicks(adCampaignId), {
    method: "POST",
  }).finally(() => {
    invalidateApiCache("feed:mixed:");
  });
}
