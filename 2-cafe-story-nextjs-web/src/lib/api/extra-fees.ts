import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { ExtraFeeResponse } from "@/types/extra-fee";

export function getExtraFees() {
  return apiFetch<ExtraFeeResponse[]>(apiEndpoints.extraFees.list, {
    method: "GET",
  });
}
