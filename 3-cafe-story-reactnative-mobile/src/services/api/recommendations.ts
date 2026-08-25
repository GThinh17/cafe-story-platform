import type { RecommendationCardResponse } from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getUserRecommendations(page = 0, size = 20) {
  const path = apiEndpoints.recommendations.users(page, size);
  return cachedApiCall(`recommendations:users:${page}:${size}`, apiCacheTtl.dynamic, () =>
    apiFetch<RecommendationCardResponse[]>(path, {
      method: "GET",
    }),
  );
}

export function getMixedRecommendations(page = 0, size = 30) {
  const path = apiEndpoints.recommendations.mixed(page, size);
  return cachedApiCall(`recommendations:mixed:${page}:${size}`, apiCacheTtl.dynamic, () =>
    apiFetch<RecommendationCardResponse[]>(path, {
      method: "GET",
    }),
  );
}

export function getReviewerRecommendations(page = 0, size = 20) {
  const path = apiEndpoints.recommendations.reviewers(page, size);
  return cachedApiCall(`recommendations:reviewers:${page}:${size}`, apiCacheTtl.dynamic, () =>
    apiFetch<RecommendationCardResponse[]>(path, {
      method: "GET",
    }),
  );
}

export function getCafePageRecommendations(page = 0, size = 20) {
  const path = apiEndpoints.recommendations.cafePages(page, size);
  return cachedApiCall(`recommendations:cafe-pages:${page}:${size}`, apiCacheTtl.dynamic, () =>
    apiFetch<RecommendationCardResponse[]>(path, {
      method: "GET",
    }),
  );
}
