import { apiFetch, buildApiUrl } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { PageResponse } from "@/types/api";
import type {
  AdminDashboardSummary,
  AdminRegionAnalytics,
  AdminRevenueAnalytics,
  AdminAssistantChatResponse,
  AdminAssistantConversation,
  AdminAssistantDraftAction,
  AdminAssistantMessage,
  AdminAssistantMessageRequest,
  AdminContentTranslationRequest,
  AdminContentTranslationResponse,
  AdminModerationResult,
  AdminPayout,
  AdminPayoutStatus,
  AdminReportAiAutoApplyJob,
  AdminReportAiAutoApplyRequest,
  AdminReportAiPolicy,
  AdminReportAiResolution,
  AdminUser,
  Blog,
  BlogRankingOverrideRequest,
  CafePage,
  Comment,
  ContentReport,
  ExtraFee,
  ExtraFeeRequest,
  ModerationResolveAction,
  PageStatus,
  Payment,
  PaymentStatus,
  PostStatus,
  RankingPeriodType,
  ReportStatus,
  ReportTargetType,
  ReviewerBadgeThreshold,
  ReviewerFormula,
  ReviewerIncome,
  ReviewerRankingSnapshot,
  UserRole,
} from "@/types/admin";

type QueryValue = string | number | boolean | null | undefined;

function withQuery(path: string, params: Record<string, QueryValue>) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, String(value));
    }
  });

  const queryString = query.toString();

  return queryString ? `${path}?${queryString}` : path;
}

export function getAdminDashboardSummary(signal?: AbortSignal) {
  return apiFetch<AdminDashboardSummary>(apiEndpoints.admin.dashboardSummary, {
    method: "GET",
    signal,
  });
}

export const getDashboardSummary = getAdminDashboardSummary;

export function translateAdminContent(request: AdminContentTranslationRequest) {
  return apiFetch<AdminContentTranslationResponse>(apiEndpoints.admin.translations, {
    body: request,
    method: "POST",
  });
}

export function getRegionAnalytics(signal?: AbortSignal) {
  return apiFetch<AdminRegionAnalytics[]>(apiEndpoints.admin.analyticsRegions, {
    method: "GET",
    signal,
  });
}

export function getRevenueAnalytics(days = 30, signal?: AbortSignal) {
  return apiFetch<AdminRevenueAnalytics>(
    withQuery(apiEndpoints.admin.analyticsRevenue, { days }),
    {
      method: "GET",
      signal,
    },
  );
}

export function createAssistantConversation(request?: { title?: string | null }) {
  return apiFetch<AdminAssistantConversation>(apiEndpoints.admin.assistantConversations, {
    method: "POST",
    body: request ?? null,
  });
}

export function getAssistantConversations(
  params: { page: number; size: number },
  signal?: AbortSignal,
) {
  return apiFetch<PageResponse<AdminAssistantConversation>>(
    withQuery(apiEndpoints.admin.assistantConversations, params),
    { method: "GET", signal },
  );
}

export function getAssistantMessages(
  conversationId: string,
  params: { page: number; size: number },
  signal?: AbortSignal,
) {
  return apiFetch<PageResponse<AdminAssistantMessage>>(
    withQuery(apiEndpoints.admin.assistantConversationMessages(conversationId), params),
    { method: "GET", signal },
  );
}

export function sendAssistantMessage(
  conversationId: string,
  request: AdminAssistantMessageRequest,
) {
  return apiFetch<AdminAssistantChatResponse>(
    apiEndpoints.admin.assistantConversationMessages(conversationId),
    { method: "POST", body: request },
  );
}

type AssistantStreamHandlers = {
  onProgress?: (payload: Record<string, unknown>) => void;
  onMessage?: (payload: AdminAssistantChatResponse) => void;
  onError?: (payload: Record<string, unknown>) => void;
  onDone?: () => void;
};

