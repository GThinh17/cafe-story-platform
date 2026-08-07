import {
  apiCacheTtl,
  cachedApiCall,
  invalidateApiCache,
} from "@/lib/api/api-cache";
import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  CafePageBlogCursorResponse,
  CafePageCreateRequest,
  CafePageFollowResponse,
  CafePageLikeResponse,
  CafePageRankingResponse,
  CafePageRatingResponse,
  CafePageResponse,
  CafeTopParams,
  CafePageUpdateRequest,
  PageMemberAddRequest,
  PageMemberResponse,
  PageMemberStatus,
} from "@/types/cafe";

type ApiRequestOptions = {
  headers?: HeadersInit;
};

type CafePageBlogParams = {
  cursor?: string | null;
  size?: number;
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

export function getAllCafePages(
  params: { query?: string; status?: string } = {},
  options: ApiRequestOptions = {},
) {
  return apiFetch<CafePageResponse[]>(
    withQuery(apiEndpoints.cafes.list, {
      query: params.query,
      status: params.status,
    }),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}

export function getTopCafePages(
  params: CafeTopParams = {},
  options: ApiRequestOptions = {},
) {
  const path = withQuery(apiEndpoints.cafes.top, {
    city: params.city,
    area: params.area,
    province: params.province,
    regionId: params.regionId,
    size: params.size,
  });

  return cachedApiCall(`cafes:top:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<CafePageRankingResponse[]>(path, {
      headers: options.headers,
      method: "GET",
    }),
  );
}

export function getFollowedCafePagesByUserId(
  userId: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(
    `cafes:follows-by-user:${userId}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<CafePageFollowResponse[]>(
        apiEndpoints.cafes.followsByUser(userId),
        {
          headers: options.headers,
          method: "GET",
        },
      ),
  );
}

export async function followCafePage(cafePageId: string) {
  await apiFetch<void>(apiEndpoints.cafes.follows(cafePageId), {
    method: "POST",
  });
  invalidateCafeFollowCache(cafePageId);
}

export async function unfollowCafePage(cafePageId: string) {
  await apiFetch<void>(apiEndpoints.cafes.follows(cafePageId), {
    method: "DELETE",
  });
  invalidateCafeFollowCache(cafePageId);
}

function invalidateCafeFollowCache(cafePageId: string) {
  invalidateApiCache(`cafes:detail:${cafePageId}`);
  invalidateApiCache("cafes:follows-by-user:");
  invalidateApiCache("users:following-targets:");
}

export function getCafePageById(
  cafePageId: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(
    `cafes:detail:${cafePageId}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<CafePageResponse>(apiEndpoints.cafes.byId(cafePageId), {
        headers: options.headers,
        method: "GET",
      }),
  );
}

export function getCafePagesByOwnerId(
  ownerUserId: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(
    `cafes:by-owner:${ownerUserId}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<CafePageResponse[]>(
        withQuery(apiEndpoints.cafes.list, {
          ownerUserId,
        }),
        {
          headers: options.headers,
          method: "GET",
        },
      ),
  );
}

export function createCafePage(request: CafePageCreateRequest) {
  return apiFetch<CafePageResponse>(apiEndpoints.cafes.list, {
    method: "POST",
    body: request,
  });
}

export async function updateCafePage(
  cafePageId: string,
  request: CafePageUpdateRequest,
) {
  const response = await apiFetch<CafePageResponse>(
    apiEndpoints.cafes.byId(cafePageId),
    {
      method: "PATCH",
      body: request,
    },
  );
  invalidateApiCache(`cafes:detail:${cafePageId}`);
  invalidateApiCache("cafes:by-owner:");
  return response;
}

export function getBlogsByCafePageId(
  cafePageId: string,
  params: CafePageBlogParams = {},
  options: ApiRequestOptions = {},
) {
  return apiFetch<CafePageBlogCursorResponse>(
    withQuery(apiEndpoints.cafes.blogs(cafePageId), {
      cursor: params.cursor ?? undefined,
      size: params.size,
    }),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}

export async function likeCafePage(
  cafePageId: string,
  options: ApiRequestOptions = {},
) {
  const response = await apiFetch<CafePageLikeResponse>(
    apiEndpoints.cafes.likes(cafePageId),
    {
      headers: options.headers,
      method: "POST",
    },
  );
  invalidateApiCache(`cafes:detail:${cafePageId}`);
  return response;
}

export async function unlikeCafePage(
  cafePageId: string,
  options: ApiRequestOptions = {},
) {
  await apiFetch<void>(apiEndpoints.cafes.likes(cafePageId), {
    headers: options.headers,
    method: "DELETE",
  });
  invalidateApiCache(`cafes:detail:${cafePageId}`);
}

/**
 * Upsert the viewer's 1-5 rating. The backend returns the recomputed average and
 * count, so callers can refresh the display without refetching the cafe page.
 */
export async function rateCafePage(
  cafePageId: string,
  rating: number,
  options: ApiRequestOptions = {},
) {
  const response = await apiFetch<CafePageRatingResponse>(
    apiEndpoints.cafes.rating(cafePageId),
    {
      headers: options.headers,
      method: "PUT",
      body: { rating },
    },
  );
  invalidateApiCache(`cafes:detail:${cafePageId}`);
  return response;
}

export async function deleteCafePageRating(
  cafePageId: string,
  options: ApiRequestOptions = {},
) {
  await apiFetch<void>(apiEndpoints.cafes.rating(cafePageId), {
    headers: options.headers,
    method: "DELETE",
  });
  invalidateApiCache(`cafes:detail:${cafePageId}`);
}

export function getPageMembers(
  cafePageId: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<PageMemberResponse[]>(apiEndpoints.cafes.members(cafePageId), {
    headers: options.headers,
    method: "GET",
  });
}

export function addPageMember(
  cafePageId: string,
  request: PageMemberAddRequest,
) {
  return apiFetch<PageMemberResponse>(apiEndpoints.cafes.members(cafePageId), {
    method: "POST",
    body: request,
  });
}

export function updatePageMemberStatus(
  cafePageId: string,
  userId: string,
  status: PageMemberStatus,
) {
  return apiFetch<PageMemberResponse>(
    apiEndpoints.cafes.memberStatus(cafePageId, userId),
    {
      method: "PATCH",
      body: { status },
    },
  );
}
