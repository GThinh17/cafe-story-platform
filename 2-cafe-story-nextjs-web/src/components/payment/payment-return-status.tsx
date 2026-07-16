"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { AlertCircle, CheckCircle2, RefreshCw } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useCurrentUser } from "@/hooks/use-current-user";
import { ApiError } from "@/lib/api/client";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { getPayment, handleVnpayReturn, syncStripePayment } from "@/lib/api/payments";
import type { PaymentResponse, PaymentStatus, VnpayReturnResponse } from "@/types/payment";

type SearchParamsValue = string | string[] | undefined;

type PaymentReturnStatusProps =
  | {
      flow: "stripe";
      paymentId?: string;
      searchParams?: never;
    }
  | {
      flow: "vnpay";
      paymentId?: never;
      searchParams: Record<string, SearchParamsValue>;
    };

type VerificationState = {
  error: string | null;
  isChecking: boolean;
  message: string;
  status: "checking" | "paid" | "pending" | "failed";
};

const paidStatuses = new Set(["PAID", "PAID_SUCCESS", "SUCCESS", "COMPLETED"]);

function isPaidStatus(status: PaymentStatus | null | undefined) {
  return paidStatuses.has(String(status ?? "").trim().toUpperCase());
}

function isSuccessfulVnpayReturn(response: VnpayReturnResponse) {
  return (
    isPaidStatus(response.paymentStatus) ||
    String(response.status ?? "").trim().toLowerCase() === "success"
  );
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) {
    return error.message;
  }

  return fallback;
}

function toUrlSearchParams(params: Record<string, SearchParamsValue>) {
  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (Array.isArray(value)) {
      value.forEach((item) => searchParams.append(key, item));
    } else if (value !== undefined) {
      searchParams.set(key, value);
    }
  }

  return searchParams;
}

async function resolveRedirectPath(payment: PaymentResponse, userId: string): Promise<string> {
  if (payment.adFeeId) {
    return `/cafes/campaigns/new?paymentId=${encodeURIComponent(payment.paymentId)}`;
  }

  if (payment.extraFeeType === "REVIEWER_REGISTRATION") {
    return "/reviewer-dashboard";
  }

  if (payment.extraFeeType === "CAFE_PAGE_OPENING") {
    try {
      const cafes = await getCafePagesByOwnerId(userId);
      const active = cafes.find((c) => c.pageActive === true || c.status === "ACTIVE");
      return active ? `/cafes/${active.id}` : "/cafes/edit";
    } catch {
      return "/cafes/edit";
    }
  }

  return "/";
}

