"use client";

import { useCallback, useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { Card } from "@/components/ui/card";
import { ReviewerLeaderboardPanel } from "@/components/reviewer-dashboard/reviewer-leaderboard-panel";
import type {
  ReviewerBadge,
  ReviewerProfile,
  ReviewerRankingItem,
} from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getReviewerByUserId, getReviewerRanking } from "@/lib/api/reviewers";
import type { AuthUser } from "@/types/auth";
import type { ReviewerRankingResponse, ReviewerResponse } from "@/types/reviewer";
import { useI18n } from "@/components/providers/locale-provider";

const numberFormatter = new Intl.NumberFormat("en", {
  notation: "compact",
  maximumFractionDigits: 1,
});

function toProfileForUI(reviewer: ReviewerResponse, authUser?: AuthUser | null): ReviewerProfile {
  return {
    reviewerId: reviewer.reviewerId,
    userId: reviewer.userId,
    role: reviewer.role ?? "REVIEWER",
    avatar: reviewer.avatar ?? authUser?.userAvatar ?? "",
    name: reviewer.name ?? authUser?.userFullName ?? authUser?.userName ?? "Reviewer",
    follower: reviewer.follower,
    follow: reviewer.follow,
    like: reviewer.like,
    badge: (reviewer.badge as ReviewerBadge) ?? "IRON",
    score: reviewer.score,
    region: { regionId: "", city: "", province: "", ward: "", area: "", street: "" },
  };
}

function toRankingItems(ranking: ReviewerRankingResponse[]): ReviewerRankingItem[] {
  return ranking.map((r) => ({
    rank: r.rank,
    reviewerId: r.reviewerId,
    score: r.score,
    likeCount: r.likeCount,
    shareCount: r.shareCount,
    commentCount: r.commentCount,
    location: r.location ?? "",
  }));
}

export function ReviewerRankingDetail() {
  const { t } = useI18n();
  const { user } = useCurrentUser();
  const [profileForUI, setProfileForUI] = useState<ReviewerProfile | null>(null);
  const [rankingItems, setRankingItems] = useState<ReviewerRankingItem[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async (userId: string, authUser: AuthUser | null) => {
    try {
      const [reviewer, ranking] = await Promise.all([
        getReviewerByUserId(userId),
        getReviewerRanking("month"),
      ]);
      setProfileForUI(toProfileForUI(reviewer, authUser));
      setRankingItems(toRankingItems(ranking ?? []));
    } catch {
      // reviewer not found
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user?.userId) void loadData(user.userId, user);
    else setLoading(false);
  }, [user?.userId, loadData]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <Loader2 className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  const current = profileForUI
    ? rankingItems.find((item) => item.reviewerId === profileForUI.reviewerId)
    : null;
  const maxScore = Math.max(...rankingItems.map((item) => item.score), 1);

  return (
    <div className="flex flex-col gap-6">
      <section>
        <p className="text-xs font-black uppercase tracking-[0.14em] text-primary">
          {t("reviewer.ranking.eyebrow")}
        </p>
        <h2 className="mt-1 text-2xl font-black text-espresso">
          {t("reviewer.ranking.subtitle")}
        </h2>
      </section>

      <section className="grid gap-6 xl:grid-cols-[340px_minmax(0,1fr)]">
        {profileForUI ? (
          <ReviewerLeaderboardPanel profile={profileForUI} ranking={rankingItems} />
        ) : (
          <Card className="flex items-center justify-center p-10 text-sm text-muted-foreground">
            {t("reviewer.ranking.noData")}
          </Card>
        )}
        <Card className="p-5">
          <p className="text-sm font-black text-muted">
            {t("reviewer.ranking.currentRank")}
          </p>
          <h3 className="mt-2 text-4xl font-black text-primary">
            #{current?.rank ?? "-"}
          </h3>
          <p className="mt-2 text-sm leading-6 text-coffee-muted">
            {t(
              current
                ? "reviewer.ranking.appears"
                : "reviewer.ranking.notAppears",
              { name: profileForUI?.name ?? t("reviewer.ranking.you") },
            )}
          </p>
          {rankingItems.length > 0 && (
            <div className="mt-6 flex flex-col gap-3">
              {rankingItems.map((item) => (
                <div className="flex items-center gap-3" key={item.reviewerId}>
                  <span className="w-10 text-sm font-black text-espresso">
                    #{item.rank}
                  </span>
                  <span className="h-3 flex-1 overflow-hidden rounded-full bg-surface-muted">
                    <span
                      className="block h-full rounded-full bg-primary"
                      style={{ width: `${(item.score / maxScore) * 100}%` }}
                    />
                  </span>
                  <span className="w-16 text-right text-sm font-black text-primary">
                    {numberFormatter.format(item.score)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </Card>
      </section>

      <Card className="p-5">
        <h3 className="text-xl font-black text-espresso">
          {t("reviewer.ranking.full")}
        </h3>
        {rankingItems.length === 0 ? (
          <p className="mt-4 text-sm text-muted-foreground">
            {t("reviewer.ranking.noRankingData")}
          </p>
        ) : (
          <div className="mt-5 overflow-x-auto">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead className="text-xs uppercase text-muted">
                <tr>
                  <th className="py-2 pr-3">{t("reviewer.table.rank")}</th>
                  <th className="py-2 pr-3">{t("reviewer.table.location")}</th>
                  <th className="py-2 pr-3">{t("reviewer.table.score")}</th>
                  <th className="py-2 pr-3">{t("reviewer.table.likes")}</th>
                  <th className="py-2 pr-3">{t("reviewer.table.shares")}</th>
                  <th className="py-2">{t("reviewer.table.comments")}</th>
                </tr>
              </thead>
              <tbody>
                {rankingItems.map((item) => {
                  const isCurrent = item.reviewerId === profileForUI?.reviewerId;
                  return (
                    <tr
                      className={`border-t border-line-soft ${isCurrent ? "bg-primary/10" : ""}`}
                      key={item.reviewerId}
                    >
                      <td className="py-3 pr-3 font-black text-espresso">#{item.rank}</td>
                      <td className="py-3 pr-3 text-muted">{item.location}</td>
                      <td className="py-3 pr-3 font-black text-primary">{item.score}</td>
                      <td className="py-3 pr-3 text-muted">{item.likeCount}</td>
                      <td className="py-3 pr-3 text-muted">{item.shareCount}</td>
                      <td className="py-3 text-muted">{item.commentCount}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
