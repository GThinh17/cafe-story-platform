import type {
  FeedImpressionBatchRequest,
  FeedImpressionResponse,
  FeedParams,
  FeedResponse,
} from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";
import { logPerformanceMetric, performanceTimestamp } from "../../utils/performance";

function withQuery(path: string, params: Record<string, string | number | null | undefined>) {
  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.set(key, String(value));
    }
  }

  const query = searchParams.toString();

  return query ? `${path}?${query}` : path;
}

export function getMixedFeed(params: FeedParams = {}) {
  const path = withQuery(apiEndpoints.feed.list, {
    cursor: params.cursor,
    size: params.size,
  });

  const fetchFromApi = async () => {
    const startedAt = performanceTimestamp();
    try {
      return await apiFetch<FeedResponse>(path, {
        method: "GET",
      });
    } finally {
      logPerformanceMetric("mobile.feed.api", startedAt, {
        bypassCache: Boolean(params.bypassCache),
        pageSize: params.size ?? null,
      });
    }
  };

  if (params.bypassCache) {
    return fetchFromApi();
  }

  return cachedApiCall(`feed:mixed:${path}`, apiCacheTtl.dynamic, fetchFromApi);
}

export function recordFeedImpressions(request: FeedImpressionBatchRequest) {
  return apiFetch<FeedImpressionResponse>(apiEndpoints.feed.impressions, {
    body: request,
    method: "POST",
  });
}
