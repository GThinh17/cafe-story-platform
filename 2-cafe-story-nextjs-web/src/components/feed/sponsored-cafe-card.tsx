"use client";

import { ExternalLinkIcon, StoreIcon } from "lucide-react";
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
import { recordAdClick } from "@/lib/api/ads";
import { useI18n } from "@/components/providers/locale-provider";
import type { SponsoredCafeResponse } from "@/types/feed";

type SponsoredCafeCardProps = {
  ad: SponsoredCafeResponse;
};

export function SponsoredCafeCard({ ad }: SponsoredCafeCardProps) {
  const { t } = useI18n();
  const imageUrl = ad.imageUrl || ad.cafeCoverUrl || ad.cafeAvatarUrl;
  const destination = ad.targetUrl?.trim() || `/cafes/${encodeURIComponent(ad.cafePageId)}`;

  async function openDestination() {
    try {
      await recordAdClick(ad.campaignId);
    } catch {
      // Tracking must not prevent the advertiser destination from opening.
    } finally {
      window.location.assign(destination);
    }
  }

  return (
    <Card
      className="mx-auto w-[85%] max-w-full overflow-hidden shadow-none"
      data-testid="sponsored-cafe-card"
    >
      {imageUrl ? (
        <img
          alt={ad.headline || ad.cafeName || t("sponsored.fallbackCafeName")}
          className="aspect-square w-full object-cover"
          decoding="async"
          src={imageUrl}
        />
      ) : (
        <div
          aria-label={t("sponsored.placeholderAlt", {
            name: ad.cafeName || t("sponsored.fallbackCafeName"),
          })}
          className="flex aspect-square w-full flex-col items-center justify-center gap-2 bg-surface-muted px-4 text-center text-primary"
          role="img"
        >
          <StoreIcon aria-hidden="true" className="size-10" strokeWidth={1.8} />
          <span className="text-sm font-bold">
            {ad.cafeName || t("sponsored.fallbackCafeName")}
          </span>
        </div>
      )}
      <CardHeader className="flex flex-col gap-2 px-4 py-4">
        <Badge className="w-fit" variant="secondary">
          Sponsored
        </Badge>
        <CardTitle className="line-clamp-2 text-base font-bold">
          {ad.headline || ad.cafeName || t("sponsored.fallbackHeadline")}
        </CardTitle>
        <CardDescription className="truncate">
          {ad.cafeName || t("sponsored.fallbackPartner")}
        </CardDescription>
      </CardHeader>
      <CardContent className="px-4 pb-4 pt-0">
        <p className="line-clamp-2 text-sm leading-6 text-muted-foreground">
          {ad.description || t("sponsored.fallbackDescription")}
        </p>
      </CardContent>
      <CardFooter className="px-4 pb-4 pt-0">
        <Button
          aria-label={t("sponsored.ctaAria", {
            cta: ad.ctaLabel || t("sponsored.fallbackCta"),
            name: ad.cafeName || t("sponsored.fallbackCafeName"),
          })}
          className="w-full sm:w-auto"
          onClick={() => void openDestination()}
          type="button"
        >
          {ad.ctaLabel || t("sponsored.fallbackCta")}
          <ExternalLinkIcon data-icon="inline-end" />
        </Button>
      </CardFooter>
    </Card>
  );
}
