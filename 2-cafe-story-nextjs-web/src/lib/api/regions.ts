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
