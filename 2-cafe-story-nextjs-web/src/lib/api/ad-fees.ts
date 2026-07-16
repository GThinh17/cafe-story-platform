import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { AdFeeResponse } from "@/types/ad-fee";

export function getAdFees() {
  return apiFetch<AdFeeResponse[]>(apiEndpoints.adFees.list, {
    method: "GET",
  });
}
