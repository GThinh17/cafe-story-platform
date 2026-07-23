import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { MixedFeedParams, MixedFeedResponse } from "@/types/feed";

type ApiRequestOptions = {
  headers?: HeadersInit;
};

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

export function getMixedFeed(
  params: MixedFeedParams = {},
  options: ApiRequestOptions = {},
) {
  return apiFetch<MixedFeedResponse>(
    withQuery(apiEndpoints.feed.list, {
      cursor: params.cursor,
      size: params.size,
    }),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}
