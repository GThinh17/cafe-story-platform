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
import { useCurrentUser } from "@/hooks/use-current-user";
import { getAdFees } from "@/lib/api/ad-fees";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { ApiError } from "@/lib/api/client";
import { getExtraFees } from "@/lib/api/extra-fees";
import { createPayment } from "@/lib/api/payments";
import { useI18n } from "@/components/providers/locale-provider";
import type { TranslationKey } from "@/lib/i18n";
import { cn } from "@/lib/utils";
import type { AdFeeResponse, AdFeeType } from "@/types/ad-fee";
import type { ExtraFeeResponse, ExtraFeeType } from "@/types/extra-fee";
import type { PaymentMethod } from "@/types/payment";

type PricingPlanModalProps = {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
};

type MembershipPlan = {
  featureKeys: TranslationKey[];
  feeType: ExtraFeeType | AdFeeType;
  kind: "extra" | "ad";
  ownersOnly?: boolean;
  highlighted?: boolean;
  nameKey: TranslationKey;
  audienceKey: TranslationKey;
  ctaKey: TranslationKey;
  price: string;
  billingSuffixKey: TranslationKey;
};

type ModalStep = "plans" | "payment";

const membershipPlans: MembershipPlan[] = [
  {
    audienceKey: "pricing.audience.personal",
    ctaKey: "pricing.cta.choosePlan",
    feeType: "REVIEWER_REGISTRATION",
    kind: "extra",
    featureKeys: [
      "pricing.reviewer.feature1",
      "pricing.reviewer.feature2",
      "pricing.reviewer.feature3",
      "pricing.reviewer.feature4",
      "pricing.reviewer.feature5",
    ],
    nameKey: "pricing.reviewer.name",
    price: "199,000 VND",
    billingSuffixKey: "pricing.billing.perMonth",
  },
  {
    audienceKey: "pricing.audience.business",
    ctaKey: "pricing.cta.choosePlan",
    feeType: "CAFE_PAGE_OPENING",
    kind: "extra",
    featureKeys: [
      "pricing.owner.feature1",
      "pricing.owner.feature2",
      "pricing.owner.feature3",
      "pricing.owner.feature4",
      "pricing.owner.feature5",
    ],
    nameKey: "pricing.owner.name",
    price: "499,000 VND",
    billingSuffixKey: "pricing.billing.perMonth",
  },
  {
    audienceKey: "pricing.audience.boost",
    ctaKey: "pricing.cta.boost",
    feeType: "FEED_10000_IMPRESSIONS_OR_30_DAYS",
    kind: "ad",
    ownersOnly: true,
    highlighted: true,
    featureKeys: [
      "pricing.ad.feature1",
      "pricing.ad.feature2",
      "pricing.ad.feature3",
      "pricing.ad.feature4",
      "pricing.ad.feature5",
    ],
    nameKey: "pricing.ad.name",
    price: "299,000 VND",
    billingSuffixKey: "pricing.billing.perCampaign",
  },
];

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
  const { user } = useCurrentUser();
  const [step, setStep] = useState<ModalStep>("plans");
  const [extraFees, setExtraFees] = useState<ExtraFeeResponse[]>([]);
  const [adFees, setAdFees] = useState<AdFeeResponse[]>([]);
  const [ownsCafePage, setOwnsCafePage] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState<MembershipPlan | null>(null);
  const [isLoadingFees, setIsLoadingFees] = useState(false);
  const [isCreatingPayment, setIsCreatingPayment] = useState(false);
  const [activeMethod, setActiveMethod] = useState<PaymentMethod | null>(null);
  const [error, setError] = useState<string | null>(null);

  const activeExtraFees = useMemo(
    () => extraFees.filter((fee) => fee.status !== false),
    [extraFees],
  );
  const activeAdFees = useMemo(
    () => adFees.filter((fee) => fee.status !== false),
    [adFees],
  );

  const visiblePlans = useMemo(
    () => membershipPlans.filter((plan) => !plan.ownersOnly || ownsCafePage),
    [ownsCafePage],
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
        const [extra, ad] = await Promise.all([
          getExtraFees(),
          getAdFees().catch(() => [] as AdFeeResponse[]),
        ]);

        if (!isMounted) return;
        setExtraFees(extra);
        setAdFees(ad);
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

    async function loadOwnership() {
      if (!user?.userId) {
        if (isMounted) setOwnsCafePage(false);
        return;
      }
      try {
        const cafes = await getCafePagesByOwnerId(user.userId);
        if (isMounted) setOwnsCafePage(cafes.length > 0);
      } catch {
        if (isMounted) setOwnsCafePage(false);
      }
    }

    void loadPlans();
    void loadOwnership();

    return () => {
      isMounted = false;
    };
  }, [isOpen, user?.userId]);

  function getFeeForPlan(plan: MembershipPlan): {
    id: string;
    price: number;
    name: string;
    description: string | null;
  } | null {
    if (plan.kind === "ad") {
      const fee = activeAdFees.find((f) => f.feeType === plan.feeType);
      if (!fee) return null;
      return {
        id: fee.adFeeId,
        price: fee.price,
        name: t(plan.nameKey),
        description: null,
      };
    }
    const fee = activeExtraFees.find((f) => f.feeType === plan.feeType);
    if (!fee) return null;
    return {
      id: fee.extraFeeId,
      price: fee.price,
      name: fee.name,
      description: fee.description,
    };
  }

  function selectPlan(plan: MembershipPlan) {
    const fee = getFeeForPlan(plan);

    setError(null);
    if (!fee) {
      setError(t("pricing.unavailablePackage"));
      return;
    }

    setSelectedPlan(plan);
    setStep("payment");
  }

  async function payWith(method: Extract<PaymentMethod, "STRIPE_CARD" | "VNPAY">) {
    if (!selectedPlan) {
      setError(t("pricing.choosePackageFirst"));
      return;
    }

    const extraFee = getFeeForPlan(selectedPlan);

    if (!extraFee) {
      setError(t("pricing.unavailablePackage"));
      return;
    }

    setActiveMethod(method);
    setIsCreatingPayment(true);
    setError(null);

    try {
      const payment = await createPayment(
        selectedPlan.kind === "ad"
          ? { adFeeId: extraFee.id, paymentMethod: method }
          : { extraFeeId: extraFee.id, paymentMethod: method },
      );

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
      <DialogContent className="max-h-[min(95vh,860px)] w-[min(96vw,860px)] max-w-[900px] overflow-y-auto border-border bg-surface px-6 pb-7 pt-6 text-foreground sm:px-8">
        <DialogClose asChild>
          <Button
            aria-label={t("pricing.close")}
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
          <div className="mt-6 grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {visiblePlans.map((plan) => {
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
                  key={plan.nameKey}
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
                      {t(plan.audienceKey)}
                    </span>
                    <CardTitle className="mt-2 font-serif text-2xl leading-tight">
                      {fee?.name || t(plan.nameKey)}
                    </CardTitle>
                    <CardDescription
                      className={cn(
                        plan.highlighted
                          ? "text-primary-foreground/75"
                          : "text-muted",
                      )}
                    >
                      {fee?.description ||
                        t(
                          plan.kind === "ad"
                            ? "pricing.description.ad"
                            : "pricing.description.membership",
                        )}
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
                        {t(plan.billingSuffixKey)}
                      </span>
                    </p>

                    <ul className="mt-8 flex flex-1 flex-col gap-3.5">
                      {plan.featureKeys.map((featureKey) => (
                        <li
                          className="grid grid-cols-[18px_1fr] gap-3 text-sm leading-5"
                          key={featureKey}
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
                            {t(featureKey)}
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
                      {isLoadingFees ? t("common.loading") : t(plan.ctaKey)}
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
              {t("common.back")}
            </Button>

            <Card>
              <CardHeader>
                <CardTitle>{t("pricing.method.title")}</CardTitle>
                <CardDescription>
                  {selectedPlan
                    ? t("pricing.method.package", {
                        name:
                          getFeeForPlan(selectedPlan)?.name ||
                          t(selectedPlan.nameKey),
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
