export type AdStatus = "DRAFT" | "ACTIVE" | "PAUSED" | "EXPIRED" | "REJECTED" | string;

export type AdTargetRegionResponse = {
  area: string | null;
  city: string | null;
  province: string | null;
  ward: string | null;
};

export type AdCampaignResponse = {
  adCampaignId: string;
  cafePageId: string;
  createdAt: string | null;
  description: string | null;
  endAt: string | null;
  imageUrl: string | null;
  maxDurationDays: number | null;
  maxImpressions: number | null;
  paymentId: string;
  priority: number | null;
  servedImpressions: number | null;
  startAt: string | null;
  status: AdStatus | null;
  targetRegions: AdTargetRegionResponse[] | null;
  targetUrl: string | null;
  title: string | null;
  updatedAt: string | null;
};
