export type ExtraFeeType = "REVIEWER_REGISTRATION" | "CAFE_PAGE_OPENING" | string;

export type ExtraFeeResponse = {
  extraFeeId: string;
  name: string;
  description: string | null;
  feeType: ExtraFeeType;
  price: number;
  durationMonths: number | null;
  maxMembers: number | null;
  status: boolean | null;
  createdAt: string | null;
  updatedAt: string | null;
};
