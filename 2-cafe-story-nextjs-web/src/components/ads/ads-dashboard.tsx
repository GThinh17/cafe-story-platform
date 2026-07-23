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

type AdsDashboardProps = {
  checkoutStatus?: string | null;
  initialPaymentId?: string | null;
};

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
      const [ownedPages, adFees, paidPayments] = await Promise.all([
        getCafePagesByOwnerId(user.userId),
        getAdFees(),
        getMyPayments("PAID"),
      ]);
      const campaignLists = await Promise.all(
        ownedPages.map((page) => getAdCampaigns(page.id)),
      );
      setCafePages(ownedPages);
      setFees(adFees.filter((fee) => fee.status !== false));
      setPayments(paidPayments.filter((payment) => Boolean(payment.adFeeId)));
      setCampaigns(campaignLists.flat());
    } catch (loadError) {
      setError(getErrorMessage(loadError, "Unable to load the Ads dashboard."));
    } finally {
      setIsLoading(false);
    }
  }, [user?.userId]);

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
        throw new Error("Stripe checkout URL was not returned.");
      }
      window.location.assign(payment.paymentUrl);
    } catch (paymentError) {
      setError(getErrorMessage(paymentError, "Unable to start Stripe checkout."));
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
        <AlertTitle>Sign in required</AlertTitle>
        <AlertDescription>Sign in with a cafe owner account to manage Ads.</AlertDescription>
      </Alert>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      {checkoutStatus === "paid" ? (
        <Alert>
          <CreditCardIcon />
          <AlertTitle>Ads payment verified</AlertTitle>
          <AlertDescription>Your paid package is ready for a new campaign.</AlertDescription>
        </Alert>
      ) : null}

      {error ? (
        <Alert variant="destructive">
          <AlertTitle>Ads dashboard unavailable</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      ) : null}

      {cafePages.length === 0 ? (
        <Alert>
          <AlertTitle>No owned cafe page</AlertTitle>
          <AlertDescription>Create and activate a cafe page before purchasing Ads.</AlertDescription>
        </Alert>
      ) : null}

      <Tabs onValueChange={setTab} value={tab}>
        <TabsList variant="line">
          <TabsTrigger value="packages">Packages</TabsTrigger>
          <TabsTrigger value="campaigns">Campaigns</TabsTrigger>
        </TabsList>

        <TabsContent className="flex flex-col gap-4 pt-4" value="packages">
          {fees.length ? fees.map((fee) => (
            <Card key={fee.adFeeId}>
              <CardHeader>
                <Badge className="w-fit" variant="secondary">Fixed Ads MVP</Badge>
                <CardTitle>10,000 served impressions or 30 days</CardTitle>
                <CardDescription>
                  Sponsored placement in the mixed feed. Delivery ends at whichever limit is reached first.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-3xl font-semibold">{formatMoney(fee.price, fee.currency)}</p>
              </CardContent>
              <CardFooter>
                <Button disabled={isCreatingPayment || cafePages.length === 0} onClick={() => void purchasePackage(fee)} type="button">
                  <CreditCardIcon data-icon="inline-start" />
                  {isCreatingPayment ? "Opening Stripe..." : "Pay with Stripe"}
                </Button>
              </CardFooter>
            </Card>
          )) : (
            <Alert>
              <AlertTitle>No active Ads package</AlertTitle>
              <AlertDescription>An administrator must activate an Ads fee before checkout.</AlertDescription>
            </Alert>
          )}
        </TabsContent>

        <TabsContent className="flex flex-col gap-5 pt-4" value="campaigns">
          {unusedPayments.length ? (
            <Card>
              <CardHeader>
                <CardTitle>Create campaign</CardTitle>
                <CardDescription>Use one paid, unused Ads package for one campaign.</CardDescription>
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
              <AlertTitle>No unused paid package</AlertTitle>
              <AlertDescription>Purchase an Ads package before creating another campaign.</AlertDescription>
            </Alert>
          )}

          <div className="flex items-center justify-between gap-3">
            <h2 className="text-xl font-semibold">Campaign history</h2>
            <Button onClick={() => void loadDashboard()} type="button" variant="outline">
              <RefreshCwIcon data-icon="inline-start" /> Refresh
            </Button>
          </div>
          {campaigns.length ? campaigns.map((campaign) => (
            <AdCampaignCard campaign={campaign} key={campaign.adCampaignId} onChanged={replaceCampaign} />
          )) : (
            <Alert>
              <AlertTitle>No campaigns yet</AlertTitle>
              <AlertDescription>Your draft and active Ads campaigns will appear here.</AlertDescription>
            </Alert>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
