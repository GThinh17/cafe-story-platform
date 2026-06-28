"use client";

import { useCallback, useEffect, useState } from "react";
import { ExternalLink, Loader2, Wifi } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ReviewerBadgeProgress } from "@/components/reviewer-dashboard/reviewer-badge-progress";
import { ReviewerLeaderboardPanel } from "@/components/reviewer-dashboard/reviewer-leaderboard-panel";
import { ReviewerPayoutPanel } from "@/components/reviewer-dashboard/reviewer-payout-panel";
import { ReviewerPerformanceChart } from "@/components/reviewer-dashboard/reviewer-performance-chart";
import { ReviewerRecentActivityPanel } from "@/components/reviewer-dashboard/reviewer-recent-activity-panel";
import { ReviewerSegmentCard } from "@/components/reviewer-dashboard/reviewer-segment-card";
import { ReviewerStatsCards } from "@/components/reviewer-dashboard/reviewer-stats-cards";
import type {
  ReviewerActivity,
  ReviewerBadge,
  ReviewerPayout,
  ReviewerPerformancePoint,
  ReviewerPeriod,
  ReviewerProfile,
  ReviewerRankingItem,
  ReviewerSegment,
  ReviewerSegmentItem,
  ReviewerStats,
} from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { useCurrentUser } from "@/hooks/use-current-user";
import {
  createOnboardingLink,
  getConnectStatus,
  getReviewerBadges,
  getReviewerByUserId,
  getReviewerPayouts,
  getReviewerRanking,
  getReviewerStats,
} from "@/lib/api/reviewers";
import type { AuthUser } from "@/types/auth";
import type {
  ReviewerBadgeResponse,
  ReviewerConnectStatus,
  ReviewerRankingResponse,
  ReviewerResponse,
  ReviewerStatsResponse,
} from "@/types/reviewer";

const MONTH_SHORT = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

const periods: ReviewerPeriod[] = ["day", "week", "month", "3months"];

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
    badge: (r.badge as ReviewerBadge) ?? "IRON",
    location: r.location ?? "",
  }));
}

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

function derivePerformance(badges: ReviewerBadgeResponse[]): ReviewerPerformancePoint[] {
  return [...badges].reverse().map((b) => {
    const monthIdx = parseInt(b.month.substring(5), 10) - 1;
    return {
      label: MONTH_SHORT[monthIdx] ?? b.month.substring(5),
      likes: Number(b.likeCount),
      shares: Number(b.shareCount),
      comments: Number(b.commentCount),
      score: Number(b.score),
    };
  });
}

function deriveSegment(
  ranking: ReviewerRankingResponse[],
  reviewerId: string,
  stats: ReviewerStatsResponse | null,
): ReviewerSegmentItem {
  const myRank = ranking.find((r) => r.reviewerId === reviewerId);
  const total = ranking.length;

  let segment: ReviewerSegment = "new";
  if (myRank && total > 0) {
    const pct = myRank.rank / total;
    if (pct <= 0.05) segment = "elite";
    else if (pct <= 0.15) segment = "top";
    else if (pct <= 0.35) segment = "strong";
    else if (pct <= 0.70) segment = "active";
    else segment = "new";
  } else if (!myRank && stats && stats.score === 0) {
    segment = "inactive";
  }

  return {
    reviewerId,
    segment,
    score: myRank ? Number(myRank.score) : (stats?.score ?? 0),
    likeCount: myRank ? Number(myRank.likeCount) : (stats?.likeCount ?? 0),
    shareCount: myRank ? Number(myRank.shareCount) : (stats?.shareCount ?? 0),
    commentCount: myRank ? Number(myRank.commentCount) : (stats?.commentCount ?? 0),
  };
}

function deriveActivities(
  badges: ReviewerBadgeResponse[],
  payouts: ReviewerPayout[],
  stats: ReviewerStatsResponse | null,
): ReviewerActivity[] {
  const activities: ReviewerActivity[] = [];

  const latestBadge = badges[0];
  if (latestBadge) {
    activities.push({
      id: `badge-${latestBadge.id}`,
      title: `${latestBadge.badge} badge – ${latestBadge.month}`,
      description: `Scored ${latestBadge.score} pts with ${latestBadge.likeCount} likes, ${latestBadge.shareCount} shares, ${latestBadge.commentCount} comments.`,
      time: latestBadge.month,
      type: "badge",
    });
  }

  const latestPayout = payouts[0];
  if (latestPayout) {
    activities.push({
      id: `payout-${latestPayout.id}`,
      title: `${latestPayout.payoutMonth} payout ${latestPayout.payoutStatus.toLowerCase()}`,
      description: `${new Intl.NumberFormat("vi-VN").format(latestPayout.totalAmount)}đ from likes, shares & comments.`,
      time: latestPayout.payoutMonth,
      type: "payout",
    });
  }

  if (stats && stats.likeCount > 0) {
    activities.push({
      id: "activity-likes",
      title: `${stats.likeCount} likes this ${stats.period}`,
      description: `Your reviews received ${stats.likeCount} likes in the current period.`,
      time: `This ${stats.period}`,
      type: "like",
    });
  }

  return activities;
}

