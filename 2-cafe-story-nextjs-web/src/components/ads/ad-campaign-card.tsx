"use client";

import { useState } from "react";
import { BarChart3Icon, PauseIcon, PlayIcon } from "lucide-react";
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
import { activateAdCampaign, getAdCampaignStats, pauseAdCampaign } from "@/lib/api/ads";
import { ApiError } from "@/lib/api/client";
import type { AdCampaignResponse, AdCampaignStatsResponse } from "@/types/ads";
import { useI18n } from "@/components/providers/locale-provider";
import { LOCALE_HTML_LANG } from "@/lib/i18n";

type AdCampaignCardProps = {
  campaign: AdCampaignResponse;
  onChanged: (campaign: AdCampaignResponse) => void;
};

function formatDate(value: string | null, locale: string, notStarted: string) {
  if (!value) return notStarted;
  return new Intl.DateTimeFormat(locale, { dateStyle: "medium" }).format(
    new Date(value),
  );
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof ApiError || error instanceof Error
    ? error.message
    : fallback;
}

export function AdCampaignCard({ campaign, onChanged }: AdCampaignCardProps) {
  const { locale, t } = useI18n();
  const dateLocale = LOCALE_HTML_LANG[locale];
  const notStarted = t("adCard.notStarted");
  const [stats, setStats] = useState<AdCampaignStatsResponse | null>(null);
  const [isLoadingStats, setIsLoadingStats] = useState(false);
  const [isMutating, setIsMutating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadStats() {
    setIsLoadingStats(true);
    setError(null);
    try {
      setStats(await getAdCampaignStats(campaign.adCampaignId));
    } catch (loadError) {
      setError(getErrorMessage(loadError, t("adCard.actionFailed")));
    } finally {
      setIsLoadingStats(false);
    }
  }

  async function changeStatus() {
    setIsMutating(true);
    setError(null);
    try {
      const updated = campaign.status === "ACTIVE"
        ? await pauseAdCampaign(campaign.adCampaignId)
        : await activateAdCampaign(campaign.adCampaignId);
      onChanged(updated);
    } catch (mutationError) {
      setError(getErrorMessage(mutationError, t("adCard.actionFailed")));
    } finally {
      setIsMutating(false);
    }
  }

  return (
    <Card>
      {campaign.imageUrl ? (
        <img alt={campaign.title} className="aspect-[16/7] w-full object-cover" src={campaign.imageUrl} />
      ) : null}
      <CardHeader>
        <div className="flex flex-wrap items-center justify-between gap-2">
          <Badge variant={campaign.status === "ACTIVE" ? "default" : "secondary"}>
            {campaign.status}
          </Badge>
          <span className="text-xs text-muted-foreground">
            {formatDate(campaign.startAt, dateLocale, notStarted)} –{" "}
            {formatDate(campaign.endAt, dateLocale, notStarted)}
          </span>
        </div>
        <CardTitle>{campaign.title}</CardTitle>
        <CardDescription>
          {campaign.description || t("adCard.noDescription")}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {error ? (
          <Alert variant="destructive">
            <AlertTitle>{t("adCard.actionFailedTitle")}</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        ) : null}

        {isLoadingStats ? (
          <div className="grid gap-3 sm:grid-cols-3">
            <Skeleton className="h-20" />
            <Skeleton className="h-20" />
            <Skeleton className="h-20" />
          </div>
        ) : stats ? (
          <div className="flex flex-col gap-4">
            <div className="grid gap-3 sm:grid-cols-3">
              <Stat
                label={t("adCard.stat.served")}
                value={`${stats.servedImpressions.toLocaleString()} / ${stats.maxImpressions.toLocaleString()}`}
              />
              <Stat
                label={t("adCard.stat.clicks")}
                value={stats.totalClicks.toLocaleString()}
              />
              <Stat
                label={t("adCard.stat.ctr")}
                value={`${Number(stats.ctrPercent).toFixed(2)}%`}
              />
            </div>
            <div className="grid gap-2">
              {stats.dailyStats.length ? stats.dailyStats.map((day) => (
                <div className="grid grid-cols-3 gap-2 rounded-lg bg-muted p-3 text-xs" key={day.statDate}>
                  <span>{day.statDate}</span>
                  <span>
                    {t("adCard.daily.served", {
                      count: day.impressions.toLocaleString(),
                    })}
                  </span>
                  <span>
                    {t("adCard.daily.clicks", {
                      count: day.clicks.toLocaleString(),
                    })}
                  </span>
                </div>
              )) : (
                <p className="text-sm text-muted-foreground">
                  {t("adCard.noDeliveryData")}
                </p>
              )}
            </div>
          </div>
        ) : null}
      </CardContent>
      <CardFooter className="flex flex-wrap gap-2">
        <Button disabled={isLoadingStats} onClick={() => void loadStats()} type="button" variant="outline">
          <BarChart3Icon data-icon="inline-start" />
          {stats ? t("adCard.refreshStats") : t("adCard.viewStats")}
        </Button>
        {campaign.status === "ACTIVE" || campaign.status === "PAUSED" || campaign.status === "DRAFT" ? (
          <Button disabled={isMutating} onClick={() => void changeStatus()} type="button" variant="outline">
            {campaign.status === "ACTIVE" ? <PauseIcon data-icon="inline-start" /> : <PlayIcon data-icon="inline-start" />}
            {isMutating
              ? t("adCard.updating")
              : campaign.status === "ACTIVE"
                ? t("adCampaigns.pause")
                : t("adCampaigns.activate")}
          </Button>
        ) : null}
      </CardFooter>
    </Card>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-muted p-3">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className="mt-1 text-lg font-semibold">{value}</p>
    </div>
  );
}
