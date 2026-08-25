"use client";

import {
  HeartIcon,
  MessageCircleIcon,
  Repeat2Icon,
  SparklesIcon,
} from "lucide-react";


import { useI18n } from "@/components/providers/locale-provider";
import { Card } from "@/components/ui/card";
import {
  reviewerPeriodName,
  type ReviewerStats,
} from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerStatsCardsProps = {
  stats: ReviewerStats;
};

const numberFormatter = new Intl.NumberFormat("en", {
  notation: "compact",
  maximumFractionDigits: 1,
});

export function ReviewerStatsCards({ stats }: ReviewerStatsCardsProps) {
  const { t } = useI18n();
  const cards = [
    { icon: HeartIcon, label: t("reviewer.stats.likes"), value: stats.likeCount },
    { icon: Repeat2Icon, label: t("reviewer.stats.shares"), value: stats.shareCount },
    {
      icon: MessageCircleIcon,
      label: t("reviewer.stats.comments"),
      value: stats.commentCount,
    },
    { icon: SparklesIcon, label: t("reviewer.stats.score"), value: stats.score },
  ];

  return (
    <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map(({ icon: Icon, label, value }) => (
        <Card className="p-5" key={label}>
          <div className="flex items-center justify-between gap-3">
            <p className="text-sm font-black text-muted">{label}</p>
            <span className="grid size-10 place-items-center rounded-md bg-primary/10 text-primary">
              <Icon className="size-5" />
            </span>
          </div>
          <p className="mt-4 text-3xl font-black text-espresso">
            {numberFormatter.format(value)}
          </p>
          <p className="mt-2 text-xs font-semibold text-muted">
            {t("reviewer.stats.currentPeriod", {
              period: reviewerPeriodName(stats.period, t),
            })}
          </p>
        </Card>
      ))}
    </section>
  );
}
