export type PaymentMethod = "STRIPE_CARD" | "BANK_TRANSFER" | "VNPAY";

export type CheckoutPaymentMethod = Extract<
  PaymentMethod,
  "STRIPE_CARD" | "VNPAY"
>;

export type PaymentStatus =
  | "PENDING"
  | "PAID"
  | "FAILED"
  | "CANCELLED"
  | "EXPIRED"
  | "REFUNDED"
  | string;

export type CreatePaymentRequest = {
  adFeeId?: string;
  buyerId?: string;
  extraFeeId?: string;
  paymentMethod: PaymentMethod;
};

export type PaymentResponse = {
  adFeeId?: string | null;
  amount?: number | string | null;
  buyerId?: string | null;
  createdAt?: string | null;
  currency?: string | null;
  expiredAt?: string | null;
  extraFeeId?: string | null;
  paidAt?: string | null;
  paymentId: string;
  paymentMethod: PaymentMethod | string;
  paymentStatus: PaymentStatus;
  paymentUrl?: string | null;
  qrCodeUrl?: string | null;
  transferContent?: string | null;
};

export type VnpayReturnResponse = {
  message?: string | null;
  paymentId?: string | null;
  paymentStatus?: PaymentStatus | null;
  responseCode?: string | null;
  status?: string | null;
  transactionNo?: string | null;
  transactionStatus?: string | null;
};
