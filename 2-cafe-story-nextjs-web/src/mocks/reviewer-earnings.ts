import type { ReviewerPayout } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

/**
 * Demo payout history for screenshots of the reviewer earnings page while the
 * monthly payout job has not produced rows yet.
 *
 * Amounts follow the real active formula (ReviewerFormulaDataInitializer) and
 * the real two-step aggregation (AdminPayoutServiceImpl.generateMonthlyPayout):
 *   likeAmount       = likeCount    x 100đ
 *   shareAmount      = shareCount   x 300đ
 *   commentAmount    = commentCount x 500đ
 *   totalBaseAmount  = likeAmount + shareAmount + commentAmount
 *   totalFinalAmount = totalBaseAmount x badgeMultiplier  (truncated, longValue)
 *
 * Counts are engagement the reviewer's own blogs RECEIVED, not engagement they
 * handed out. Badge follows the monthly ranking snapshot, so it climbs as the
 * months improve; multipliers are IRON 1.0 / BRONZE 1.2 / SILVER 1.5 / GOLD 2.0
 * / DIAMOND 3.0.
 *
 * `payoutMonth` is `yyyy-MM` and rows are ordered newest first. The newest
 * month sits at PENDING because the admin has not approved it yet; older
 * months are PAID. Statuses come from the AdminPayoutStatus enum
 * (PENDING | APPROVED | PAID | CANCELLED).
 */
const demoReviewerId = "8f2c1d64-4a37-4f1e-9b52-6c0d3e7a9151";

export const mockReviewerPayouts: ReviewerPayout[] = [
  {
    id: "b1f0a7c2-1d54-4f80-9a1e-2c7b48d90f11",
    reviewerId: demoReviewerId,
    payoutMonth: "2026-07",
    likeCount: 412,
    shareCount: 96,
    commentCount: 148,
    likeAmount: 41_200,
    shareAmount: 28_800,
    commentAmount: 74_000,
    totalBaseAmount: 144_000,
    badge: "GOLD",
    badgeMultiplier: 2,
    totalFinalAmount: 288_000,
    status: "PENDING",
    paidAt: null,
  },
  {
    id: "c2a91e37-8b46-4d13-8f75-5e1a06c4b722",
    reviewerId: demoReviewerId,
    payoutMonth: "2026-06",
    likeCount: 365,
    shareCount: 82,
    commentCount: 131,
    likeAmount: 36_500,
    shareAmount: 24_600,
    commentAmount: 65_500,
    totalBaseAmount: 126_600,
    badge: "GOLD",
    badgeMultiplier: 2,
    totalFinalAmount: 253_200,
    status: "PAID",
    paidAt: "2026-07-03T04:12:00",
  },
  {
    id: "d3b8250e-6f92-4c07-a3d8-91b7f24e5c33",
    reviewerId: demoReviewerId,
    payoutMonth: "2026-05",
    likeCount: 298,
    shareCount: 71,
    commentCount: 109,
    likeAmount: 29_800,
    shareAmount: 21_300,
    commentAmount: 54_500,
    totalBaseAmount: 105_600,
    badge: "SILVER",
    badgeMultiplier: 1.5,
    totalFinalAmount: 158_400,
    status: "PAID",
    paidAt: "2026-06-02T04:08:00",
  },
  {
    id: "e4c73f15-2a80-4b96-bc41-7d3e08a6f944",
    reviewerId: demoReviewerId,
    payoutMonth: "2026-04",
    likeCount: 254,
    shareCount: 63,
    commentCount: 94,
    likeAmount: 25_400,
    shareAmount: 18_900,
    commentAmount: 47_000,
    totalBaseAmount: 91_300,
    badge: "SILVER",
    badgeMultiplier: 1.5,
    totalFinalAmount: 136_950,
    status: "PAID",
    paidAt: "2026-05-02T04:05:00",
  },
  {
    id: "f5d6408a-9c23-4e5f-8017-b62f4a15d055",
    reviewerId: demoReviewerId,
    payoutMonth: "2026-03",
    likeCount: 187,
    shareCount: 45,
    commentCount: 72,
    likeAmount: 18_700,
    shareAmount: 13_500,
    commentAmount: 36_000,
    totalBaseAmount: 68_200,
    badge: "BRONZE",
    badgeMultiplier: 1.2,
    totalFinalAmount: 81_840,
    status: "PAID",
    paidAt: "2026-04-02T04:07:00",
  },
  {
    id: "a6e51b93-7d08-4a62-95c3-04f8e2b71d66",
    reviewerId: demoReviewerId,
    payoutMonth: "2026-02",
    likeCount: 121,
    shareCount: 28,
    commentCount: 44,
    likeAmount: 12_100,
    shareAmount: 8_400,
    commentAmount: 22_000,
    totalBaseAmount: 42_500,
    badge: "IRON",
    badgeMultiplier: 1,
    totalFinalAmount: 42_500,
    status: "PAID",
    paidAt: "2026-03-02T04:03:00",
  },
];
