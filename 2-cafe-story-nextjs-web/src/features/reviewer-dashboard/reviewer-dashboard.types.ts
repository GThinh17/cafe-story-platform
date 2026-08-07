import { dictionaries, type Translate, type TranslationKey } from "@/lib/i18n";

export type ReviewerPeriod = "day" | "week" | "month" | "3months";

export type ReviewerBadge = "IRON" | "BRONZE" | "SILVER" | "GOLD" | "DIAMOND";

export type ReviewerSegment =
  | "inactive"
  | "new"
  | "active"
  | "strong"
  | "top"
  | "elite";

export type ReviewerProfile = {
  reviewerId: string;
  userId: string;
  role: "REVIEWER" | string;
  avatar: string;
  region: {
    regionId: string;
    city: string;
    province: string;
    ward: string;
    area: string;
    street: string;
  };
  name: string;
  follower: number;
  follow: number;
  like: number;
  badge: ReviewerBadge;
  score: number;
};

export type ReviewerStats = {
  reviewerId: string;
  period: ReviewerPeriod;
  likeCount: number;
  shareCount: number;
  commentCount: number;
  score: number;
};

export type ReviewerRankingItem = {
  rank: number;
  reviewerId: string;
  score: number;
  likeCount: number;
  shareCount: number;
  commentCount: number;
  location: string;
};

export type ReviewerSegmentItem = {
  reviewerId: string;
  segment: ReviewerSegment;
  score: number;
  likeCount: number;
  shareCount: number;
  commentCount: number;
};

export type ReviewerPayout = {
  id: string;
  reviewerId: string;
  payoutMonth: string;
  likeCount: number;
  shareCount: number;
  commentCount: number;
  likeAmount: number;
  shareAmount: number;
  commentAmount: number;
  totalAmount: number;
  payoutStatus: string;
};

export type ReviewerBadgeHistoryItem = {
  id: string;
  reviewerId: string;
  month: string;
  score: number;
  badge: ReviewerBadge;
  likeCount: number;
  shareCount: number;
  commentCount: number;
};

export type ReviewerActivity = {
  id: string;
  title: string;
  description: string;
  time: string;
  type: "like" | "share" | "badge" | "payout" | "ranking";
};

export type ReviewerPerformancePoint = {
  label: string;
  likes: number;
  shares: number;
  comments: number;
  score: number;
};

/** `stats.period` arrives as a raw API value; map it to localized copy. */
export function reviewerPeriodName(period: string, t: Translate) {
  const key = `reviewer.periodName.${period}` as TranslationKey;

  return dictionaries.en[key] ? t(key) : period;
}
