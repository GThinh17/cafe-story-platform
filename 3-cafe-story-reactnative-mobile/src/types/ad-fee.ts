export type AdFeeType = "FEED_10000_IMPRESSIONS_OR_30_DAYS" | string;

export type AdFeeResponse = {
  adFeeId: string;
  createdAt?: string | null;
  currency?: string | null;
  feeType: AdFeeType;
  price: number | string;
  status?: boolean | null;
  updatedAt?: string | null;
};
