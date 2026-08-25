export type AdStatus = "DRAFT" | "ACTIVE" | "PAUSED" | "EXPIRED" | "REJECTED";

export type AdTargetRegionRequest = {
  province?: string | null;
  city?: string | null;
  area?: string | null;
  ward?: string | null;
};

export type AdTargetRegionResponse = {
  adTargetRegionId: string;
  province: string | null;
  city: string | null;
  area: string | null;
  ward: string | null;
};

export type CreateAdCampaignRequest = {
  paymentId: string;
  cafePageId: string;
  title: string;
  description?: string | null;
  imageUrl?: string | null;
  targetUrl?: string | null;
  priority?: number;
  targetRegions?: AdTargetRegionRequest[];
  activateNow?: boolean;
};

export type AdCampaignResponse = {
  adCampaignId: string;
  cafePageId: string;
  paymentId: string;
  title: string;
  description: string | null;
  imageUrl: string | null;
  targetUrl: string | null;
  status: AdStatus;
  startAt: string | null;
  endAt: string | null;
  priority: number;
  maxImpressions: number;
  servedImpressions: number;
  maxDurationDays: number;
  targetRegions: AdTargetRegionResponse[];
  createdAt: string | null;
  updatedAt: string | null;
};
