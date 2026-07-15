import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  CreatePaymentRequest,
  PaymentResponse,
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

export function getPayments(paymentStatus?: string) {
  const query = new URLSearchParams();
  if (paymentStatus) {
    query.set("paymentStatus", paymentStatus);
  }
  const queryString = query.toString();
  const path = queryString
    ? `${apiEndpoints.payments.list}?${queryString}`
    : apiEndpoints.payments.list;
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
