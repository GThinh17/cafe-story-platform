import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { CafePageRankingResponse, CafeTopParams } from "@/types/cafe";

type ApiRequestOptions = {
  headers?: HeadersInit;
};

function withQuery(path: string, params: Record<string, string | number | undefined>) {
  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") {
      searchParams.set(key, String(value));
    }
  }

  const query = searchParams.toString();

  return query ? `${path}?${query}` : path;
}

export function getTopCafePages(
  params: CafeTopParams = {},
  options: ApiRequestOptions = {},
) {
  return apiFetch<CafePageRankingResponse[]>(
    withQuery(apiEndpoints.cafes.top, {
      city: params.city,
      regionId: params.regionId,
      size: params.size,
    }),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}
