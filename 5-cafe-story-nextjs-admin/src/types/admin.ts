export type UUID = string;

export type UserRole = "USER" | "REVIEWER" | "ADMIN" | "CAFE_PAGE";
export type PostStatus = "DRAFT" | "PUBLISHED" | "HIDDEN" | "REMOVED";
export type PageStatus = "DRAFT" | "ACTIVE" | "SUSPENDED";
export type ReportStatus = "OPEN" | "REVIEWING" | "RESOLVED" | "REJECTED";
export type ReportTargetType = "BLOG" | "COMMENT" | "USER" | "CAFE_PAGE";
export type AdminReportAiReportDecision =
  | "RESOLVE"
  | "REJECT"
  | "NEEDS_MANUAL_REVIEW";
export type AdminReportAiTargetAction =
  | "KEEP_VISIBLE"
  | "NO_ACTION"
  | "APPROVE"
  | "HIDE"
  | "REMOVE"
  | "KEEP_ACTIVE"
  | "SUSPEND_USER"
  | "SUSPEND_PAGE"
  | "NONE";
export type AdminReportAiAutoApplyJobStatus =
  | "SCHEDULED"
  | "APPLYING"
  | "APPLIED"
  | "CANCELLED"
  | "FAILED"
  | "SKIPPED";
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
export type AiStatus = "SEND_ADMIN" | "APPROVE" | "DENY";
export type RankingPeriodType = "DAILY" | "WEEKLY" | "MONTHLY";
export type ReviewerBadge = "IRON" | "BRONZE" | "SILVER" | "GOLD" | "DIAMOND";
export type AdminAssistantMessageRole = "USER" | "ASSISTANT" | "SYSTEM";
export type AdminAssistantDraftActionType =
  | "REPORT_RESOLVE"
  | "REPORT_REJECT"
  | "REPORT_ASK_AI_RESOLUTION"
  | "REPORT_CANCEL_AUTO_APPLY"
  | "BLOG_HIDE"
  | "BLOG_REMOVE"
  | "COMMENT_HIDE"
  | "COMMENT_REMOVE"
  | "USER_DEACTIVATE"
  | "CAFE_PAGE_SUSPEND";
export type AdminAssistantDraftActionStatus =
  | "PENDING"
  | "EXECUTED"
  | "CANCELLED"
  | "EXPIRED"
  | "FAILED";

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

export type AdminRegionAnalytics = {
  provinceCode: string;
  provinceName: string;
  userCount: number;
  cafePageCount: number;
  reviewerCount: number;
};

export type AdminRevenuePoint = {
  date: string;
  totalAmount: number;
  reviewerRegistrationAmount: number;
  cafePageOpeningAmount: number;
  advertiseAmount: number;
};

export type AdminRevenueAnalytics = {
  rangeDays: number;
  currency: string;
  totalAmount: number;
  reviewerRegistrationAmount: number;
  cafePageOpeningAmount: number;
  advertiseAmount: number;
  daily: AdminRevenuePoint[];
};

export type AdminAssistantConversation = {
  id: UUID;
  adminUserId: UUID;
  title: string | null;
  createdAt: string;
  updatedAt: string | null;
};

export type AdminAssistantMessage = {
  id: UUID;
  conversationId: UUID;
  role: AdminAssistantMessageRole;
  content: string;
  metadata: Record<string, unknown> | null;
  createdAt: string;
};

export type AdminAssistantDraftAction = {
  id: UUID;
  conversationId: UUID;
  messageId: UUID | null;
  actionType: AdminAssistantDraftActionType;
  payload: Record<string, unknown> | null;
  explanation: string | null;
  sourceRefs: Array<Record<string, unknown>> | null;
  status: AdminAssistantDraftActionStatus;
  expiresAt: string;
  executedAt: string | null;
  executionResult: Record<string, unknown> | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string | null;
};

export type AdminAssistantChatResponse = {
  message: AdminAssistantMessage;
  draftAction: AdminAssistantDraftAction | null;
  citations: Array<Record<string, unknown>>;
  toolCalls: Array<Record<string, unknown>>;
};

export type AdminAssistantMessageRequest = {
  message: string;
  pageContext?: Record<string, unknown>;
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
  authorUserAvatar: string | null;
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
  authorUserAvatar: string | null;
  caption: string | null;
  score: number | null;
  decision: ModerationDecision | null;
  captionScore: number | null;
  captionReason: string | null;
  imageScore: number | null;
  imageReason: string | null;
  tags: string[];
  aiStatus: AiStatus | null;
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
  reporterUserAvatar: string | null;
  targetType: ReportTargetType;
  targetId: UUID;
  blogId: UUID | null;
  commentId: UUID | null;
  reportedUserId: UUID | null;
  cafePageId: UUID | null;
  reasonId: UUID | null;
  reasonCode: string | null;
  reason: string;
  reasonLabel: string | null;
  reasonSeverity: number | null;
  description: string | null;
  status: ReportStatus;
  createdAt: string;
  resolvedAt: string | null;
};

