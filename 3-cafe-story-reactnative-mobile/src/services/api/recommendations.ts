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
