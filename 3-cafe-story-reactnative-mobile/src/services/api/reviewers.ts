import type { ReviewerResponse } from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getReviewerByUserId(userId: string) {
  return apiFetch<ReviewerResponse>(apiEndpoints.reviewers.byUser(userId), {
    method: "GET",
  });
}
