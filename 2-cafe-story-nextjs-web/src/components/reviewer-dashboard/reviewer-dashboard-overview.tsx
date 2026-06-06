"use client";

import { useState } from "react";
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
  mockReviewerPayouts,
  mockReviewerPerformance,
  mockReviewerProfile,
  mockReviewerRanking,
  mockReviewerSegment,
  mockReviewerStatsByPeriod,
} from "@/features/reviewer-dashboard/reviewer-dashboard.mock";
import type { ReviewerPeriod } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

const periods: ReviewerPeriod[] = ["day", "week", "month", "3months"];

export function ReviewerDashboardOverview() {
  const [period, setPeriod] = useState<ReviewerPeriod>("month");
  const stats = mockReviewerStatsByPeriod[period];

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <ReviewerProfileSummary profile={mockReviewerProfile} />
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

      <ReviewerPayoutPanel payouts={mockReviewerPayouts} />
    </div>
  );
}