export async function streamAssistantMessage(
  conversationId: string,
  request: AdminAssistantMessageRequest,
  handlers: AssistantStreamHandlers,
) {
  const response = await fetch(
    buildApiUrl(apiEndpoints.admin.assistantConversationMessagesStream(conversationId)),
    {
      method: "POST",
      credentials: "include",
      headers: {
        Accept: "text/event-stream",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    },
  );

  if (!response.ok || !response.body) {
    throw new Error("Admin assistant stream failed.");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  function handleEvent(rawEvent: string) {
    const lines = rawEvent.split(/\r?\n/);
    const eventName =
      lines.find((line) => line.startsWith("event:"))?.slice("event:".length).trim() ??
      "message";
    const data = lines
      .filter((line) => line.startsWith("data:"))
      .map((line) => line.slice("data:".length).trim())
      .join("\n");
    const payload = data ? (JSON.parse(data) as Record<string, unknown>) : {};

    if (eventName === "progress") handlers.onProgress?.(payload);
    if (eventName === "message") handlers.onMessage?.(payload as AdminAssistantChatResponse);
    if (eventName === "error") handlers.onError?.(payload);
    if (eventName === "done") handlers.onDone?.();
  }

  while (true) {
    const { done, value } = await reader.read();
    buffer += decoder.decode(value ?? new Uint8Array(), { stream: !done });
    const events = buffer.split(/\r?\n\r?\n/);
    buffer = events.pop() ?? "";
    events.filter(Boolean).forEach(handleEvent);
    if (done) break;
  }

  if (buffer.trim()) {
    handleEvent(buffer.trim());
  }
}

export function getAssistantDraftAction(draftActionId: string, signal?: AbortSignal) {
  return apiFetch<AdminAssistantDraftAction>(
    apiEndpoints.admin.assistantDraftAction(draftActionId),
    { method: "GET", signal },
  );
}

export function executeAssistantDraftAction(draftActionId: string) {
  return apiFetch<AdminAssistantDraftAction>(
    apiEndpoints.admin.assistantDraftActionExecute(draftActionId),
    { method: "POST" },
  );
}

export function getAdminUsers(params: {
  search?: string;
  accountStatus?: boolean | null;
  role?: UserRole | "";
  page: number;
  size: number;
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<AdminUser>>(
    withQuery(apiEndpoints.admin.users, params),
    { method: "GET", signal },
  );
}

export const getUsers = getAdminUsers;

export function getAdminUser(userId: string, signal?: AbortSignal) {
  return apiFetch<AdminUser>(apiEndpoints.admin.user(userId), {
    method: "GET",
    signal,
  });
}

export function updateUserStatus(userId: string, accountStatus: boolean) {
  return apiFetch<AdminUser>(apiEndpoints.admin.userStatus(userId), {
    method: "PATCH",
    body: { accountStatus },
  });
}

export function updateUserRoles(userId: string, roles: UserRole[]) {
  return apiFetch<AdminUser>(apiEndpoints.admin.userRoles(userId), {
    method: "PATCH",
    body: { roles },
  });
}

export function getAdminBlogs(params: {
  status?: PostStatus | "";
  authorUserId?: string;
  pageId?: string;
  page: number;
  size: number;
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<Blog>>(withQuery(apiEndpoints.admin.blogs, params), {
    method: "GET",
    signal,
  });
}

export const getBlogs = getAdminBlogs;

export function getAdminBlog(blogId: string, signal?: AbortSignal) {
  return apiFetch<Blog>(apiEndpoints.admin.blog(blogId), {
    method: "GET",
    signal,
  });
}

export function updateBlogStatus(blogId: string, status: PostStatus) {
  return apiFetch<Blog>(apiEndpoints.admin.blogStatus(blogId), {
    method: "PATCH",
    body: { status },
  });
}

export function deleteBlog(blogId: string) {
  return apiFetch<void>(apiEndpoints.admin.blog(blogId), { method: "DELETE" });
}

export function createBlogRankingOverride(
  blogId: string,
  request: BlogRankingOverrideRequest,
) {
  return apiFetch(apiEndpoints.admin.blogRankingOverride(blogId), {
    method: "POST",
    body: request,
  });
}

export function getAdminCafePages(params: {
  status?: PageStatus | "";
  ownerUserId?: string;
  page: number;
  size: number;
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<CafePage>>(
    withQuery(apiEndpoints.admin.cafePages, params),
    { method: "GET", signal },
  );
}

export const getCafePages = getAdminCafePages;

export function getAdminCafePage(pageId: string, signal?: AbortSignal) {
  return apiFetch<CafePage>(apiEndpoints.admin.cafePage(pageId), {
    method: "GET",
    signal,
  });
}

export function updateCafePageStatus(pageId: string, status: PageStatus) {
  return apiFetch<CafePage>(apiEndpoints.admin.cafePageStatus(pageId), {
    method: "PATCH",
    body: { status },
  });
}

export function deleteCafePage(pageId: string) {
  return apiFetch<void>(apiEndpoints.admin.cafePage(pageId), { method: "DELETE" });
}

export function getAdminComments(params: {
  status?: PostStatus | "";
  blogId?: string;
  userId?: string;
  page: number;
  size: number;
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<Comment>>(
    withQuery(apiEndpoints.admin.comments, params),
    { method: "GET", signal },
  );
}

export const getComments = getAdminComments;

export function getAdminComment(commentId: string, signal?: AbortSignal) {
  return apiFetch<Comment>(apiEndpoints.admin.comment(commentId), {
    method: "GET",
    signal,
  });
}

export function updateCommentStatus(commentId: string, status: PostStatus) {
  return apiFetch<Comment>(apiEndpoints.admin.commentStatus(commentId), {
    method: "PATCH",
    body: { status },
  });
}

export function deleteComment(commentId: string) {
  return apiFetch<void>(apiEndpoints.admin.comment(commentId), { method: "DELETE" });
}

export type ModerationFilterParams = {
  page: number;
  size: number;
  aiStatus?: string;
  decision?: string;
  resolved?: boolean | null;
};

export function getAdminModerationResults(
  mode: "queue" | "results",
  params: ModerationFilterParams,
  signal?: AbortSignal,
) {
  const endpoint =
    mode === "queue"
      ? apiEndpoints.admin.moderationQueue
      : apiEndpoints.admin.moderationResults;

  const queryParams: Record<string, QueryValue> = {
    page: params.page,
    size: params.size,
  };

  if (params.aiStatus) queryParams.aiStatus = params.aiStatus;
  if (params.decision) queryParams.decision = params.decision;
  if (params.resolved !== undefined && params.resolved !== null) queryParams.resolved = params.resolved;

  return apiFetch<PageResponse<AdminModerationResult>>(withQuery(endpoint, queryParams), {
    method: "GET",
    signal,
  });
}

export const getModerationResults = getAdminModerationResults;

export function getAdminModerationQueue(
  params: { page: number; size: number },
  signal?: AbortSignal,
) {
  return getAdminModerationResults("queue", params, signal);
}

export function getAdminModerationResult(resultId: string, signal?: AbortSignal) {
  return apiFetch<AdminModerationResult>(
    apiEndpoints.admin.moderationResult(resultId),
    {
      method: "GET",
      signal,
    },
  );
}

export function resolveModerationResult(
  resultId: string,
  action: ModerationResolveAction,
) {
  return apiFetch<AdminModerationResult>(
    apiEndpoints.admin.moderationResolve(resultId),
    {
      method: "POST",
      body: { action },
    },
  );
}

export function getAdminReports(params: {
  status?: ReportStatus | "";
  targetType?: ReportTargetType | "";
  page: number;
  size: number;
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<ContentReport>>(
    withQuery(apiEndpoints.admin.reports, params),
    { method: "GET", signal },
  );
}

export const getReports = getAdminReports;

export function getAdminReport(reportId: string, signal?: AbortSignal) {
  return apiFetch<ContentReport>(apiEndpoints.admin.report(reportId), {
    method: "GET",
    signal,
  });
}

export function updateReportStatus(reportId: string, status: ReportStatus) {
  return apiFetch<ContentReport>(apiEndpoints.admin.reportStatus(reportId), {
    method: "PATCH",
    body: { status },
  });
}

export function resolveReport(reportId: string) {
  return apiFetch<ContentReport>(apiEndpoints.admin.reportResolve(reportId), {
    method: "POST",
  });
}

export function createReportAiResolution(
  reportId: string,
  request?: AdminReportAiAutoApplyRequest,
) {
  return apiFetch<AdminReportAiResolution>(
    apiEndpoints.admin.reportAiResolution(reportId),
    { method: "POST", body: request ?? null },
  );
}

export function getReportAiResolutions(
  reportId: string,
  params: { page: number; size: number },
  signal?: AbortSignal,
) {
  return apiFetch<PageResponse<AdminReportAiResolution>>(
    withQuery(apiEndpoints.admin.reportAiResolutions(reportId), params),
    { method: "GET", signal },
  );
}

export function getReportAiPolicy(reportId: string, signal?: AbortSignal) {
  return apiFetch<AdminReportAiPolicy>(apiEndpoints.admin.reportAiPolicy(reportId), {
    method: "GET",
    signal,
  });
}

export function getReportAiAutoResolutions(
  reportId: string,
  params: { page: number; size: number },
  signal?: AbortSignal,
) {
  return apiFetch<PageResponse<AdminReportAiAutoApplyJob>>(
    withQuery(apiEndpoints.admin.reportAiAutoResolutions(reportId), params),
    { method: "GET", signal },
  );
}

export function cancelReportAiAutoResolution(jobId: string) {
  return apiFetch<AdminReportAiAutoApplyJob>(
    apiEndpoints.admin.reportAiAutoResolutionCancel(jobId),
    { method: "POST" },
  );
}

export function getAdminPayments(params: {
  paymentStatus?: PaymentStatus | "";
  buyerId?: string;
  page: number;
  size: number;
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<Payment>>(
    withQuery(apiEndpoints.admin.payments, params),
    { method: "GET", signal },
  );
}

export const getPayments = getAdminPayments;

export function getAdminPayment(paymentId: string, signal?: AbortSignal) {
  return apiFetch<Payment>(apiEndpoints.admin.payment(paymentId), {
    method: "GET",
    signal,
  });
}

export function markBankTransferPaid(paymentId: string) {
  return apiFetch<Payment>(apiEndpoints.admin.markBankTransferPaid(paymentId), {
    method: "POST",
  });
}

export function refundPayment(paymentId: string) {
  return apiFetch<Payment>(apiEndpoints.admin.refundPayment(paymentId), {
    method: "POST",
  });
}

export function getAdminExtraFees(params: {
  status?: boolean | null;
  page: number;
  size: number;
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<ExtraFee>>(
    withQuery(apiEndpoints.admin.extraFees, params),
    { method: "GET", signal },
  );
}

export const getExtraFees = getAdminExtraFees;

export function createExtraFee(request: ExtraFeeRequest) {
  return apiFetch<ExtraFee>(apiEndpoints.admin.extraFees, {
    method: "POST",
    body: request,
  });
}

export function updateExtraFee(extraFeeId: string, request: ExtraFeeRequest) {
  return apiFetch<ExtraFee>(apiEndpoints.admin.extraFee(extraFeeId), {
    method: "PUT",
    body: request,
  });
}

export function updateExtraFeeStatus(extraFeeId: string, status: boolean) {
  return apiFetch<ExtraFee>(apiEndpoints.admin.extraFeeStatus(extraFeeId), {
    method: "PATCH",
    body: { status },
  });
}

export function deleteExtraFee(extraFeeId: string) {
  return apiFetch<void>(apiEndpoints.admin.extraFee(extraFeeId), {
    method: "DELETE",
  });
}

export function getReviewerRanking(params: {
  period: string;
  periodType: RankingPeriodType;
  page: number;
  size: number;
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<ReviewerRankingSnapshot>>(
    withQuery(apiEndpoints.admin.reviewerRanking, params),
    { method: "GET", signal },
  );
}

export function generateReviewerRanking(
  periodType: RankingPeriodType,
  referenceDate?: string,
) {
  return apiFetch<void>(
    withQuery(apiEndpoints.admin.generateReviewerRanking, {
      periodType,
      referenceDate,
    }),
    { method: "POST" },
  );
}

// ── Unified formulas ─────────────────────────────────────────────────────────

export function getFormulas(signal?: AbortSignal) {
  return apiFetch<ReviewerFormula[]>(apiEndpoints.admin.formulas, {
    method: "GET",
    signal,
  });
}

export function createFormula(request: Omit<ReviewerFormula, "id" | "active" | "createdAt">) {
  return apiFetch<ReviewerFormula>(apiEndpoints.admin.formulas, {
    method: "POST",
    body: request,
  });
}

export function activateFormula(id: string) {
  return apiFetch<ReviewerFormula>(apiEndpoints.admin.formulaActivate(id), {
    method: "PUT",
  });
}

export function getFormulaThresholds(formulaId: string, signal?: AbortSignal) {
  return apiFetch<ReviewerBadgeThreshold[]>(
    apiEndpoints.admin.formulaThresholds(formulaId),
    { method: "GET", signal },
  );
}

// ── Reviewer daily income ─────────────────────────────────────────────────────

export function generatePayoutIncome(date?: string) {
  return apiFetch<void>(
    withQuery(apiEndpoints.admin.generatePayoutIncome, { date }),
    { method: "POST" },
  );
}

export function getPayoutIncome(params: {
  reviewerId?: string;
  month?: string;
  page: number;
  size: number;
  sortDir?: "asc" | "desc";
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<ReviewerIncome>>(
    withQuery(apiEndpoints.admin.payoutIncome, params),
    { method: "GET", signal },
  );
}

// ── Admin monthly payout ──────────────────────────────────────────────────────

export function generateMonthlyPayout(month?: string) {
  return apiFetch<void>(
    withQuery(apiEndpoints.admin.generatePayoutMonthly, { month }),
    { method: "POST" },
  );
}

export function getMonthlyPayouts(params: {
  month?: string;
  status?: AdminPayoutStatus | "";
  page: number;
  size: number;
  sortDir?: "asc" | "desc";
}, signal?: AbortSignal) {
  return apiFetch<PageResponse<AdminPayout>>(
    withQuery(apiEndpoints.admin.payoutMonthly, params),
    { method: "GET", signal },
  );
}

export function updatePayoutStatus(id: string, status: AdminPayoutStatus, note?: string) {
  return apiFetch<AdminPayout>(apiEndpoints.admin.payoutMonthlyStatus(id), {
    method: "PATCH",
    body: { status, note },
  });
}
