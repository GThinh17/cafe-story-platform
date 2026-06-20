import type {
  ReviewerDashboardBadgeHistoryItem,
  ReviewerDashboardPayout,
  ReviewerDashboardPeriod,
  ReviewerDashboardProfile,
  ReviewerDashboardRankingItem,
  ReviewerDashboardStats,
} from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getReviewerByUserId(userId: string) {
  return cachedApiCall(`reviewers:user:${userId}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerDashboardProfile>(apiEndpoints.reviewers.byUser(userId), {
      method: "GET",
    }),
  );
}

function withQuery(path: string, params: Record<string, string | number | undefined>) {
  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") {
      searchParams.set(key, String(value));
    }
  }

  const query = searchParams.toString();

  return query ? `${path}?${query}` : path;
}

export function getReviewerStats(
  reviewerId: string,
  period: ReviewerDashboardPeriod,
) {
  const path = withQuery(apiEndpoints.reviewers.stats(reviewerId), { period });

  return cachedApiCall(`reviewers:stats:${reviewerId}:${period}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerDashboardStats>(path, {
      method: "GET",
    }),
  );
}

export function getReviewerRanking(
  period: ReviewerDashboardPeriod,
  page = 1,
  limit = 5,
) {
  const path = withQuery(apiEndpoints.reviewers.ranking, {
    limit,
    page,
    period,
  });

  return cachedApiCall(`reviewers:ranking:${period}:${page}:${limit}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerDashboardRankingItem[]>(path, {
      method: "GET",
    }),
  );
}

export function getReviewerPayouts(reviewerId: string) {
  return cachedApiCall(`reviewers:payouts:${reviewerId}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerDashboardPayout[]>(apiEndpoints.reviewers.payouts(reviewerId), {
      method: "GET",
    }),
  );
}

export function getReviewerBadges(reviewerId: string) {
  return cachedApiCall(`reviewers:badges:${reviewerId}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerDashboardBadgeHistoryItem[]>(apiEndpoints.reviewers.badges(reviewerId), {
      method: "GET",
    }),
  );
}
