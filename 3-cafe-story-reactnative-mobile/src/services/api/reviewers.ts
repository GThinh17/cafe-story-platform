import type { ReviewerResponse } from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getReviewerByUserId(userId: string) {
  return cachedApiCall(`reviewers:user:${userId}`, apiCacheTtl.dynamic, () =>
    apiFetch<ReviewerResponse>(apiEndpoints.reviewers.byUser(userId), {
      method: "GET",
    }),
  );
}
