"use client";

import { useI18n } from "@/components/providers/locale-provider";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type {
  ReviewerBadge,
  ReviewerBadgeHistoryItem,
  ReviewerProfile,
} from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerBadgeProgressProps = {
  badges: ReviewerBadgeHistoryItem[];
  profile: ReviewerProfile;
  currentScore?: number;
};

const badgeThresholds: Record<ReviewerBadge, { max: number; min: number }> = {
  IRON: { min: 0, max: 99 },
  BRONZE: { min: 100, max: 299 },
  SILVER: { min: 300, max: 699 },
  GOLD: { min: 700, max: 1499 },
  DIAMOND: { min: 1500, max: Number.POSITIVE_INFINITY },
};

const badgeOrder: ReviewerBadge[] = ["IRON", "BRONZE", "SILVER", "GOLD", "DIAMOND"];

function getNextBadge(current: ReviewerBadge) {
  const index = badgeOrder.indexOf(current);

  return badgeOrder[index + 1];
}

export function ReviewerBadgeProgress({
  badges,
  profile,
  currentScore,
}: ReviewerBadgeProgressProps) {
  const { t } = useI18n();
  const score = currentScore ?? profile.score;
  const nextBadge = getNextBadge(profile.badge);
  const nextMin = nextBadge ? badgeThresholds[nextBadge].min : score;
  const currentMin = badgeThresholds[profile.badge].min;
  const progress =
    nextBadge && nextMin > currentMin
      ? Math.min(100, ((score - currentMin) / (nextMin - currentMin)) * 100)
      : 100;

  return (
    <Card className="p-5">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-black text-muted">
            {t("reviewer.badgeProgress.title")}
          </p>
          <h2 className="mt-1 text-xl font-black text-espresso">
            {profile.badge}
          </h2>
        </div>
        <Badge variant="rating">
          {t("reviewer.badgeProgress.scoreBadge", { score })}
        </Badge>
      </div>

      <div className="mt-5">
        <div className="flex justify-between text-xs font-semibold text-muted">
          <span>{profile.badge}</span>
          <span>
            {nextBadge
              ? t("reviewer.badgeProgress.next", { badge: nextBadge })
              : t("reviewer.badgeProgress.topReached")}
          </span>
        </div>
        <div className="mt-2 h-3 overflow-hidden rounded-full bg-surface-muted">
          <div
            className="h-full rounded-full bg-primary"
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>

      <div className="mt-6 flex flex-col gap-3">
        {badges.map((item) => (
          <div
            className="flex items-center justify-between gap-3 rounded-md bg-background px-3 py-3"
            key={item.id}
          >
            <div>
              <p className="text-sm font-black text-espresso">{item.month}</p>
              <p className="text-xs font-semibold text-muted">
                {t("reviewer.badgeProgress.engagementLine", {
                  likes: item.likeCount,
                  shares: item.shareCount,
                  comments: item.commentCount,
                })}
              </p>
            </div>
            <Badge variant="secondary">{item.badge}</Badge>
          </div>
        ))}
      </div>
    </Card>
  );
}
