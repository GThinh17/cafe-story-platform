export type AdFeeType = "FEED_10000_IMPRESSIONS_OR_30_DAYS" | string;

export type AdFeeResponse = {
  adFeeId: string;
  feeType: AdFeeType;
  price: number | string;
  currency: string | null;
  status: boolean | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type AdStatus = "DRAFT" | "ACTIVE" | "PAUSED" | "EXPIRED" | "REJECTED" | string;

export type AdTargetRegionRequest = {
  province?: string | null;
  city?: string | null;
  area?: string | null;
  ward?: string | null;
};

export type AdTargetRegionResponse = AdTargetRegionRequest & {
  adTargetRegionId?: string | null;
};

export type CreateAdCampaignRequest = {
  paymentId: string;
  cafePageId: string;
  title: string;
  description?: string | null;
  imageUrl?: string | null;
  targetUrl?: string | null;
  targetRegions?: AdTargetRegionRequest[];
  activateNow: boolean;
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

export type AdDailyStatResponse = {
  statDate: string;
  impressions: number;
  clicks: number;
};

export type AdCampaignStatsResponse = {
  campaignId: string;
  servedImpressions: number;
  maxImpressions: number;
  remainingImpressions: number;
  totalClicks: number;
  ctrPercent: number;
  startAt: string | null;
  endAt: string | null;
  remainingDays: number;
  dailyStats: AdDailyStatResponse[];
};
