import type {
  RegionCityResponse,
  RegionProvinceResponse,
  RegionRequest,
  RegionRequirement,
  RegionResponse,
  RegionWardResponse,
} from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getRegionProvinces() {
  return cachedApiCall("regions:provinces", apiCacheTtl.lookup, () =>
    apiFetch<RegionProvinceResponse[]>(apiEndpoints.regions.provinces, {
      method: "GET",
    }),
  );
}

export function createRegion(
  request: RegionRequest,
  requirement: RegionRequirement = "FULL_ADDRESS",
) {
  return apiFetch<RegionResponse>(apiEndpoints.regions.create(requirement), {
    body: request,
    method: "POST",
  });
}

export function getRegionCities(provinceCode?: string) {
  return cachedApiCall(`regions:cities:${provinceCode ?? "all"}`, apiCacheTtl.lookup, () =>
    apiFetch<RegionCityResponse[]>(apiEndpoints.regions.cities(provinceCode), {
      method: "GET",
    }),
  );
}

export function getRegionWards(params: { cityCode?: string; provinceCode?: string } = {}) {
  return cachedApiCall(
    `regions:wards:${params.provinceCode ?? "all"}:${params.cityCode ?? "all"}`,
    apiCacheTtl.lookup,
    () =>
      apiFetch<RegionWardResponse[]>(apiEndpoints.regions.wards(params), {
        method: "GET",
      }),
  );
}
