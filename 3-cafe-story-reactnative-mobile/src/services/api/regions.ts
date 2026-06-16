import type {
  RegionCityResponse,
  RegionProvinceResponse,
  RegionRequest,
  RegionRequirement,
  RegionResponse,
  RegionWardResponse,
} from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getRegionProvinces() {
  return apiFetch<RegionProvinceResponse[]>(apiEndpoints.regions.provinces, {
    method: "GET",
  });
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
  return apiFetch<RegionCityResponse[]>(apiEndpoints.regions.cities(provinceCode), {
    method: "GET",
  });
}

export function getRegionWards(params: { cityCode?: string; provinceCode?: string } = {}) {
  return apiFetch<RegionWardResponse[]>(apiEndpoints.regions.wards(params), {
    method: "GET",
  });
}
