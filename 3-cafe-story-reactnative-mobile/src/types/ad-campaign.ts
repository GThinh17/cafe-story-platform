export type AdStatus = "DRAFT" | "ACTIVE" | "PAUSED" | "EXPIRED" | "REJECTED" | string;

export type AdTargetRegionResponse = {
  area: string | null;
  city: string | null;
  province: string | null;
  ward: string | null;
};

export type AdTargetRegionRequest = {
  area?: string | null;
  city?: string | null;
  province?: string | null;
  ward?: string | null;
};

export type CreateAdCampaignRequest = {
  activateNow: boolean;
  cafePageId: string;
  description?: string | null;
  imageUrl?: string | null;
  paymentId: string;
  targetRegions?: AdTargetRegionRequest[];
  targetUrl?: string | null;
  title: string;
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

export type AdDailyStatResponse = {
  clicks: number;
  impressions: number;
  statDate: string;
};

export type AdCampaignStatsResponse = {
  campaignId: string;
  ctrPercent: number;
  dailyStats: AdDailyStatResponse[];
  endAt: string | null;
  maxImpressions: number;
  remainingDays: number;
  remainingImpressions: number;
  servedImpressions: number;
  startAt: string | null;
  totalClicks: number;
};
