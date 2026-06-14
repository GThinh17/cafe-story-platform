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

export type RegionRequest = {
  city?: string;
  cityCode?: string;
  province?: string;
  provinceCode?: string;
  street?: string;
  ward?: string;
  wardCode?: string;
};
