import type { RecommendationCardResponse } from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getUserRecommendations(page = 0, size = 20) {
  return apiFetch<RecommendationCardResponse[]>(
    apiEndpoints.recommendations.users(page, size),
    {
      method: "GET",
    },
  );
}

export function getMixedRecommendations(page = 0, size = 30) {
  return apiFetch<RecommendationCardResponse[]>(
    apiEndpoints.recommendations.mixed(page, size),
    {
      method: "GET",
    },
  );
}

export function getReviewerRecommendations(page = 0, size = 20) {
  return apiFetch<RecommendationCardResponse[]>(
    apiEndpoints.recommendations.reviewers(page, size),
    {
      method: "GET",
    },
  );
}

export function getCafePageRecommendations(page = 0, size = 20) {
  return apiFetch<RecommendationCardResponse[]>(
    apiEndpoints.recommendations.cafePages(page, size),
    {
      method: "GET",
    },
  );
}
