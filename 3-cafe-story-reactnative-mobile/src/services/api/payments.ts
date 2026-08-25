import type {
  CreatePaymentRequest,
  PaymentResponse,
  VnpayReturnResponse,
} from "../../types";
import { invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

function withSearchParams(path: string, params: URLSearchParams) {
  const query = params.toString();

  return query ? `${path}?${query}` : path;
}

export async function createPayment(request: CreatePaymentRequest) {
  const response = await apiFetch<PaymentResponse>(apiEndpoints.payments.list, {
    body: request,
    method: "POST",
  });
  invalidateApiCache("payments:");
  return response;
}

export function getPayment(paymentId: string) {
  return apiFetch<PaymentResponse>(apiEndpoints.payments.byId(paymentId), {
    method: "GET",
  });
}

export function getPayments(paymentStatus?: string) {
  const query = new URLSearchParams();
  if (paymentStatus) query.set("paymentStatus", paymentStatus);
  const queryString = query.toString();
  const path = queryString ? `${apiEndpoints.payments.list}?${queryString}` : apiEndpoints.payments.list;
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
