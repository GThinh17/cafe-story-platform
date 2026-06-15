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

export type RegionRequirement = "FULL_ADDRESS" | "BLOG_LOCATION";

export type RegionRequest = {
  city?: string;
  cityCode?: string;
  province?: string;
  provinceCode?: string;
  street?: string;
  ward?: string;
  wardCode?: string;
};

export type RegionResponse = {
  area: string | null;
  city: string | null;
  cityCode: string | null;
  province: string | null;
  provinceCode: string | null;
  regionId: string;
  street: string | null;
  ward: string | null;
  wardCode: string | null;
};
