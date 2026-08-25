import type { ExploreSearchResults } from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

const MAX_SEARCH_RESULTS_PER_GROUP = 8;

export async function searchExplore(query: string): Promise<ExploreSearchResults> {
  const normalizedQuery = query.trim();

  if (normalizedQuery.length < 2) {
    return {
      blogs: [],
      cafePages: [],
      users: [],
    };
  }

  const path = apiEndpoints.search.explore(
    normalizedQuery,
    MAX_SEARCH_RESULTS_PER_GROUP,
  );

  return cachedApiCall(
    `search:explore:${normalizedQuery.toLowerCase()}`,
    apiCacheTtl.dynamic,
    () => apiFetch<ExploreSearchResults>(path, { method: "GET" }),
  );
}
