"use client";

import { useCallback, useEffect, useState } from "react";
import { ExternalLink, Loader2, Wifi } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ReviewerBadgeProgress } from "@/components/reviewer-dashboard/reviewer-badge-progress";
import { ReviewerLeaderboardPanel } from "@/components/reviewer-dashboard/reviewer-leaderboard-panel";
import { ReviewerPayoutPanel } from "@/components/reviewer-dashboard/reviewer-payout-panel";
import { ReviewerPerformanceChart } from "@/components/reviewer-dashboard/reviewer-performance-chart";
import { ReviewerProfileSummary } from "@/components/reviewer-dashboard/reviewer-profile-summary";
import { ReviewerRecentActivityPanel } from "@/components/reviewer-dashboard/reviewer-recent-activity-panel";
import { ReviewerSegmentCard } from "@/components/reviewer-dashboard/reviewer-segment-card";
import { ReviewerStatsCards } from "@/components/reviewer-dashboard/reviewer-stats-cards";
import {
  mockReviewerActivities,
  mockReviewerBadges,
  mockReviewerPerformance,
  mockReviewerProfile,
  mockReviewerRanking,
  mockReviewerSegment,
  mockReviewerStatsByPeriod,
} from "@/features/reviewer-dashboard/reviewer-dashboard.mock";
import type { ReviewerPeriod, ReviewerPayout } from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { useCurrentUser } from "@/hooks/use-current-user";
import { createOnboardingLink, getConnectStatus, getReviewerByUserId, getReviewerPayouts } from "@/lib/api/reviewers";
import type { ReviewerConnectStatus } from "@/types/reviewer";

const periods: ReviewerPeriod[] = ["day", "week", "month", "3months"];

export function ReviewerDashboardOverview() {
  const [period, setPeriod] = useState<ReviewerPeriod>("month");
  const stats = mockReviewerStatsByPeriod[period];

  const { user } = useCurrentUser();
  const [payouts, setPayouts] = useState<ReviewerPayout[]>([]);
  const [payoutsLoading, setPayoutsLoading] = useState(true);
  const [connectStatus, setConnectStatus] = useState<ReviewerConnectStatus | null>(null);
  const [connectLoading, setConnectLoading] = useState(true);
  const [onboarding, setOnboarding] = useState(false);

  const loadDashboardData = useCallback(async (userId: string) => {
    try {
      const reviewer = await getReviewerByUserId(userId);

      const [payoutData, connectData] = await Promise.allSettled([
        getReviewerPayouts(reviewer.reviewerId),
        getConnectStatus(),
      ]);

      if (payoutData.status === "fulfilled") {
        setPayouts(payoutData.value);
      }
      if (connectData.status === "fulfilled") {
        setConnectStatus(connectData.value);
      }
    } catch {
      // reviewer not found or API error — silently skip
    } finally {
      setPayoutsLoading(false);
      setConnectLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user?.userId) {
      void loadDashboardData(user.userId);
    } else {
      setPayoutsLoading(false);
      setConnectLoading(false);
    }
  }, [user?.userId, loadDashboardData]);

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

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <ReviewerProfileSummary profile={mockReviewerProfile} />
        <div className="flex flex-wrap items-center gap-2">
          {/* Stripe Connect status */}
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

          {/* Period selector */}
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

      <ReviewerStatsCards stats={stats} />

      <section className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
        <ReviewerPerformanceChart data={mockReviewerPerformance} />
        <ReviewerRecentActivityPanel activities={mockReviewerActivities} />
      </section>

      <section className="grid gap-6 xl:grid-cols-3">
        <ReviewerSegmentCard segment={mockReviewerSegment} />
        <ReviewerLeaderboardPanel
          profile={mockReviewerProfile}
          ranking={mockReviewerRanking}
        />
        <ReviewerBadgeProgress
          badges={mockReviewerBadges}
          profile={mockReviewerProfile}
        />
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
