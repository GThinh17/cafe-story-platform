"use client";

import { useCallback, useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ReviewerPerformanceChart } from "@/components/reviewer-dashboard/reviewer-performance-chart";
import { ReviewerStatsCards } from "@/components/reviewer-dashboard/reviewer-stats-cards";
import type {
  ReviewerPerformancePoint,
  ReviewerPeriod,
  ReviewerStats,
} from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getReviewerBadges, getReviewerByUserId, getReviewerStats } from "@/lib/api/reviewers";
import type { ReviewerBadgeResponse, ReviewerStatsResponse } from "@/types/reviewer";
import { useI18n } from "@/components/providers/locale-provider";
import { LOCALE_HTML_LANG, type Locale } from "@/lib/i18n";


const periods: ReviewerPeriod[] = ["day", "week", "month", "3months"];

function toStats(s: ReviewerStatsResponse): ReviewerStats {
  return {
    reviewerId: s.reviewerId,
    period: s.period as ReviewerPeriod,
    likeCount: s.likeCount,
    shareCount: s.shareCount,
    commentCount: s.commentCount,
    score: s.score,
  };
}

function derivePerformance(
  badges: ReviewerBadgeResponse[],
  locale: Locale,
): ReviewerPerformancePoint[] {
  const monthFormatter = new Intl.DateTimeFormat(LOCALE_HTML_LANG[locale], {
    month: "short",
  });

  return [...badges].reverse().map((b) => {
    const monthIdx = parseInt(b.month.substring(5), 10) - 1;
    return {
      label: Number.isNaN(monthIdx)
        ? b.month.substring(5)
        : monthFormatter.format(new Date(2000, monthIdx, 1)),
      likes: Number(b.likeCount),
      shares: Number(b.shareCount),
      comments: Number(b.commentCount),
      score: Number(b.score),
    };
  });
}

export function ReviewerPerformanceDetail() {
  const { locale, t } = useI18n();
  const { user } = useCurrentUser();
  const [period, setPeriod] = useState<ReviewerPeriod>("month");
  const [reviewerId, setReviewerId] = useState<string | null>(null);
  const [stats, setStats] = useState<ReviewerStatsResponse | null>(null);
  const [statsLoading, setStatsLoading] = useState(true);
  const [performance, setPerformance] = useState<ReviewerPerformancePoint[]>([]);
  const [performanceLoading, setPerformanceLoading] = useState(true);

  const loadInitial = useCallback(async (userId: string) => {
    try {
      const reviewer = await getReviewerByUserId(userId);
      setReviewerId(reviewer.reviewerId);

      const [badgesData] = await Promise.allSettled([
        getReviewerBadges(reviewer.reviewerId),
      ]);
      if (badgesData.status === "fulfilled") {
        setPerformance(derivePerformance(badgesData.value ?? [], locale));
      }
    } catch {
      // reviewer not found
    } finally {
      setPerformanceLoading(false);
    }
  }, [locale]);

  useEffect(() => {
    if (user?.userId) void loadInitial(user.userId);
    else {
      setPerformanceLoading(false);
      setStatsLoading(false);
    }
  }, [user?.userId, loadInitial]);

  useEffect(() => {
    if (!reviewerId) return;
    setStatsLoading(true);
    void getReviewerStats(reviewerId, period)
      .then((s) => setStats(s))
      .catch(() => setStats(null))
      .finally(() => setStatsLoading(false));
  }, [period, reviewerId]);

  const bestMonth = performance.length > 0
    ? performance.reduce((best, item) => (item.score > best.score ? item : best))
    : null;
  const maxScore = performance.length > 0
    ? Math.max(...performance.map((item) => item.score), 1)
    : 1;

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.14em] text-primary">
            {t("reviewer.performance.detailEyebrow")}
          </p>
          <h2 className="mt-1 text-2xl font-black text-espresso">
            Engagement by period
          </h2>
        </div>
        <div className="flex flex-wrap gap-2">
          {periods.map((item) => (
            <Button
              className="capitalize"
              key={item}
              onClick={() => setPeriod(item)}
              size="sm"
              type="button"
              variant={period === item ? "default" : "outline"}
            >
              {item}
            </Button>
          ))}
        </div>
      </section>

      {statsLoading ? (
        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Card className="p-5" key={i}>
              <div className="h-24 animate-pulse rounded-md bg-surface-muted" />
            </Card>
          ))}
        </section>
      ) : stats ? (
        <ReviewerStatsCards stats={toStats(stats)} />
      ) : null}

      <section className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
        {performanceLoading ? (
          <Card className="flex items-center justify-center p-10">
            <Loader2 className="size-5 animate-spin text-muted-foreground" />
          </Card>
        ) : performance.length > 0 ? (
          <ReviewerPerformanceChart data={performance} />
        ) : (
          <Card className="flex items-center justify-center p-10 text-sm text-muted-foreground">
            {t("reviewer.performance.noChartData")}
          </Card>
        )}

        <Card className="p-5">
          <p className="text-sm font-black text-muted">
            {t("reviewer.performance.insight")}
          </p>
          {bestMonth ? (
            <>
              <h3 className="mt-2 text-xl font-black text-espresso">
                {t("reviewer.performance.bestMonth", { month: bestMonth.label })}
              </h3>
              <p className="mt-3 text-sm leading-6 text-coffee-muted">
                {t("reviewer.performance.bestMonthDescription", {
                  score: bestMonth.score,
                  likes: bestMonth.likes,
                  shares: bestMonth.shares,
                  comments: bestMonth.comments,
                })}
              </p>
            </>
          ) : (
            <p className="mt-3 text-sm text-muted-foreground">
              {t("reviewer.performance.noData")}
            </p>
          )}
        </Card>
      </section>

      <Card className="p-5">
        <h3 className="text-xl font-black text-espresso">
          {t("reviewer.performance.monthlyBreakdown")}
        </h3>
        {performance.length === 0 ? (
          <p className="mt-4 text-sm text-muted-foreground">
            {t("reviewer.performance.noMonthlyData")}
          </p>
        ) : (
          <div className="mt-5 overflow-x-auto">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead className="text-xs uppercase text-muted">
                <tr>
                  <th className="py-2 pr-3">{t("reviewer.table.month")}</th>
                  <th className="py-2 pr-3">{t("reviewer.table.likes")}</th>
                  <th className="py-2 pr-3">{t("reviewer.table.shares")}</th>
                  <th className="py-2 pr-3">{t("reviewer.table.comments")}</th>
                  <th className="py-2">{t("reviewer.table.score")}</th>
                </tr>
              </thead>
              <tbody>
                {performance.map((item) => (
                  <tr className="border-t border-line-soft" key={item.label}>
                    <td className="py-3 pr-3 font-black text-espresso">{item.label}</td>
                    <td className="py-3 pr-3 text-muted">{item.likes}</td>
                    <td className="py-3 pr-3 text-muted">{item.shares}</td>
                    <td className="py-3 pr-3 text-muted">{item.comments}</td>
                    <td className="py-3 font-black text-primary">
                      <div className="flex items-center gap-3">
                        <span className="w-12">{item.score}</span>
                        <span className="h-2 flex-1 overflow-hidden rounded-full bg-surface-muted">
                          <span
                            className="block h-full rounded-full bg-primary"
                            style={{ width: `${(item.score / maxScore) * 100}%` }}
                          />
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
