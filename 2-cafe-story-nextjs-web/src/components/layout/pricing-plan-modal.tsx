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
import { useI18n } from "@/components/providers/locale-provider";
import type { TranslationKey } from "@/lib/i18n";
import type { ExtraFeeResponse, ExtraFeeType } from "@/types/extra-fee";
import type { PaymentMethod } from "@/types/payment";

type PricingPlanModalProps = {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
};

/**
 * Ad packages are bought on /ads, which fetches them itself — this modal only
 * sells extra fees and deliberately does not load ad fees.
 *
 * Cards are driven by whatever getExtraFees() returns: one card per active
 * package, so a backend that sells three tiers renders three cards without a
 * frontend change. Copy per fee type lives in `planPresentations`; an unknown
 * fee type still renders with the API's own name/description.
 */
type PlanPresentation = {
  audienceKey: TranslationKey;
  ctaKey: TranslationKey;
  featureKeys: TranslationKey[];
  nameKey: TranslationKey;
};

type PlanCard = {
  fee: ExtraFeeResponse;
  presentation: PlanPresentation;
};

const planPresentations: Partial<Record<ExtraFeeType, PlanPresentation>> = {
  CAFE_PAGE_OPENING: {
    audienceKey: "pricing.audience.business",
    ctaKey: "pricing.cta.choosePlan",
    featureKeys: [
      "pricing.owner.feature1",
      "pricing.owner.feature2",
      "pricing.owner.feature3",
      "pricing.owner.feature4",
      "pricing.owner.feature5",
    ],
    nameKey: "pricing.owner.name",
  },
  REVIEWER_REGISTRATION: {
    audienceKey: "pricing.audience.personal",
    ctaKey: "pricing.cta.choosePlan",
    featureKeys: [
      "pricing.reviewer.feature1",
      "pricing.reviewer.feature2",
      "pricing.reviewer.feature3",
      "pricing.reviewer.feature4",
      "pricing.reviewer.feature5",
    ],
    nameKey: "pricing.reviewer.name",
  },
};

const fallbackPresentation: PlanPresentation = {
  audienceKey: "pricing.audience.general",
  ctaKey: "pricing.cta.choosePlan",
  featureKeys: [],
  nameKey: "pricing.description.membership",
};

const paymentMethods: {
  descriptionKey: TranslationKey;
  label: string;
  method: Extract<PaymentMethod, "STRIPE_CARD" | "VNPAY">;
  icon: typeof CreditCard;
}[] = [
  {
    descriptionKey: "pricing.method.stripeDescription",
    icon: CreditCard,
    label: "Stripe",
    method: "STRIPE_CARD",
  },
  {
    descriptionKey: "pricing.method.vnpayDescription",
    icon: Landmark,
    label: "VNPAY",
    method: "VNPAY",
  },
];

