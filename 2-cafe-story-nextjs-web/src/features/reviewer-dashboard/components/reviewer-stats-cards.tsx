import {
  HeartIcon,
  MessageCircleIcon,
  Repeat2Icon,
  SparklesIcon,
} from "lucide-react";
import { Card } from "@/components/ui/card";
import type { ReviewerStats } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerStatsCardsProps = {
  stats: ReviewerStats;
};

const numberFormatter = new Intl.NumberFormat("en", {
  notation: "compact",
  maximumFractionDigits: 1,
});

export function ReviewerStatsCards({ stats }: ReviewerStatsCardsProps) {
  const cards = [
    { icon: HeartIcon, label: "Likes", value: stats.likeCount },
    { icon: Repeat2Icon, label: "Shares", value: stats.shareCount },
    { icon: MessageCircleIcon, label: "Comments", value: stats.commentCount },
    { icon: SparklesIcon, label: "Engagement Score", value: stats.score },
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
            Current {stats.period} period
          </p>
        </Card>
      ))}
    </section>
  );
}
