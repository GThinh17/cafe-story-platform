"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { CreditCardIcon, MegaphoneIcon, RefreshCwIcon } from "lucide-react";
import { AdCampaignCard } from "@/components/ads/ad-campaign-card";
import { AdCampaignForm } from "@/components/ads/ad-campaign-form";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getAdCampaigns, getAdFees } from "@/lib/api/ads";
import { ApiError } from "@/lib/api/client";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { createPayment, getMyPayments } from "@/lib/api/payments";
import type { AdCampaignResponse, AdFeeResponse } from "@/types/ads";
import type { CafePageResponse } from "@/types/cafe";
import type { PaymentResponse } from "@/types/payment";
import type { TranslationKey } from "@/lib/i18n";
import { useI18n } from "@/components/providers/locale-provider";

type AdsDashboardProps = {
  checkoutStatus?: string | null;
  initialPaymentId?: string | null;
};

/**
 * Card copy is per fee type — the backend now serves four of them, so a single
 * hardcoded title would label every package as the feed impressions bundle.
 */
const AD_FEE_COPY: Record<
  string,
  { descriptionKey: TranslationKey; titleKey: TranslationKey }
> = {
  CAFE_AD_GROWTH: {
    descriptionKey: "ads.package.growth.description",
    titleKey: "ads.package.growth.title",
  },
  CAFE_AD_PREMIUM: {
    descriptionKey: "ads.package.premium.description",
    titleKey: "ads.package.premium.title",
  },
  CAFE_AD_STARTER: {
    descriptionKey: "ads.package.starter.description",
    titleKey: "ads.package.starter.title",
  },
  FEED_10000_IMPRESSIONS_OR_30_DAYS: {
    descriptionKey: "ads.package.description",
    titleKey: "ads.package.title",
  },
};

function formatFeeTypeLabel(feeType: string) {
  return feeType
    .split("_")
    .filter(Boolean)
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(" ");
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback;
}

function formatMoney(value: number | string, currency: string | null) {
  const amount = Number(value);
  if (!Number.isFinite(amount)) return `${value} ${currency || ""}`.trim();
  return new Intl.NumberFormat("vi-VN", {
    currency: currency || "VND",
    maximumFractionDigits: 0,
    style: "currency",
  }).format(amount);
}

