import type { AdFeeResponse } from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getAdFees() {
  return cachedApiCall("ad-fees:all", apiCacheTtl.dynamic, () =>
    apiFetch<AdFeeResponse[]>(apiEndpoints.adFees.list, {
      method: "GET",
    }),
  );
}
