export type ReviewerDashboardPeriod = "day" | "week" | "month" | "3months";

export type ReviewerDashboardBadge =
  | "IRON"
  | "BRONZE"
  | "SILVER"
  | "GOLD"
  | "DIAMOND";

export type ReviewerDashboardSegment =
  | "inactive"
  | "new"
  | "active"
  | "strong"
  | "top"
  | "elite";

export type ReviewerDashboardProfile = {
  avatar: string | null;
  badge: ReviewerDashboardBadge | null;
  expireDate: string | null;
  follow: number;
  follower: number;
  like: number;
  name: string | null;
  region: {
    area?: string | null;
    city?: string | null;
    province?: string | null;
    regionId: string;
    street?: string | null;
    ward?: string | null;
  } | null;
  reviewerId: string;
  role: "REVIEWER" | string;
  score: number;
  userId: string;
};

export type ReviewerDashboardStats = {
  commentCount: number;
  likeCount: number;
  period: ReviewerDashboardPeriod;
  reviewerId: string;
  score: number;
  shareCount: number;
};

export type ReviewerDashboardRankingItem = {
  badge: ReviewerDashboardBadge | null;
  commentCount: number;
  likeCount: number;
  location: string;
  rank: number;
  reviewerId: string;
  score: number;
  shareCount: number;
};

export type ReviewerDashboardSegmentItem = {
  commentCount: number;
  likeCount: number;
  reviewerId: string;
  score: number;
  segment: ReviewerDashboardSegment;
  shareCount: number;
};

export type ReviewerDashboardPayout = {
  commentAmount: number;
  commentCount: number;
  id: string;
  likeAmount: number;
  likeCount: number;
  payoutMonth: string;
  payoutStatus: string;
  reviewerId: string;
  shareAmount: number;
  shareCount: number;
  totalAmount: number;
};

export type ReviewerDashboardBadgeHistoryItem = {
  badge: ReviewerDashboardBadge;
  commentCount: number;
  id: string;
  likeCount: number;
  month: string;
  reviewerId: string;
  score: number;
  shareCount: number;
};

export type ReviewerDashboardActivity = {
  description: string;
  id: string;
  time: string;
  title: string;
  type: "like" | "share" | "badge" | "payout" | "ranking";
};

export type ReviewerDashboardPerformancePoint = {
  comments: number;
  label: string;
  likes: number;
  score: number;
  shares: number;
};
