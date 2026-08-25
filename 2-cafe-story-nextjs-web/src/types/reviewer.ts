export type ReviewerBadge = "IRON" | "BRONZE" | "SILVER" | "GOLD" | "DIAMOND";

export type ReviewerDiscoveryResponse = {
  rank: number;
  reviewerId: string;
  userId: string;
  userName: string | null;
  userFullName: string | null;
  avatar: string | null;
  city: string | null;
  province: string | null;
  area: string | null;
  reviewCount: number;
  followerCount: number;
  badge: ReviewerBadge | string | null;
  rankingScore: number;
  isFollowing: boolean;
  isMe: boolean;
};

export type ReviewerResponse = {
  reviewerId: string;
  userId: string;
  role: string | null;
  avatar: string | null;
  name: string | null;
  follower: number;
  follow: number;
  like: number;
  badge: ReviewerBadge | string | null;
  score: number;
  expireDate: string | null;
  isFollowing: boolean | null;
};

export type ReviewerPayoutStatus =
  | "PENDING"
  | "APPROVED"
  | "PAID"
  | "CANCELLED"
  | string;

/**
 * Thu nhập một tháng của reviewer.
 *
 * `totalBaseAmount` là tổng ba khoản like/share/comment; `totalFinalAmount` là
 * số thực nhận sau khi nhân hệ số badge của tháng. Hai số này khác nhau nên UI
 * phải hiển thị đúng cái cuối cùng khi nói về tiền nhận được.
 */
export type ReviewerEarningsResponse = {
  id: string;
  reviewerId: string;
  payoutMonth: string;
  likeCount: number;
  shareCount: number;
  commentCount: number;
  likeAmount: number;
  shareAmount: number;
  commentAmount: number;
  totalBaseAmount: number;
  badge: ReviewerBadge | string | null;
  badgeMultiplier: number;
  totalFinalAmount: number;
  status: ReviewerPayoutStatus;
  paidAt: string | null;
};

export type ReviewerConnectStatus = {
  reviewerId: string | null;
  userName: string | null;
  userAvatar: string | null;
  stripeAccountId: string | null;
  onboardingStatus: "PENDING" | "INCOMPLETE" | "COMPLETE" | string | null;
  chargesEnabled: boolean;
  payoutsEnabled: boolean;
  createdAt: string | null;
  updatedAt: string | null;
};

export type ReviewerConnectOnboardResponse = {
  onboardingUrl: string;
  stripeAccountId: string | null;
  onboardingStatus: string | null;
};

export type ReviewerStatsResponse = {
  reviewerId: string;
  period: string;
  likeCount: number;
  shareCount: number;
  commentCount: number;
  score: number;
};

export type ReviewerBadgeResponse = {
  id: string;
  reviewerId: string;
  month: string;
  score: number;
  badge: ReviewerBadge;
  likeCount: number;
  shareCount: number;
  commentCount: number;
};

export type ReviewerRankingResponse = {
  rank: number;
  reviewerId: string;
  userId: string;
  userName: string | null;
  userAvatar: string | null;
  score: number;
  likeCount: number;
  shareCount: number;
  commentCount: number;
  location: string | null;
};
