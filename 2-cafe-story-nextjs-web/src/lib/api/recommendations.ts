import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { RecommendationCardResponse } from "@/types/recommendation";

type ApiRequestOptions = {
  headers?: HeadersInit;
};

export function getMixedRecommendations(
  page = 0,
  size = 30,
  options: ApiRequestOptions = {},
) {
  return apiFetch<RecommendationCardResponse[]>(
    apiEndpoints.recommendations.mixed(page, size),
    { headers: options.headers, method: "GET" },
  );
}

export function getCafePageRecommendations(
  page = 0,
  size = 20,
  options: ApiRequestOptions = {},
) {
  return apiFetch<RecommendationCardResponse[]>(
    apiEndpoints.recommendations.cafePages(page, size),
    { headers: options.headers, method: "GET" },
  );
}

export function getReviewerRecommendations(
  page = 0,
  size = 20,
  options: ApiRequestOptions = {},
) {
  return apiFetch<RecommendationCardResponse[]>(
    apiEndpoints.recommendations.reviewers(page, size),
    { headers: options.headers, method: "GET" },
  );
}
