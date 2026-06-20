import type {
  CafePageBlogPageResponse,
  CafePageResponse,
  CafePageUpdateRequest,
  PageFollowResponse,
  PageLikeResponse,
} from "../../types";
import { apiCacheTtl, cachedApiCall, invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

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

export function getCafePagesByOwner(ownerUserId: string) {
  const path = withQuery(apiEndpoints.cafePages.list, { ownerUserId });
  return cachedApiCall(`cafe-pages:owner:${ownerUserId}`, apiCacheTtl.dynamic, () =>
    apiFetch<CafePageResponse[]>(path, {
      method: "GET",
    }),
  );
}

export function getCafePageById(cafePageId: string) {
  return cachedApiCall(`cafe-pages:detail:${cafePageId}`, apiCacheTtl.dynamic, () =>
    apiFetch<CafePageResponse>(apiEndpoints.cafePages.byId(cafePageId), {
      method: "GET",
    }),
  );
}

export async function updateCafePage(cafePageId: string, request: CafePageUpdateRequest) {
  const response = await apiFetch<CafePageResponse>(apiEndpoints.cafePages.byId(cafePageId), {
    body: request,
    method: "PATCH",
  });
  invalidateCafePageCache(cafePageId);
  return response;
}

export function getCafePageBlogs(
  cafePageId: string,
  params: { cursor?: string; size?: number } = {},
) {
  const path = withQuery(apiEndpoints.cafePages.blogs(cafePageId), {
      cursor: params.cursor,
      size: params.size,
    });
  return cachedApiCall(`cafe-pages:blogs:${cafePageId}:${params.cursor ?? "first"}:${params.size ?? "default"}`, apiCacheTtl.dynamic, () =>
    apiFetch<CafePageBlogPageResponse>(path, {
      method: "GET",
    }),
  );
}

export async function followCafePage(cafePageId: string) {
  const response = await apiFetch<PageFollowResponse>(apiEndpoints.cafePages.follows(cafePageId), {
    method: "POST",
  });
  invalidateCafePageCache(cafePageId);
  return response;
}

export async function unfollowCafePage(cafePageId: string) {
  const response = await apiFetch<void>(apiEndpoints.cafePages.follows(cafePageId), {
    method: "DELETE",
  });
  invalidateCafePageCache(cafePageId);
  return response;
}

export async function likeCafePage(cafePageId: string) {
  const response = await apiFetch<PageLikeResponse>(apiEndpoints.cafePages.likes(cafePageId), {
    method: "POST",
  });
  invalidateCafePageCache(cafePageId);
  return response;
}

export async function unlikeCafePage(cafePageId: string) {
  const response = await apiFetch<void>(apiEndpoints.cafePages.likes(cafePageId), {
    method: "DELETE",
  });
  invalidateCafePageCache(cafePageId);
  return response;
}

function invalidateCafePageCache(cafePageId: string) {
  invalidateApiCache(`cafe-pages:detail:${cafePageId}`);
  invalidateApiCache(`cafe-pages:blogs:${cafePageId}`);
  invalidateApiCache("cafe-pages:owner:");
  invalidateApiCache("users:following-targets:");
  invalidateApiCache("recommendations:");
  invalidateApiCache("feed:");
}