export function ReviewerDashboardOverview() {
  const [period, setPeriod] = useState<ReviewerPeriod>("month");
  const { user } = useCurrentUser();

  const [reviewer, setReviewer] = useState<ReviewerResponse | null>(null);
  const [payouts, setPayouts] = useState<ReviewerPayout[]>([]);
  const [payoutsLoading, setPayoutsLoading] = useState(true);
  const [connectStatus, setConnectStatus] = useState<ReviewerConnectStatus | null>(null);
  const [connectLoading, setConnectLoading] = useState(true);
  const [ranking, setRanking] = useState<ReviewerRankingResponse[]>([]);
  const [rankingLoading, setRankingLoading] = useState(true);
  const [badges, setBadges] = useState<ReviewerBadgeResponse[]>([]);
  const [badgesLoading, setBadgesLoading] = useState(true);
  const [stats, setStats] = useState<ReviewerStatsResponse | null>(null);
  const [statsLoading, setStatsLoading] = useState(true);
  const [onboarding, setOnboarding] = useState(false);

  const loadDashboardData = useCallback(async (userId: string) => {
    try {
      const reviewerData = await getReviewerByUserId(userId);
      setReviewer(reviewerData);

      const [payoutData, connectData, rankingData, badgesData] = await Promise.allSettled([
        getReviewerPayouts(reviewerData.reviewerId),
        getConnectStatus(),
        getReviewerRanking("month"),
        getReviewerBadges(reviewerData.reviewerId),
      ]);

      if (payoutData.status === "fulfilled") setPayouts(payoutData.value ?? []);
      if (connectData.status === "fulfilled") setConnectStatus(connectData.value);
      if (rankingData.status === "fulfilled") setRanking(rankingData.value ?? []);
      if (badgesData.status === "fulfilled") setBadges(badgesData.value ?? []);
    } catch {
      // reviewer not found or API error — clear all loading states
      setStatsLoading(false);
    } finally {
      setPayoutsLoading(false);
      setConnectLoading(false);
      setRankingLoading(false);
      setBadgesLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user?.userId) {
      void loadDashboardData(user.userId);
    } else {
      setPayoutsLoading(false);
      setConnectLoading(false);
      setRankingLoading(false);
      setBadgesLoading(false);
      setStatsLoading(false);
    }
  }, [user?.userId, loadDashboardData]);

  // Re-fetch stats whenever period or reviewer changes
  useEffect(() => {
    if (!reviewer?.reviewerId) return;
    setStatsLoading(true);
    void getReviewerStats(reviewer.reviewerId, period)
      .then((s) => setStats(s))
      .catch(() => setStats(null))
      .finally(() => setStatsLoading(false));
  }, [period, reviewer?.reviewerId]);

  const handleOnboard = useCallback(async () => {
    setOnboarding(true);
    try {
      const result = await createOnboardingLink();
      window.location.href = result.onboardingUrl;
    } catch {
      setOnboarding(false);
    }
  }, []);

  const isConnected =
    connectStatus?.onboardingStatus === "COMPLETE" && connectStatus.payoutsEnabled;

  const profileForUI = reviewer ? toProfileForUI(reviewer, user) : null;
  const rankingItems = toRankingItems(ranking);
  const performance = derivePerformance(badges);
  const segmentItem = reviewer
    ? deriveSegment(ranking, reviewer.reviewerId, stats)
    : null;
  const activities = deriveActivities(badges, payouts, stats);

  const isLoading = !profileForUI && (badgesLoading || rankingLoading);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <Loader2 className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-wrap items-center justify-end gap-2">
        {!connectLoading && (
          <>
            {isConnected ? (
              <Badge className="gap-1.5" variant="outline">
                <Wifi className="size-3.5 text-green-500" />
                Payouts enabled
              </Badge>
            ) : (
              <Button
                disabled={onboarding}
                onClick={() => void handleOnboard()}
                size="sm"
                type="button"
                variant="outline"
              >
                {onboarding ? (
                  <Loader2 className="size-4 animate-spin" data-icon="inline-start" />
                ) : (
                  <ExternalLink className="size-4" data-icon="inline-start" />
                )}
                {onboarding ? "Redirecting..." : "Connect Stripe Account"}
              </Button>
            )}
          </>
        )}

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

      <section className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
        {performance.length > 0 ? (
          <ReviewerPerformanceChart data={performance} />
        ) : (
          !badgesLoading && (
            <Card className="flex items-center justify-center p-10 text-sm text-muted-foreground">
              No performance data yet
            </Card>
          )
        )}
        <ReviewerRecentActivityPanel activities={activities} />
      </section>

      <section className="grid gap-6 xl:grid-cols-3">
        {segmentItem && <ReviewerSegmentCard segment={segmentItem} />}
        {profileForUI && (
          <ReviewerLeaderboardPanel
            profile={profileForUI}
            ranking={rankingItems}
          />
        )}
        {profileForUI && (
          <ReviewerBadgeProgress
            badges={badges}
            currentScore={stats?.score}
            profile={profileForUI}
          />
        )}
      </section>

      {payoutsLoading ? (
        <div className="flex items-center justify-center py-8">
          <Loader2 className="size-5 animate-spin text-muted-foreground" />
        </div>
      ) : payouts.length > 0 ? (
        <ReviewerPayoutPanel payouts={payouts} />
      ) : null}
    </div>
  );
}
