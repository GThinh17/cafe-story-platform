"use client";

import Link from "next/link";
import { useI18n } from "@/components/providers/locale-provider";
import { AvatarImage } from "@/components/ui/avatar-image";
import type { TopCafe } from "@/types/feed";

type TopCafesNearbyProps = {
  cafes: TopCafe[];
};

export function TopCafesNearby({ cafes }: TopCafesNearbyProps) {
  const { t } = useI18n();

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-base font-bold">{t("topCafes.title")}</h2>
        <Link className="text-xs font-bold text-foreground" href="/explore">
          {t("common.seeAll")}
        </Link>
      </div>
      <div className="space-y-4">
        {cafes.map((cafe) => (
          <Link
            className="flex items-center gap-3"
            href={cafe.id ? `/cafes/${cafe.id}` : "#"}
            key={cafe.id ?? cafe.name}
          >
            <span className="block size-11 shrink-0 overflow-hidden rounded-full border border-border bg-surface-muted">
              <AvatarImage alt={cafe.name} src={cafe.avatarUrl ?? null} />
            </span>
            <span className="min-w-0 flex-1">
              <span className="block truncate text-sm font-bold">
                {cafe.name}
              </span>
              <span className="block text-xs font-medium text-muted">
                {cafe.rating} - {cafe.type}
              </span>
            </span>
            <span className="text-xs font-bold text-primary">{t("topCafes.view")}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
