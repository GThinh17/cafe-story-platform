import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  AdCampaignResponse,
  CreateAdCampaignRequest,
} from "@/types/ad-campaign";

function withQuery(path: string, params: Record<string, string | undefined>) {
  const searchParams = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") {
      searchParams.set(key, value);
    }
  }
  const query = searchParams.toString();
  return query ? `${path}?${query}` : path;
}

export function getAdCampaignsByCafePage(cafePageId: string) {
  return apiFetch<AdCampaignResponse[]>(
    withQuery(apiEndpoints.adCampaigns.list, { cafePageId }),
    { method: "GET" },
  );
}

export function getAdCampaign(adCampaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.byId(adCampaignId), {
    method: "GET",
  });
}

export function createAdCampaign(request: CreateAdCampaignRequest) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.list, {
    method: "POST",
    body: request,
  });
}

export function pauseAdCampaign(adCampaignId: string) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.pause(adCampaignId), {
    method: "POST",
  });
}

export function activateAdCampaign(adCampaignId: string) {
  return apiFetch<AdCampaignResponse>(
    apiEndpoints.adCampaigns.activate(adCampaignId),
    { method: "POST" },
  );
}
