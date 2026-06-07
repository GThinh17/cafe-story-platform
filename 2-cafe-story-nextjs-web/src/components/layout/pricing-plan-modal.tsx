"use client";

import { useEffect, useMemo, useState } from "react";
import { ArrowLeft, CheckCircle2, CreditCard, Landmark, X } from "lucide-react";
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
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogTitle,
} from "@/components/ui/dialog";
import { ApiError } from "@/lib/api/client";
import { getExtraFees } from "@/lib/api/extra-fees";
import { createPayment } from "@/lib/api/payments";
import { cn } from "@/lib/utils";
import type { ExtraFeeResponse, ExtraFeeType } from "@/types/extra-fee";
import type { PaymentMethod } from "@/types/payment";

type PricingPlanModalProps = {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
};

type MembershipPlan = {
  audience: string;
  cta: string;
  features: string[];
  feeType: ExtraFeeType;
  highlighted?: boolean;
  name: string;
  price: string;
};

type ModalStep = "plans" | "payment";

const membershipPlans: MembershipPlan[] = [
  {
    audience: "Personal",
    cta: "Choose plan",
    feeType: "REVIEWER_REGISTRATION",
    features: [
      "Reviewer Pro badge on profile and reviews",
      "Priority placement in featured reviewer lists",
      "Per-review view, save, and engagement stats",
      "Reward eligibility from review views and engagement",
      "Access to partner tasting events and cafe offers",
    ],
    name: "Reviewer Membership",
    price: "199,000 VND",
  },
  {
    audience: "Business",
    cta: "Choose plan",
    feeType: "CAFE_PAGE_OPENING",
    features: [
      "Verified cafe profile with Official badge eligibility",
      "Cafe menu, opening hours, gallery, and booking details",
      "Promotion tools for nearby reviewers and customers",
      "Profile analytics for views, saves, and audience sources",
      "Up to one additional member to manage the cafe page",
    ],
    highlighted: true,
    name: "Cafe Owner Plan",
    price: "499,000 VND",
  },
];

const paymentMethods: {
  description: string;
  label: string;
  method: Extract<PaymentMethod, "STRIPE_CARD" | "VNPAY">;
  icon: typeof CreditCard;
}[] = [
  {
    description: "Pay with Stripe checkout using a card.",
    icon: CreditCard,
    label: "Stripe",
    method: "STRIPE_CARD",
  },
  {
    description: "Pay through the VNPAY hosted payment page.",
    icon: Landmark,
    label: "VNPAY",
    method: "VNPAY",
  },
];

function formatVnd(price: number) {
  return new Intl.NumberFormat("vi-VN", {
    currency: "VND",
    maximumFractionDigits: 0,
    style: "currency",
  }).format(price);
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) {
    return error.message;
  }

  return fallback;
}

