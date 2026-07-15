import type {
  FeedImpressionBatchRequest,
  FeedImpressionResponse,
  FeedParams,
  FeedResponse,
} from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

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

  if (params.bypassCache) {
    return apiFetch<FeedResponse>(path, {
      method: "GET",
    });
  }

  return cachedApiCall(`feed:mixed:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<FeedResponse>(path, {
      method: "GET",
    }),
  );
}

export function recordFeedImpressions(request: FeedImpressionBatchRequest) {
  return apiFetch<FeedImpressionResponse>(apiEndpoints.feed.impressions, {
    body: request,
    method: "POST",
  });
}
