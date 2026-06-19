import type { ExtraFeeResponse } from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getExtraFees() {
  return cachedApiCall("extra-fees:active", apiCacheTtl.dynamic, () =>
    apiFetch<ExtraFeeResponse[]>(apiEndpoints.extraFees.list, {
      method: "GET",
    }),
  );
}
