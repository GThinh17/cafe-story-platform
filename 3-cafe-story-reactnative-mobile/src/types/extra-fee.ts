export type ExtraFeeType =
  | "REVIEWER_REGISTRATION"
  | "CAFE_PAGE_OPENING"
  | string;

export type ExtraFeeResponse = {
  createdAt?: string | null;
  description?: string | null;
  durationMonths?: number | null;
  extraFeeId: string;
  feeType: ExtraFeeType;
  maxMembers?: number | null;
  name: string;
  price: number;
  status?: boolean | null;
  updatedAt?: string | null;
};
