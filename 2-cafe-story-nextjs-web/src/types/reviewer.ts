export type ReviewerBadge = "IRON" | "BRONZE" | "SILVER" | "GOLD" | "DIAMOND";

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
};

export type ReviewerPayoutResponse = {
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
