"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { Megaphone, PlayCircle, Sparkles } from "lucide-react";
import { useI18n } from "@/components/providers/locale-provider";
import { Button } from "@/components/ui/button";
import { PricingPlanModal } from "@/components/layout/pricing-plan-modal";
import {
  activateAdCampaign,
  getAdCampaignsByCafePage,
  pauseAdCampaign,
} from "@/lib/api/ad-campaigns";
import { ApiError } from "@/lib/api/client";
import { getMyPayments } from "@/lib/api/payments";
import { cn } from "@/lib/utils";
import { LOCALE_HTML_LANG, type TranslationKey } from "@/lib/i18n";
import type { AdCampaignResponse, AdStatus } from "@/types/ad-campaign";
import type { PaymentResponse } from "@/types/payment";

type AdCampaignsSectionProps = {
  cafePageId: string;
};

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) return error.message;
  return fallback;
}

const statusLabelKey: Record<AdStatus, TranslationKey> = {
  ACTIVE: "adStatus.ACTIVE",
  DRAFT: "adStatus.DRAFT",
  PAUSED: "adStatus.PAUSED",
  EXPIRED: "adStatus.EXPIRED",
  REJECTED: "adStatus.REJECTED",
};

const statusStyle: Record<AdStatus, string> = {
  ACTIVE: "bg-primary/15 text-primary-strong",
  DRAFT: "bg-surface-muted text-muted",
  PAUSED: "bg-surface-muted text-muted",
  EXPIRED: "bg-surface-muted text-muted",
  REJECTED: "bg-accent/15 text-accent",
};

export function AdCampaignsSection({ cafePageId }: AdCampaignsSectionProps) {
  const { locale, t } = useI18n();
  const [campaigns, setCampaigns] = useState<AdCampaignResponse[]>([]);
  const [payments, setPayments] = useState<PaymentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingCampaignId, setPendingCampaignId] = useState<string | null>(null);
  const [isPricingOpen, setIsPricingOpen] = useState(false);

  const refresh = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [nextCampaigns, nextPayments] = await Promise.all([
        getAdCampaignsByCafePage(cafePageId),
        getMyPayments("PAID"),
      ]);
      setCampaigns(nextCampaigns);
      setPayments(nextPayments);
    } catch (loadError) {
      setError(getErrorMessage(loadError, t("adCampaigns.loadError")));
    } finally {
      setIsLoading(false);
    }
  }, [cafePageId, t]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const availablePayments = useMemo(() => {
    const usedPaymentIds = new Set(campaigns.map((c) => c.paymentId));
    return payments.filter(
      (p) => Boolean(p.adFeeId) && !usedPaymentIds.has(p.paymentId),
    );
  }, [campaigns, payments]);

  async function togglePause(campaign: AdCampaignResponse) {
    setPendingCampaignId(campaign.adCampaignId);
    try {
      if (campaign.status === "ACTIVE") {
        await pauseAdCampaign(campaign.adCampaignId);
      } else if (campaign.status === "PAUSED" || campaign.status === "DRAFT") {
        await activateAdCampaign(campaign.adCampaignId);
      }
      await refresh();
    } catch (toggleError) {
      setError(getErrorMessage(toggleError, t("adCampaigns.updateError")));
    } finally {
      setPendingCampaignId(null);
    }
  }

  return (
    <div className="overflow-hidden rounded-md border border-border bg-surface">
      <div className="flex flex-wrap items-start justify-between gap-3 border-b border-border p-4 sm:p-5">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <Megaphone className="size-4 text-primary" />
            <h2 className="text-base font-bold text-foreground">
              {t("adCampaigns.title")}
            </h2>
          </div>
          <p className="mt-1 text-xs text-muted">
            {t("adCampaigns.description")}
          </p>
        </div>
        <Button
          onClick={() => setIsPricingOpen(true)}
          size="sm"
          type="button"
          variant="outline"
        >
          <Sparkles data-icon="inline-start" />
          {t("adCampaigns.buyPack")}
        </Button>
      </div>

      <div className="space-y-4 p-4 sm:p-5">
        {isLoading ? (
          <p className="text-sm text-muted">{t("adCampaigns.loading")}</p>
        ) : null}

        {error ? (
          <p className="text-sm text-accent">{error}</p>
        ) : null}

        {!isLoading && campaigns.length === 0 && availablePayments.length === 0 ? (
          <p className="text-sm text-muted">
            {t("adCampaigns.empty")}
          </p>
        ) : null}

        {campaigns.map((campaign) => {
          const progress =
            campaign.maxImpressions > 0
              ? Math.min(
                  100,
                  Math.round(
                    (campaign.servedImpressions / campaign.maxImpressions) * 100,
                  ),
                )
              : 0;
          const canToggle =
            campaign.status === "ACTIVE" ||
            campaign.status === "PAUSED" ||
            campaign.status === "DRAFT";

          return (
            <div
              className="rounded-md border border-border bg-background p-4"
              key={campaign.adCampaignId}
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0 space-y-1">
                  <p className="truncate text-sm font-bold text-foreground">
                    {campaign.title}
                  </p>
                  <p className="text-xs text-muted">
                    {t("adCampaigns.impressions", {
                      served: campaign.servedImpressions.toLocaleString(),
                      max: campaign.maxImpressions.toLocaleString(),
                    })}
                    {" · "}
                    {t("adCampaigns.delivered", { percent: progress })}
                  </p>
                </div>
                <span
                  className={cn(
                    "rounded-full px-2 py-0.5 text-[10px] font-black uppercase tracking-widest",
                    statusStyle[campaign.status],
                  )}
                >
                  {t(statusLabelKey[campaign.status])}
                </span>
              </div>
              {canToggle ? (
                <div className="mt-3">
                  <Button
                    disabled={pendingCampaignId === campaign.adCampaignId}
                    onClick={() => void togglePause(campaign)}
                    size="sm"
                    type="button"
                    variant="ghost"
                  >
                    {campaign.status === "ACTIVE"
                      ? t("adCampaigns.pause")
                      : t("adCampaigns.activate")}
                  </Button>
                </div>
              ) : null}
            </div>
          );
        })}

        {availablePayments.length > 0 ? (
          <div className="space-y-2">
            <p className="text-xs font-bold uppercase tracking-widest text-muted">
              {t("adCampaigns.readyToLaunch", {
                count: availablePayments.length,
              })}
            </p>
            {availablePayments.map((payment) => (
              <Link
                className="flex items-center justify-between gap-3 rounded-md border border-dashed border-border bg-background p-4 transition hover:border-primary hover:bg-primary/5"
                href={`/ads?paymentId=${encodeURIComponent(payment.paymentId)}`}
                key={payment.paymentId}
              >
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-foreground">
                    {t("adCampaigns.createFromPack")}
                  </p>
                  <p className="truncate text-xs text-muted">
                    {t("adCampaigns.paymentLine", {
                      id: payment.paymentId.slice(0, 8),
                      date: payment.paidAt
                        ? new Date(payment.paidAt).toLocaleDateString(
                            LOCALE_HTML_LANG[locale],
                          )
                        : "—",
                    })}
                  </p>
                </div>
                <PlayCircle className="size-5 text-primary" />
              </Link>
            ))}
          </div>
        ) : null}
      </div>

      <PricingPlanModal
        isOpen={isPricingOpen}
        onOpenChange={setIsPricingOpen}
      />
    </div>
  );
}
