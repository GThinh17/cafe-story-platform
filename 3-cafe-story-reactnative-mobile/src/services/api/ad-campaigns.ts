import type { AdCampaignResponse } from "../../types";
import { invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function recordAdClick(adCampaignId: string, userId?: string | null) {
  return apiFetch<AdCampaignResponse>(apiEndpoints.adCampaigns.clicks(adCampaignId), {
    body: userId ? { userId } : {},
    method: "POST",
  }).finally(() => {
    invalidateApiCache("feed:mixed:");
  });
}
