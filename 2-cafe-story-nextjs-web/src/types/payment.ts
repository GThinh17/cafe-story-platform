export type PaymentMethod = "STRIPE_CARD" | "BANK_TRANSFER" | "VNPAY";

export type PaymentStatus =
  | "PENDING"
  | "PAID"
  | "FAILED"
  | "CANCELLED"
  | "EXPIRED"
  | "REFUNDED"
  | string;

export type CreatePaymentRequest = {
  buyerId?: string;
  extraFeeId?: string;
  adFeeId?: string;
  cafePageId?: string;
  paymentMethod: PaymentMethod;
};

export type PaymentResponse = {
  paymentId: string;
  buyerId?: string | null;
  extraFeeId?: string | null;
  extraFeeType?: "REVIEWER_REGISTRATION" | "CAFE_PAGE_OPENING" | string | null;
  adFeeId?: string | null;
  activatedCafePageId?: string | null;
  paymentMethod: PaymentMethod | string;
  amount?: number | string | null;
  currency?: string | null;
  paymentStatus: PaymentStatus;
  paymentUrl?: string | null;
  qrCodeUrl?: string | null;
  transferContent?: string | null;
  createdAt?: string | null;
  paidAt?: string | null;
  expiredAt?: string | null;
};

export type VnpayReturnResponse = {
  paymentId?: string | null;
  status?: string | null;
  paymentStatus?: PaymentStatus | null;
  responseCode?: string | null;
  transactionStatus?: string | null;
  transactionNo?: string | null;
  message?: string | null;
};
