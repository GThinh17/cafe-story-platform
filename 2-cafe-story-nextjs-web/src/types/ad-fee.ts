export type AdFeeType = "FEED_10000_IMPRESSIONS_OR_30_DAYS" | string;

export type AdFeeResponse = {
  adFeeId: string;
  feeType: AdFeeType;
  price: number;
  currency: string | null;
  status: boolean | null;
  createdAt: string | null;
  updatedAt: string | null;
};
