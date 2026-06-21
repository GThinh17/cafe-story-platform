import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  ReviewerConnectOnboardResponse,
  ReviewerConnectStatus,
  ReviewerPayoutResponse,
  ReviewerResponse,
} from "@/types/reviewer";

export function getTopReviewers(size = 20) {
  return apiFetch<ReviewerResponse[]>(
    `${apiEndpoints.reviewers.top}?size=${size}`,
    { method: "GET" },
  );
}

export function getReviewerByUserId(userId: string) {
  return apiFetch<ReviewerResponse>(apiEndpoints.reviewers.byUserId(userId), {
    method: "GET",
  });
}

export function getReviewerPayouts(reviewerId: string) {
  return apiFetch<ReviewerPayoutResponse[]>(apiEndpoints.reviewers.payouts(reviewerId), {
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

export function syncConnectStatus() {
  return apiFetch<ReviewerConnectStatus>(apiEndpoints.reviewers.connect.sync, {
    method: "POST",
  });
}
