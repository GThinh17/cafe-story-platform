import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";

export type RegionProvinceResponse = {
  name: string;
  provinceCode: string;
};

export type RegionCityResponse = {
  cityCode: string;
  name: string;
  provinceCode: string;
};

export type RegionWardResponse = {
  cityCode: string;
  name: string;
  provinceCode: string;
  wardCode: string;
};

export function getRegionProvinces() {
  return apiFetch<RegionProvinceResponse[]>(apiEndpoints.regions.provinces, {
    method: "GET",
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

export type RegionRequest = {
  city?: string;
  cityCode?: string;
  province?: string;
  provinceCode?: string;
  ward?: string;
  wardCode?: string;
};

export type RegionResponse = {
  city: string | null;
  cityCode: string | null;
  province: string | null;
  provinceCode: string | null;
  regionId: string;
  ward: string | null;
  wardCode: string | null;
};

export function createRegion(
  request: RegionRequest,
  requirement: "FULL_ADDRESS" | "BLOG_LOCATION" = "BLOG_LOCATION",
) {
  return apiFetch<RegionResponse>(apiEndpoints.regions.create(requirement), {
    body: request,
    method: "POST",
  });
}