export type AdminReportAiResolution = {
  id: UUID;
  contractVersion: "2.0" | "legacy-v1";
  correlationId: UUID | null;
  automationMode: "A0_RECOMMEND_ONLY";
  recommendationState: AdminReportAiReportDecision | null;
  contentReportId: UUID;
  targetType: ReportTargetType;
  targetId: UUID;
  reportDecision: AdminReportAiReportDecision;
  targetAction: AdminReportAiTargetAction;
  confidenceScore: number | null;
  riskScore: number | null;
  labels: string[];
  ruleCode: string | null;
  explanation: string | null;
  modelName: string | null;
  createdAt: string;
  autoApplyJob?: AdminReportAiAutoApplyJob | null;
  autoApplyWarning?: string | null;
  findings: AdminReportAiFinding[] | null;
  evidenceSummary: AdminReportAiEvidenceSummary | null;
  blockedReasons: string[] | null;
  evidenceQuality: "HIGH" | "MEDIUM" | "LOW" | "UNUSABLE" | null;
  evidenceSufficiency:
    | "SUFFICIENT"
    | "INSUFFICIENT"
    | "CONFLICTED"
    | "UNASSESSABLE"
    | null;
  violationLikelihood: "HIGH" | "MEDIUM" | "LOW" | "UNKNOWN" | null;
  harmSeverity: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | "UNKNOWN" | null;
  actionRisk: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | null;
  policyVersion: string | null;
  ruleCatalogVersion: string | null;
  promptVersion: string | null;
  workflowVersion: string | null;
  targetSnapshotHash: string | null;
};

export type AdminReportAiFinding = {
  ruleId: string;
  ruleVersion: string;
  outcome: "SUPPORTED" | "NOT_SUPPORTED" | "INCONCLUSIVE";
  evidenceIds: string[];
  counterEvidenceIds: string[];
  missingEvidenceIds: string[];
  violationLikelihood: "HIGH" | "MEDIUM" | "LOW" | "UNKNOWN";
  rationale: string;
};

export type AdminReportAiEvidenceSummary = {
  usedEvidenceIds?: string[];
  counterEvidenceIds?: string[];
  missingEvidenceIds?: string[];
  [key: string]: unknown;
};

export type AdminReportAiAutoApplyRequest = {
  autoApplyEnabled: boolean;
  autoApplyDelayMinutes?: number | null;
};

export type AdminReportAiAutoApplyJob = {
  id: UUID;
  contentReportId: UUID;
  aiResolutionId: UUID;
  targetType: ReportTargetType;
  targetId: UUID;
  status: AdminReportAiAutoApplyJobStatus;
  reportDecision: AdminReportAiReportDecision;
  targetAction: AdminReportAiTargetAction;
  confidenceScore: number | null;
  riskScore: number | null;
  scheduledAt: string;
  appliedAt: string | null;
  cancelledAt: string | null;
  lastError: string | null;
  createdAt: string;
  updatedAt: string | null;
};

export type Payment = {
  paymentId: UUID;
  buyerId: UUID;
  buyerUserName: string | null;
  buyerUserFullName: string | null;
  buyerUserAvatar: string | null;
  extraFeeId: UUID | null;
  extraFeeType: ExtraFeeType | null;
  adFeeId: UUID | null;
  activatedCafePageId: UUID | null;
  productName: string | null;
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

export type ReviewerFormula = {
  id: UUID;
  likeWeight: number;
  commentWeight: number;
  shareWeight: number;
  likePayoutAmount: number;
  commentPayoutAmount: number;
  sharePayoutAmount: number;
  ironMultiplier: number;
  bronzeMultiplier: number;
  silverMultiplier: number;
  goldMultiplier: number;
  diamondMultiplier: number;
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
  reviewerUserName: string | null;
  reviewerUserAvatar: string | null;
  period: string;
  periodType: RankingPeriodType;
  rankPosition: number;
  score: number;
  likeCount: number;
  shareCount: number;
  commentCount: number;
  badge: ReviewerBadge | null;
  formulaId: UUID | null;
};

export type AdminPayoutStatus = "PENDING" | "APPROVED" | "PAID" | "CANCELLED";


export type ReviewerIncome = {
  id: UUID;
  reviewerId: UUID;
  reviewerUserName: string | null;
  reviewerUserAvatar: string | null;
  incomeDate: string;
  likeCount: number;
  commentCount: number;
  shareCount: number;
  badge: ReviewerBadge;
  badgeMultiplier: number;
  baseAmount: number;
  finalAmount: number;
  formulaId: UUID;
  createdAt: string;
  updatedAt: string;
};

export type AdminPayout = {
  id: UUID;
  reviewerId: UUID;
  reviewerUserName: string | null;
  reviewerUserAvatar: string | null;
  payoutMonth: string;
  totalBaseAmount: number;
  badge: ReviewerBadge;
  badgeMultiplier: number;
  totalFinalAmount: number;
  status: AdminPayoutStatus;
  approvedBy: UUID | null;
  approvedAt: string | null;
  paidAt: string | null;
  note: string | null;
  formulaId: UUID;
  createdAt: string;
  updatedAt: string;
};
