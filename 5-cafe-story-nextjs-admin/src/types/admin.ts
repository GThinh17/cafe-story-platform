export type UUID = string;

export type UserRole = "USER" | "REVIEWER" | "ADMIN" | "CAFE_PAGE";
export type PostStatus = "DRAFT" | "PUBLISHED" | "HIDDEN" | "REMOVED";
export type PageStatus = "DRAFT" | "ACTIVE" | "SUSPENDED";
export type ReportStatus = "OPEN" | "REVIEWING" | "RESOLVED" | "REJECTED";
export type ReportTargetType = "BLOG" | "COMMENT" | "USER" | "CAFE_PAGE";
export type PaymentStatus =
  | "PENDING"
  | "PAID"
  | "FAILED"
  | "CANCELLED"
  | "EXPIRED"
  | "REFUNDED";
export type PaymentMethod = "STRIPE_CARD" | "BANK_TRANSFER" | "VNPAY";
export type ExtraFeeType = "REVIEWER_REGISTRATION" | "CAFE_PAGE_OPENING";
export type ModerationDecision = "SAFE" | "NEEDS_REVIEW" | "VIOLATION";
export type ModerationResolveAction = "APPROVE" | "HIDE" | "REMOVE";
export type RankingPeriodType = "DAILY" | "WEEKLY" | "MONTHLY";
export type ReviewerBadge = "IRON" | "BRONZE" | "SILVER" | "GOLD" | "DIAMOND";

export type AdminDashboardSummary = {
  totalUsers: number;
  activeUsers: number;
  totalCafePages: number;
  activeCafePages: number;
  totalBlogs: number;
  publishedBlogs: number;
  hiddenBlogs: number;
  totalComments: number;
  totalPayments: number;
  paidPayments: number;
  pendingPayments: number;
  failedPayments: number;
  totalReviewers: number;
  pendingModerationItems: number;
};

export type AdminUser = {
  userId: UUID;
  userName: string;
  userFullName: string | null;
  userEmail: string;
  userPhone: number | null;
  userAvatar: string | null;
  userLike: number | null;
  userFollower: number | null;
  accountStatus: boolean;
  regionId: UUID | null;
  roles: UserRole[];
};

export type Blog = {
  id: UUID;
  authorUserId: UUID;
  authorUserName: string;
  authorUserFullName: string | null;
  authorUserAvatar: string | null;
  pageId: UUID | null;
  pageName: string | null;
  pageAvatarUrl: string | null;
  regionId: UUID | null;
  content: string | null;
  imageUrls: string[];
  status: PostStatus;
  isPinned: boolean | null;
  allowComment: boolean | null;
  likeCount: number | null;
  shareCount: number | null;
  commentCount: number | null;
  ratingScore: number | null;
  ratingCount: number | null;
  saveCount: number | null;
  displayAuthorType: string | null;
  displayName: string | null;
  displayAvatarUrl: string | null;
  createdAt: string;
  updatedAt: string | null;
};

export type CafePage = {
  id: UUID;
  ownerUserId: UUID;
  regionId: UUID | null;
  regionCity: string | null;
  regionProvince: string | null;
  regionWard: string | null;
  regionArea: string | null;
  regionStreet: string | null;
  name: string;
  address: string | null;
  description: string | null;
  avatarUrl: string | null;
  coverUrl: string | null;
  status: PageStatus;
  likeCount: number | null;
  followerCount: number | null;
  ratingScore: number | null;
  ratingCount: number | null;
  maxMembers: number | null;
  pageActive: boolean | null;
  pageExpiresAt: string | null;
  createdAt: string;
  updatedAt: string | null;
};

export type Comment = {
  id: UUID;
  blogId: UUID;
  userId: UUID;
  authorUserName: string;
  parentCommentId: UUID | null;
  content: string | null;
  imageUrls: string[];
  status: PostStatus;
  createdAt: string;
  updatedAt: string | null;
};

export type AdminModerationResult = {
  id: UUID;
  blogId: UUID | null;
  authorUserId: UUID | null;
  authorUserName: string | null;
  authorUserFullName: string | null;
  caption: string | null;
  score: number | null;
  decision: ModerationDecision | null;
  captionScore: number | null;
  captionReason: string | null;
  imageScore: number | null;
  imageReason: string | null;
  tags: string[];
  aiStatus: string | null;
  blogStatus: PostStatus | null;
  labels: string | null;
  explanation: string | null;
  modelName: string | null;
  resolved: boolean | null;
  resolvedAction: ModerationResolveAction | null;
  resolvedAt: string | null;
  createdAt: string;
  updatedAt: string | null;
};

export type ContentReport = {
  id: UUID;
  reporterUserId: UUID;
  reporterUserName: string | null;
  targetType: ReportTargetType;
  targetId: UUID;
  blogId: UUID | null;
  commentId: UUID | null;
  reportedUserId: UUID | null;
  cafePageId: UUID | null;
  reason: string;
  description: string | null;
  status: ReportStatus;
  createdAt: string;
  resolvedAt: string | null;
};

export type Payment = {
  paymentId: UUID;
  buyerId: UUID;
  extraFeeId: UUID | null;
  adFeeId: UUID | null;
  paymentMethod: PaymentMethod;
  amount: number;
  currency: string;
  paymentStatus: PaymentStatus;
  paymentUrl: string | null;
  qrCodeUrl: string | null;
  transferContent: string | null;
  createdAt: string;
  paidAt: string | null;
  expiredAt: string | null;
};

export type ExtraFee = {
  extraFeeId: UUID;
  name: string;
  description: string | null;
  feeType: ExtraFeeType;
  price: number;
  durationMonths: number | null;
  maxMembers: number | null;
  status: boolean | null;
  createdAt: string;
  updatedAt: string | null;
};

export type ExtraFeeRequest = {
  name: string;
  description?: string | null;
  feeType: ExtraFeeType;
  price: number;
  durationMonths?: number | null;
  maxMembers?: number | null;
  status?: boolean | null;
};

export type BlogRankingOverrideRequest = {
  boost_score?: number | null;
  is_pinned?: boolean | null;
  reason?: string | null;
  start_at: string;
  end_at?: string | null;
};

export type ReviewerScoringFormula = {
  id: UUID;
  likeWeight: number;
  commentWeight: number;
  shareWeight: number;
  likePayoutAmount: number;
  commentPayoutAmount: number;
  sharePayoutAmount: number;
  active: boolean;
  description: string | null;
  createdAt: string;
};

export type ReviewerBadgeThreshold = {
  id: UUID;
  badge: ReviewerBadge;
  minScore: number;
  formulaId: UUID;
};

export type ReviewerRankingSnapshot = {
  id: UUID;
  reviewerId: UUID;
  period: string;
  periodType: RankingPeriodType;
  rankPosition: number;
  score: number;
  likeCount: number;
  shareCount: number;
  commentCount: number;
  badge: ReviewerBadge;
  formulaId: UUID | null;
};