export function AdsDashboard({ checkoutStatus, initialPaymentId }: AdsDashboardProps) {
  const { t } = useI18n();
  const { user, isLoading: isLoadingUser } = useCurrentUser();
  const [tab, setTab] = useState(initialPaymentId ? "campaigns" : "packages");
  const [fees, setFees] = useState<AdFeeResponse[]>([]);
  const [payments, setPayments] = useState<PaymentResponse[]>([]);
  const [cafePages, setCafePages] = useState<CafePageResponse[]>([]);
  const [campaigns, setCampaigns] = useState<AdCampaignResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreatingPayment, setIsCreatingPayment] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = useCallback(async () => {
    if (!user?.userId) {
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      // Settled, not all: one failing endpoint used to abort the whole load, so a
      // broken /api/ad-fees left cafePages empty and surfaced a misleading
      // "No owned cafe page" instead of the real error.
      const [pagesResult, feesResult, paymentsResult] = await Promise.allSettled([
        getCafePagesByOwnerId(user.userId),
        getAdFees(),
        getMyPayments("PAID"),
      ]);
      const failures: string[] = [];

      const ownedPages =
        pagesResult.status === "fulfilled" ? pagesResult.value : [];
      setCafePages(ownedPages);
      if (pagesResult.status === "rejected") {
        failures.push(getErrorMessage(pagesResult.reason, t("ads.loadError")));
      }

      if (feesResult.status === "fulfilled") {
        setFees(feesResult.value.filter((fee) => fee.status !== false));
      } else {
        setFees([]);
        failures.push(getErrorMessage(feesResult.reason, t("ads.loadError")));
      }

      if (paymentsResult.status === "fulfilled") {
        setPayments(
          paymentsResult.value.filter((payment) => Boolean(payment.adFeeId)),
        );
      } else {
        setPayments([]);
        failures.push(getErrorMessage(paymentsResult.reason, t("ads.loadError")));
      }

      const campaignResults = await Promise.allSettled(
        ownedPages.map((page) => getAdCampaigns(page.id)),
      );
      setCampaigns(
        campaignResults.flatMap((result) =>
          result.status === "fulfilled" ? result.value : [],
        ),
      );
      for (const result of campaignResults) {
        if (result.status === "rejected") {
          failures.push(getErrorMessage(result.reason, t("ads.loadError")));
        }
      }

      setError(failures.length ? [...new Set(failures)].join(" ") : null);
    } catch (loadError) {
      setError(getErrorMessage(loadError, t("ads.loadError")));
    } finally {
      setIsLoading(false);
    }
  }, [t, user?.userId]);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  const usedPaymentIds = useMemo(
    () => new Set(campaigns.map((campaign) => campaign.paymentId)),
    [campaigns],
  );
  const unusedPayments = useMemo(
    () => payments.filter((payment) => !usedPaymentIds.has(payment.paymentId)),
    [payments, usedPaymentIds],
  );

  async function purchasePackage(fee: AdFeeResponse) {
    setIsCreatingPayment(true);
    setError(null);
    try {
      const payment = await createPayment({
        adFeeId: fee.adFeeId,
        paymentMethod: "STRIPE_CARD",
      });
      if (!payment.paymentUrl?.trim()) {
        throw new Error(t("ads.checkoutUrlMissing"));
      }
      window.location.assign(payment.paymentUrl);
    } catch (paymentError) {
      setError(getErrorMessage(paymentError, t("ads.checkoutError")));
      setIsCreatingPayment(false);
    }
  }

  function replaceCampaign(updated: AdCampaignResponse) {
    setCampaigns((current) => current.map((campaign) =>
      campaign.adCampaignId === updated.adCampaignId ? updated : campaign,
    ));
  }

  function addCampaign(campaign: AdCampaignResponse) {
    setCampaigns((current) => [campaign, ...current]);
    setTab("campaigns");
  }

  if (isLoadingUser || isLoading) {
    return (
      <div className="grid gap-4">
        <Skeleton className="h-10 w-64" />
        <Skeleton className="h-56 w-full" />
        <Skeleton className="h-56 w-full" />
      </div>
    );
  }

  if (!user) {
    return (
      <Alert>
        <AlertTitle>{t("ads.signInRequired.title")}</AlertTitle>
        <AlertDescription>{t("ads.signInRequired.description")}</AlertDescription>
      </Alert>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      {checkoutStatus === "paid" ? (
        <Alert>
          <CreditCardIcon />
          <AlertTitle>{t("ads.paid.title")}</AlertTitle>
          <AlertDescription>{t("ads.paid.description")}</AlertDescription>
        </Alert>
      ) : null}

      {error ? (
        <Alert variant="destructive">
          <AlertTitle>{t("ads.dashboardUnavailable")}</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      ) : null}

      {cafePages.length === 0 ? (
        <Alert>
          <AlertTitle>{t("ads.noCafePage.title")}</AlertTitle>
          <AlertDescription>{t("ads.noCafePage.description")}</AlertDescription>
        </Alert>
      ) : null}

      <Tabs onValueChange={setTab} value={tab}>
        <TabsList variant="line">
          <TabsTrigger value="packages">{t("ads.tab.packages")}</TabsTrigger>
          <TabsTrigger value="campaigns">{t("ads.tab.campaigns")}</TabsTrigger>
        </TabsList>

        <TabsContent className="flex flex-col gap-4 pt-4" value="packages">
          {fees.length ? fees.map((fee) => {
            const copy = AD_FEE_COPY[fee.feeType];

            return (
              <Card key={fee.adFeeId}>
                <CardHeader>
                  <Badge className="w-fit" variant="secondary">
                    {t("ads.package.badge")}
                  </Badge>
                  <CardTitle>
                    {copy ? t(copy.titleKey) : formatFeeTypeLabel(fee.feeType)}
                  </CardTitle>
                  <CardDescription>
                    {copy
                      ? t(copy.descriptionKey)
                      : t("ads.package.genericDescription")}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <p className="text-3xl font-semibold">{formatMoney(fee.price, fee.currency)}</p>
                </CardContent>
                <CardFooter>
                  <Button disabled={isCreatingPayment || cafePages.length === 0} onClick={() => void purchasePackage(fee)} type="button">
                    <CreditCardIcon data-icon="inline-start" />
                    {isCreatingPayment
                      ? t("ads.package.opening")
                      : t("ads.package.pay")}
                  </Button>
                </CardFooter>
              </Card>
            );
          }) : (
            <Alert>
              <AlertTitle>{t("ads.package.none.title")}</AlertTitle>
              <AlertDescription>{t("ads.package.none.description")}</AlertDescription>
            </Alert>
          )}
        </TabsContent>

        <TabsContent className="flex flex-col gap-5 pt-4" value="campaigns">
          {unusedPayments.length ? (
            <Card>
              <CardHeader>
                <CardTitle>{t("ads.createCampaign.title")}</CardTitle>
                <CardDescription>{t("ads.createCampaign.description")}</CardDescription>
              </CardHeader>
              <CardContent>
                <AdCampaignForm
                  cafePages={cafePages}
                  initialPaymentId={initialPaymentId}
                  onCreated={addCampaign}
                  payments={unusedPayments}
                />
              </CardContent>
            </Card>
          ) : (
            <Alert>
              <MegaphoneIcon />
              <AlertTitle>{t("ads.noPackage.title")}</AlertTitle>
              <AlertDescription>{t("ads.noPackage.description")}</AlertDescription>
            </Alert>
          )}

          <div className="flex items-center justify-between gap-3">
            <h2 className="text-xl font-semibold">{t("ads.history.title")}</h2>
            <Button onClick={() => void loadDashboard()} type="button" variant="outline">
              <RefreshCwIcon data-icon="inline-start" /> {t("common.refresh")}
            </Button>
          </div>
          {campaigns.length ? campaigns.map((campaign) => (
            <AdCampaignCard campaign={campaign} key={campaign.adCampaignId} onChanged={replaceCampaign} />
          )) : (
            <Alert>
              <AlertTitle>{t("ads.history.empty.title")}</AlertTitle>
              <AlertDescription>{t("ads.history.empty.description")}</AlertDescription>
            </Alert>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
