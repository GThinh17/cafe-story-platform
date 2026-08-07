"use client";

import { TrophyIcon } from "lucide-react";
import { useI18n } from "@/components/providers/locale-provider";
import { Card } from "@/components/ui/card";
import type {
  ReviewerProfile,
  ReviewerRankingItem,
} from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerLeaderboardPanelProps = {
  profile: ReviewerProfile;
  ranking: ReviewerRankingItem[];
};

const numberFormatter = new Intl.NumberFormat("en", {
  notation: "compact",
  maximumFractionDigits: 1,
});

export function ReviewerLeaderboardPanel({
  profile,
  ranking,
}: ReviewerLeaderboardPanelProps) {
  const { t } = useI18n();
  const currentRank = ranking.find(
    (item) => item.reviewerId === profile.reviewerId,
  );

  return (
    <Card className="p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-sm font-black text-muted">
            {t("reviewer.leaderboard.title")}
          </p>
          <h2 className="mt-1 text-xl font-black text-espresso">
            {t("reviewer.leaderboard.rankThisMonth", {
              rank: currentRank?.rank ?? "-",
            })}
          </h2>
        </div>
        <span className="grid size-12 place-items-center rounded-md bg-rating/15 text-rating">
          <TrophyIcon className="size-6" />
        </span>
      </div>

      <div className="mt-5 flex flex-col gap-3">
        {ranking.map((item) => {
          const isCurrent = item.reviewerId === profile.reviewerId;

          return (
            <div
              className={`flex items-center justify-between gap-3 rounded-md border px-3 py-3 ${
                isCurrent
                  ? "border-primary bg-primary/10"
                  : "border-border bg-background"
              }`}
              key={item.reviewerId}
            >
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <p className="text-sm font-black text-espresso">
                    #{item.rank}
                  </p>
                  {isCurrent ? (
                    <span className="rounded-md bg-primary px-2 py-0.5 text-xs font-black text-primary-foreground">
                      {t("reviewer.leaderboard.you")}
                    </span>
                  ) : null}
                </div>
                <p className="mt-1 truncate text-xs font-semibold text-muted">
                  {t("reviewer.leaderboard.scoreLine", {
                    location: item.location,
                    score: numberFormatter.format(item.score),
                  })}
                </p>
              </div>
              <p className="shrink-0 text-sm font-black text-primary">
                {t("reviewer.leaderboard.comments", {
                  count: numberFormatter.format(item.commentCount),
                })}
              </p>
            </div>
          );
        })}
      </div>
    </Card>
  );
}