export function PaymentReturnStatus(props: PaymentReturnStatusProps) {
  const router = useRouter();
  const { user } = useCurrentUser();
  const flow = props.flow;
  const paymentId = props.flow === "stripe" ? props.paymentId : undefined;
  const rawVnpayParams = props.flow === "vnpay" ? props.searchParams : null;
  const [state, setState] = useState<VerificationState>({
    error: null,
    isChecking: true,
    message: "Checking payment status...",
    status: "checking",
  });

  const vnpaySearchParams = useMemo(
    () => (rawVnpayParams ? toUrlSearchParams(rawVnpayParams) : null),
    [rawVnpayParams],
  );

  const verifyPayment = useCallback(async () => {
    setState({
      error: null,
      isChecking: true,
      message: "Checking payment status...",
      status: "checking",
    });

    try {
      if (flow === "stripe") {
        if (!paymentId) {
          setState({
            error: "Missing paymentId in the Stripe return URL.",
            isChecking: false,
            message: "Payment cannot be verified.",
            status: "failed",
          });
          return;
        }

        const payment = await getPayment(paymentId);

        if (isPaidStatus(payment.paymentStatus)) {
          setState({
            error: null,
            isChecking: false,
            message: "Payment verified. Redirecting...",
            status: "paid",
          });
          const redirectPath = await resolveRedirectPath(payment, user?.userId ?? "");
          router.replace(redirectPath);
          return;
        }

        setState({
          error: null,
          isChecking: false,
          message: `Current payment status: ${payment.paymentStatus}`,
          status: "pending",
        });
        return;
      }

      if (!vnpaySearchParams || Array.from(vnpaySearchParams.keys()).length === 0) {
        setState({
          error: "Missing VNPAY return parameters.",
          isChecking: false,
          message: "Payment cannot be verified.",
          status: "failed",
        });
        return;
      }

      const response = await handleVnpayReturn(vnpaySearchParams);

      if (isSuccessfulVnpayReturn(response)) {
        setState({
          error: null,
          isChecking: false,
          message: "Payment verified. Redirecting...",
          status: "paid",
        });
        const vnpayPaymentId = response.paymentId;
        if (vnpayPaymentId) {
          try {
            const payment = await getPayment(vnpayPaymentId);
            const redirectPath = await resolveRedirectPath(payment, user?.userId ?? "");
            router.replace(redirectPath);
            return;
          } catch {
            // fallback
          }
        }
        router.replace("/");
        return;
      }

      setState({
        error: response.message ?? null,
        isChecking: false,
        message: `VNPAY status: ${response.status ?? response.paymentStatus ?? "unknown"}`,
        status:
          String(response.status ?? "").toLowerCase() === "pending"
            ? "pending"
            : "failed",
      });
    } catch (error) {
      setState({
        error: getErrorMessage(error, "Unable to verify payment."),
        isChecking: false,
        message: "Payment verification failed.",
        status: "failed",
      });
    }
  }, [flow, paymentId, router, user?.userId, vnpaySearchParams]);

  const syncAndVerify = useCallback(async () => {
    if (!paymentId) return;

    setState({
      error: null,
      isChecking: true,
      message: "Syncing with Stripe...",
      status: "checking",
    });

    try {
      const payment = await syncStripePayment(paymentId);

      if (isPaidStatus(payment.paymentStatus)) {
        setState({
          error: null,
          isChecking: false,
          message: "Payment verified. Redirecting...",
          status: "paid",
        });
        const redirectPath = await resolveRedirectPath(payment, user?.userId ?? "");
        router.replace(redirectPath);
        return;
      }

      setState({
        error: null,
        isChecking: false,
        message: `Current payment status: ${payment.paymentStatus}`,
        status: "pending",
      });
    } catch (error) {
      setState({
        error: getErrorMessage(error, "Failed to sync payment status."),
        isChecking: false,
        message: "Sync failed.",
        status: "failed",
      });
    }
  }, [paymentId, router, user?.userId]);

  useEffect(() => {
    void verifyPayment();
  }, [verifyPayment]);

  const isPaid = state.status === "paid";
  const isStripePending = flow === "stripe" && state.status === "pending";

  return (
    <Card>
      <CardHeader>
        <CardTitle>
          {flow === "stripe" ? "Stripe payment" : "VNPAY payment"}
        </CardTitle>
        <CardDescription>{state.message}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <Alert variant={state.status === "failed" ? "destructive" : "default"}>
          {state.status === "failed" ? <AlertCircle /> : <CheckCircle2 />}
          <AlertTitle>
            {state.isChecking
              ? "Verification in progress"
              : isPaid
                ? "Payment verified"
                : "Payment not completed"}
          </AlertTitle>
          <AlertDescription>
            {state.error ??
              (isPaid
                ? "You will be redirected shortly."
                : isStripePending
                  ? "Stripe may still be processing. Click 'Check again' to sync."
                  : "You can check again after the payment provider finishes processing.")}
          </AlertDescription>
        </Alert>
      </CardContent>
      {!isPaid ? (
        <CardFooter>
          <Button
            disabled={state.isChecking}
            onClick={() => void (isStripePending ? syncAndVerify() : verifyPayment())}
            type="button"
          >
            <RefreshCw data-icon="inline-start" />
            {state.isChecking ? "Checking..." : "Check again"}
          </Button>
        </CardFooter>
      ) : null}
    </Card>
  );
}