type ModalStep = "plans" | "payment";

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
  const { t } = useI18n();
  const [step, setStep] = useState<ModalStep>("plans");
  const [extraFees, setExtraFees] = useState<ExtraFeeResponse[]>([]);
  const [selectedPlan, setSelectedPlan] = useState<PlanCard | null>(null);
  const [isLoadingFees, setIsLoadingFees] = useState(false);
  const [isCreatingPayment, setIsCreatingPayment] = useState(false);
  const [activeMethod, setActiveMethod] = useState<PaymentMethod | null>(null);
  const [error, setError] = useState<string | null>(null);

  /** Grouped by fee type, cheapest tier first inside each group. */
  const visiblePlans = useMemo<PlanCard[]>(
    () =>
      extraFees
        .filter((fee) => fee.status !== false)
        .slice()
        .sort(
          (a, b) =>
            a.feeType.localeCompare(b.feeType) || a.price - b.price,
        )
        .map((fee) => ({
          fee,
          presentation: planPresentations[fee.feeType] ?? fallbackPresentation,
        })),
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

    async function loadPlans() {
      setIsLoadingFees(true);
      setError(null);

      try {
        const extra = await getExtraFees();

        if (!isMounted) return;
        setExtraFees(extra);
      } catch (loadError) {
        if (isMounted) {
          setError(getErrorMessage(loadError, t("pricing.loadError")));
        }
      } finally {
        if (isMounted) {
          setIsLoadingFees(false);
        }
      }
    }

    void loadPlans();

    return () => {
      isMounted = false;
    };
  }, [isOpen]);

  function getBillingSuffix(fee: ExtraFeeResponse) {
    if (!fee.durationMonths) {
      return t("pricing.billing.oneTime");
    }

    return fee.durationMonths === 1
      ? t("pricing.billing.perMonth")
      : t("pricing.billing.perMonths", { count: fee.durationMonths });
  }

  function selectPlan(plan: PlanCard) {
    setError(null);
    setSelectedPlan(plan);
    setStep("payment");
  }

  async function payWith(method: Extract<PaymentMethod, "STRIPE_CARD" | "VNPAY">) {
    if (!selectedPlan) {
      setError(t("pricing.choosePackageFirst"));
      return;
    }

    setActiveMethod(method);
    setIsCreatingPayment(true);
    setError(null);

    try {
      const payment = await createPayment({
        extraFeeId: selectedPlan.fee.extraFeeId,
        paymentMethod: method,
      });

      if (!payment.paymentUrl?.trim()) {
        setError(t("pricing.paymentUrlMissing"));
        return;
      }

      window.location.href = payment.paymentUrl;
    } catch (paymentError) {
      setError(getErrorMessage(paymentError, t("pricing.createPaymentError")));
    } finally {
      setIsCreatingPayment(false);
      setActiveMethod(null);
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[min(95vh,860px)] w-[min(96vw,1032px)] max-w-[1080px] overflow-y-auto border-border bg-surface px-6 pb-7 pt-6 text-foreground sm:px-8">
        <DialogClose asChild>
          <Button
            aria-label={t("pricing.close")}
            className="absolute right-4 top-4 hover:text-coffee"
            size="icon-sm"
            type="button"
            variant="ghost"
          >
            <X />
          </Button>
        </DialogClose>

        <header className="mx-auto flex max-w-[840px] flex-col items-center gap-3 pr-7 text-center">
          <DialogTitle className="font-heading text-3xl font-bold leading-[1.08] text-foreground sm:text-4xl">
            {t("pricing.title")}
          </DialogTitle>
          <p className="text-sm leading-6 text-muted">
            {t("pricing.subtitle")}
          </p>
        </header>

        {error ? (
          <Alert className="mt-5" variant="destructive">
            <AlertTitle>{t("pricing.errorTitle")}</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        ) : null}

        {step === "plans" ? (
          <div className="mt-6 flex flex-wrap justify-center gap-5">
            {visiblePlans.map(({ fee, presentation }) => (
              <Card
                className="flex min-h-[500px] min-w-0 grow basis-[290px] flex-col bg-surface shadow-sm sm:max-w-[400px]"
                key={fee.extraFeeId}
              >
                <CardHeader>
                  <span className="w-fit rounded-full bg-coffee/12 px-3 py-1 text-[10px] font-black uppercase tracking-[0.14em] text-coffee">
                    {t(presentation.audienceKey)}
                  </span>
                  <CardTitle className="mt-2 text-2xl font-bold leading-tight">
                    {fee.name || t(presentation.nameKey)}
                  </CardTitle>
                  <CardDescription>
                    {fee.description || t("pricing.description.membership")}
                  </CardDescription>
                </CardHeader>
                <CardContent className="flex flex-1 flex-col">
                  <p className="flex items-end gap-1">
                    <span className="text-3xl font-black leading-none">
                      {formatVnd(fee.price)}
                    </span>
                    <span className="text-sm text-muted">
                      {getBillingSuffix(fee)}
                    </span>
                  </p>

                  <ul className="mt-8 flex flex-1 flex-col gap-3.5">
                    {presentation.featureKeys.map((featureKey) => (
                      <li
                        className="grid grid-cols-[18px_1fr] gap-3 text-sm leading-5"
                        key={featureKey}
                      >
                        <CheckCircle2
                          aria-hidden="true"
                          className="mt-0.5 text-coffee"
                        />
                        <span className="text-muted">{t(featureKey)}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
                <CardFooter>
                  <Button
                    className="h-12 w-full bg-coffee font-black text-coffee-foreground hover:bg-coffee-strong focus-visible:border-coffee focus-visible:ring-coffee/30"
                    disabled={isLoadingFees}
                    onClick={() => selectPlan({ fee, presentation })}
                    type="button"
                  >
                    {t(presentation.ctaKey)}
                  </Button>
                </CardFooter>
              </Card>
            ))}

            {!isLoadingFees && visiblePlans.length === 0 ? (
              <p className="py-10 text-sm text-muted">
                {t("pricing.emptyPlans")}
              </p>
            ) : null}

            {isLoadingFees && visiblePlans.length === 0 ? (
              <p className="py-10 text-sm text-muted">{t("common.loading")}</p>
            ) : null}
          </div>
        ) : (
          <div className="mt-6 flex flex-col gap-4">
            <Button
              className="w-fit hover:text-coffee"
              disabled={isCreatingPayment}
              onClick={() => {
                setStep("plans");
                setError(null);
              }}
              type="button"
              variant="ghost"
            >
              <ArrowLeft data-icon="inline-start" />
              {t("common.back")}
            </Button>

            <Card>
              <CardHeader>
                <CardTitle>{t("pricing.method.title")}</CardTitle>
                <CardDescription>
                  {selectedPlan
                    ? t("pricing.method.package", {
                        name:
                          selectedPlan.fee.name ||
                          t(selectedPlan.presentation.nameKey),
                      })
                    : t("pricing.method.selectHint")}
                </CardDescription>
              </CardHeader>
              <CardContent className="grid gap-3 sm:grid-cols-2">
                {paymentMethods.map((paymentMethod) => {
                  const Icon = paymentMethod.icon;
                  const isCurrentMethod = activeMethod === paymentMethod.method;

                  return (
                    <Button
                      className="h-auto min-h-28 justify-start p-4 text-left hover:border-coffee hover:text-coffee"
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
                              ? t("pricing.method.redirecting", {
                                  provider: paymentMethod.label,
                                })
                              : paymentMethod.label}
                          </span>
                          <span className="whitespace-normal text-sm font-normal leading-5 text-muted">
                            {t(paymentMethod.descriptionKey)}
                          </span>
                        </span>
                      </span>
                    </Button>
                  );
                })}
              </CardContent>
              <CardFooter>
                <Button
                  className="hover:text-coffee"
                  disabled={isCreatingPayment}
                  onClick={() => onOpenChange(false)}
                  type="button"
                  variant="ghost"
                >
                  {t("common.cancel")}
                </Button>
              </CardFooter>
            </Card>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
