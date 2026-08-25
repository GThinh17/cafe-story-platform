import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  CreatePaymentRequest,
  PaymentResponse,
  PaymentStatus,
  VnpayReturnResponse,
} from "@/types/payment";

function withSearchParams(path: string, params: URLSearchParams) {
  const query = params.toString();

  return query ? `${path}?${query}` : path;
}

export function createPayment(request: CreatePaymentRequest) {
  return apiFetch<PaymentResponse>(apiEndpoints.payments.list, {
    method: "POST",
    body: request,
  });
}

export function getPayment(paymentId: string) {
  return apiFetch<PaymentResponse>(apiEndpoints.payments.byId(paymentId), {
    method: "GET",
  });
}

export function getMyPayments(paymentStatus?: PaymentStatus) {
  const path = paymentStatus
    ? `${apiEndpoints.payments.mine}?paymentStatus=${encodeURIComponent(String(paymentStatus))}`
    : apiEndpoints.payments.mine;
  return apiFetch<PaymentResponse[]>(path, { method: "GET" });
}

export function syncStripePayment(paymentId: string) {
  return apiFetch<PaymentResponse>(apiEndpoints.payments.stripeSync(paymentId), {
    method: "POST",
  });
}

export function handleVnpayReturn(params: URLSearchParams) {
  return apiFetch<VnpayReturnResponse>(
    withSearchParams(apiEndpoints.payments.vnpayReturn, params),
    {
      method: "GET",
    },
  );
}
