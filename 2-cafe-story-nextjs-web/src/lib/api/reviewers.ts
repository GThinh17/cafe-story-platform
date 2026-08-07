import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  ReviewerBadgeResponse,
  ReviewerConnectOnboardResponse,
  ReviewerConnectStatus,
  ReviewerDiscoveryResponse,
  ReviewerEarningsResponse,
  ReviewerRankingResponse,
  ReviewerResponse,
  ReviewerStatsResponse,
} from "@/types/reviewer";

function withQuery(path: string, params: Record<string, string | number | boolean | undefined>) {
  const searchParams = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "" && value !== null) {
      searchParams.set(key, String(value));
    }
  }
  const query = searchParams.toString();
  return query ? `${path}?${query}` : path;
}

export function getTopReviewers(page = 0, size = 20) {
  return apiFetch<ReviewerDiscoveryResponse[]>(
    withQuery(apiEndpoints.reviewers.top, { page, size }),
    { method: "GET" },
  );
}

export function getReviewersByRegion(params: {
  area?: string;
  city?: string;
  province?: string;
  size?: number;
}) {
  return apiFetch<ReviewerDiscoveryResponse[]>(
    withQuery(apiEndpoints.reviewers.region, params),
    { method: "GET" },
  );
}

export function searchReviewers(query: string) {
  return apiFetch<ReviewerResponse[]>(
    withQuery(apiEndpoints.reviewers.list, { query }),
    { method: "GET" },
  );
}

export function getAllActiveReviewers() {
  return apiFetch<ReviewerResponse[]>(
    withQuery(apiEndpoints.reviewers.list, { activeOnly: true }),
    { method: "GET" },
  );
}

export function getReviewerByUserId(userId: string) {
  return apiFetch<ReviewerResponse>(apiEndpoints.reviewers.byUserId(userId), {
    method: "GET",
  });
}

export function getReviewerEarnings(reviewerId: string) {
  return apiFetch<ReviewerEarningsResponse[]>(apiEndpoints.reviewers.payouts(reviewerId), {
    method: "GET",
  });
}

export function getConnectStatus() {
  return apiFetch<ReviewerConnectStatus>(apiEndpoints.reviewers.connect.status, {
    method: "GET",
  });
}

export function createOnboardingLink() {
  return apiFetch<ReviewerConnectOnboardResponse>(apiEndpoints.reviewers.connect.onboard, {
    method: "POST",
  });
}

/**
 * Xin link onboarding mới khi link cũ hết hạn — Stripe đá người dùng về
 * refresh-url trong trường hợp đó.
 */
export function refreshOnboardingLink() {
  return apiFetch<ReviewerConnectOnboardResponse>(apiEndpoints.reviewers.connect.refresh, {
    method: "POST",
  });
}

export function syncConnectStatus() {
  return apiFetch<ReviewerConnectStatus>(apiEndpoints.reviewers.connect.sync, {
    method: "POST",
  });
}

export function getReviewerStats(reviewerId: string, period: string) {
  return apiFetch<ReviewerStatsResponse>(
    withQuery(apiEndpoints.reviewers.stats(reviewerId), { period }),
    { method: "GET" },
  );
}

export function getReviewerBadges(reviewerId: string) {
  return apiFetch<ReviewerBadgeResponse[]>(apiEndpoints.reviewers.badges(reviewerId), {
    method: "GET",
  });
}

export function getReviewerRanking(period: string, page = 1, limit = 20) {
  return apiFetch<ReviewerRankingResponse[]>(
    withQuery(apiEndpoints.reviewers.ranking, { period, page, limit }),
    { method: "GET" },
  );
}
