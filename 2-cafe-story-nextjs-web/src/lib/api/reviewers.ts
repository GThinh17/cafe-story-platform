import { apiCacheTtl, cachedApiCall } from "@/lib/api/api-cache";
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

/**
 * Discovery reads are cached because /explore re-runs its whole region cascade
 * whenever the user flips back to the reviewers tab, and the reviewer dashboard
 * asks for the same reviewer twice per navigation (sidebar plus page body).
 * Everything under the `reviewers:` prefix is dropped by `invalidateFollowCache`
 * in `users.ts` when a follow changes, since the discovery DTOs carry the
 * viewer's own `isFollowing`.
 */
export function getTopReviewers(page = 0, size = 20) {
  const path = withQuery(apiEndpoints.reviewers.top, { page, size });
  return cachedApiCall(`reviewers:top:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerDiscoveryResponse[]>(path, { method: "GET" }),
  );
}

export function getReviewersByRegion(params: {
  area?: string;
  city?: string;
  province?: string;
  size?: number;
}) {
  const path = withQuery(apiEndpoints.reviewers.region, params);
  return cachedApiCall(`reviewers:region:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerDiscoveryResponse[]>(path, { method: "GET" }),
  );
}

export function searchReviewers(query: string) {
  const path = withQuery(apiEndpoints.reviewers.list, { query });
  return cachedApiCall(`reviewers:search:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerResponse[]>(path, { method: "GET" }),
  );
}

export function getAllActiveReviewers() {
  const path = withQuery(apiEndpoints.reviewers.list, { activeOnly: true });
  return cachedApiCall(`reviewers:active:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerResponse[]>(path, { method: "GET" }),
  );
}

export function getReviewerByUserId(userId: string) {
  return cachedApiCall(
    `reviewers:by-user:${userId}`,
    apiCacheTtl.shortUser,
    () =>
      apiFetch<ReviewerResponse>(apiEndpoints.reviewers.byUserId(userId), {
        method: "GET",
      }),
  );
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