export function PricingPlanModal({
  isOpen,
  onOpenChange,
}: PricingPlanModalProps) {
  const [step, setStep] = useState<ModalStep>("plans");
  const [extraFees, setExtraFees] = useState<ExtraFeeResponse[]>([]);
  const [selectedPlan, setSelectedPlan] = useState<MembershipPlan | null>(null);
  const [isLoadingFees, setIsLoadingFees] = useState(false);
  const [isCreatingPayment, setIsCreatingPayment] = useState(false);
  const [activeMethod, setActiveMethod] = useState<PaymentMethod | null>(null);
  const [error, setError] = useState<string | null>(null);

  const activeExtraFees = useMemo(
    () => extraFees.filter((fee) => fee.status !== false),
    [extraFees],
  );

  useEffect(() => {
    if (!isOpen) {
      setStep("plans");
      setSelectedPlan(null);
      setError(null);
      setActiveMethod(null);
      return;
    }

    let isMounted = true;

    async function loadExtraFees() {
      setIsLoadingFees(true);
      setError(null);

      try {
        const fees = await getExtraFees();

        if (isMounted) {
          setExtraFees(fees);
        }
      } catch (loadError) {
        if (isMounted) {
          setError(getErrorMessage(loadError, "Unable to load membership plans."));
        }
      } finally {
        if (isMounted) {
          setIsLoadingFees(false);
        }
      }
    }

    void loadExtraFees();

    return () => {
      isMounted = false;
    };
  }, [isOpen]);

  function getFeeForPlan(plan: MembershipPlan) {
    return activeExtraFees.find((fee) => fee.feeType === plan.feeType) ?? null;
  }

  function selectPlan(plan: MembershipPlan) {
    const fee = getFeeForPlan(plan);

    setError(null);
    if (!fee) {
      setError("This package is not available right now.");
      return;
    }

    setSelectedPlan(plan);
    setStep("payment");
  }

  async function payWith(method: Extract<PaymentMethod, "STRIPE_CARD" | "VNPAY">) {
    if (!selectedPlan) {
      setError("Please choose a package first.");
      return;
    }

    const extraFee = getFeeForPlan(selectedPlan);

    if (!extraFee) {
      setError("This package is not available right now.");
      return;
    }

    setActiveMethod(method);
    setIsCreatingPayment(true);
    setError(null);

    try {
      const payment = await createPayment({
        extraFeeId: extraFee.extraFeeId,
        paymentMethod: method,
      });

      if (!payment.paymentUrl?.trim()) {
        setError("Payment URL was not returned by the server.");
        return;
      }

      window.location.href = payment.paymentUrl;
    } catch (paymentError) {
      setError(getErrorMessage(paymentError, "Unable to create payment."));
    } finally {
      setIsCreatingPayment(false);
      setActiveMethod(null);
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[min(95vh,860px)] w-[min(96vw,860px)] max-w-[900px] overflow-y-auto border-border bg-surface px-6 pb-7 pt-6 text-foreground sm:px-8">
        <DialogClose asChild>
          <Button
            aria-label="Close membership plans"
            className="absolute right-4 top-4"
            size="icon-sm"
            type="button"
            variant="ghost"
          >
            <X />
          </Button>
        </DialogClose>

        <header className="mx-auto flex max-w-[700px] flex-col items-center gap-3 pr-7 text-center">
          <DialogTitle className="font-heading text-3xl font-bold leading-[1.08] text-foreground sm:text-4xl">
            Upgrade CafeStory
          </DialogTitle>
          <p className="text-sm leading-6 text-muted">
            Choose a package, then complete payment with Stripe or VNPAY.
          </p>
        </header>

        {error ? (
          <Alert className="mt-5" variant="destructive">
            <AlertTitle>Payment unavailable</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        ) : null}

        {step === "plans" ? (
          <div className="mt-6 grid gap-4 md:grid-cols-2">
            {membershipPlans.map((plan) => {
              const fee = getFeeForPlan(plan);
              const displayPrice = fee ? formatVnd(fee.price) : plan.price;
              const isUnavailable = isLoadingFees || !fee;

              return (
                <Card
                  className={cn(
                    "flex min-h-[500px] flex-col shadow-sm",
                    plan.highlighted
                      ? "border-primary bg-primary text-primary-foreground"
                      : "bg-surface",
                  )}
                  key={plan.name}
                >
                  <CardHeader>
                    <span
                      className={cn(
                        "w-fit rounded-full px-3 py-1 text-[10px] font-black uppercase tracking-[0.14em]",
                        plan.highlighted
                          ? "bg-primary-foreground/15 text-primary-foreground"
                          : "bg-muted/15 text-muted",
                      )}
                    >
                      {plan.audience}
                    </span>
                    <CardTitle className="mt-2 font-serif text-2xl leading-tight">
                      {fee?.name || plan.name}
                    </CardTitle>
                    <CardDescription
                      className={cn(
                        plan.highlighted
                          ? "text-primary-foreground/75"
                          : "text-muted",
                      )}
                    >
                      {fee?.description || "CafeStory membership package"}
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="flex flex-1 flex-col">
                    <p className="flex items-end gap-1">
                      <span className="font-serif text-3xl font-bold leading-none">
                        {displayPrice}
                      </span>
                      <span
                        className={cn(
                          "text-sm",
                          plan.highlighted
                            ? "text-primary-foreground/72"
                            : "text-muted",
                        )}
                      >
                        /month
                      </span>
                    </p>

                    <ul className="mt-8 flex flex-1 flex-col gap-3.5">
                      {plan.features.map((feature) => (
                        <li
                          className="grid grid-cols-[18px_1fr] gap-3 text-sm leading-5"
                          key={feature}
                        >
                          <CheckCircle2
                            aria-hidden="true"
                            className={cn(
                              "mt-0.5",
                              plan.highlighted
                                ? "text-primary-foreground"
                                : "text-primary",
                            )}
                          />
                          <span
                            className={cn(
                              plan.highlighted
                                ? "text-primary-foreground/88"
                                : "text-muted",
                            )}
                          >
                            {feature}
                          </span>
                        </li>
                      ))}
                    </ul>
                  </CardContent>
                  <CardFooter>
                    <Button
                      className="h-12 w-full font-black"
                      disabled={isUnavailable}
                      onClick={() => selectPlan(plan)}
                      type="button"
                      variant={plan.highlighted ? "secondary" : "default"}
                    >
                      {isLoadingFees ? "Loading..." : plan.cta}
                    </Button>
                  </CardFooter>
                </Card>
              );
            })}
          </div>
        ) : (
          <div className="mt-6 flex flex-col gap-4">
            <Button
              className="w-fit"
              disabled={isCreatingPayment}
              onClick={() => {
                setStep("plans");
                setError(null);
              }}
              type="button"
              variant="ghost"
            >
              <ArrowLeft data-icon="inline-start" />
              Back
            </Button>

            <Card>
              <CardHeader>
                <CardTitle>Choose payment method</CardTitle>
                <CardDescription>
                  {selectedPlan
                    ? `Package: ${getFeeForPlan(selectedPlan)?.name || selectedPlan.name}`
                    : "Select how you want to pay."}
                </CardDescription>
              </CardHeader>
              <CardContent className="grid gap-3 sm:grid-cols-2">
                {paymentMethods.map((paymentMethod) => {
                  const Icon = paymentMethod.icon;
                  const isCurrentMethod = activeMethod === paymentMethod.method;

                  return (
                    <Button
                      className="h-auto min-h-28 justify-start p-4 text-left"
                      disabled={isCreatingPayment}
                      key={paymentMethod.method}
                      onClick={() => payWith(paymentMethod.method)}
                      type="button"
                      variant="outline"
                    >
                      <span className="grid grid-cols-[24px_1fr] gap-3">
                        <Icon data-icon="inline-start" />
                        <span className="flex flex-col gap-1">
                          <span className="font-semibold">
                            {isCurrentMethod
                              ? `Redirecting to ${paymentMethod.label}...`
                              : paymentMethod.label}
                          </span>
                          <span className="whitespace-normal text-sm font-normal leading-5 text-muted">
                            {paymentMethod.description}
                          </span>
                        </span>
                      </span>
                    </Button>
                  );
                })}
              </CardContent>
              <CardFooter>
                <Button
                  disabled={isCreatingPayment}
                  onClick={() => onOpenChange(false)}
                  type="button"
                  variant="ghost"
                >
                  Cancel
                </Button>
              </CardFooter>
            </Card>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
