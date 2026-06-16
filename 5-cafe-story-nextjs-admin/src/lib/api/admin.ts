import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { PageResponse } from "@/types/api";
import type {
  AdminDashboardSummary,
  AdminModerationResult,
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
  ReportStatus,
  ReportTargetType,
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
  decision?: string;
  resolved?: boolean | null;
  resolvedAction?: string;
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

  if (params.decision) queryParams.decision = params.decision;
  if (params.resolved !== undefined && params.resolved !== null) queryParams.resolved = params.resolved;
  if (params.resolvedAction) queryParams.resolvedAction = params.resolvedAction;

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
