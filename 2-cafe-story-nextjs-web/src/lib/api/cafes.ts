import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  CafePageBlogCursorResponse,
  CafePageCreateRequest,
  CafePageFollowResponse,
  CafePageLikeResponse,
  CafePageRankingResponse,
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
  return apiFetch<CafePageRankingResponse[]>(
    withQuery(apiEndpoints.cafes.top, {
      city: params.city,
      area: params.area,
      province: params.province,
      regionId: params.regionId,
      size: params.size,
    }),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}

export function getFollowedCafePagesByUserId(
  userId: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<CafePageFollowResponse[]>(
    apiEndpoints.cafes.followsByUser(userId),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}

export function followCafePage(cafePageId: string) {
  return apiFetch<void>(apiEndpoints.cafes.follows(cafePageId), {
    method: "POST",
  });
}

export function unfollowCafePage(cafePageId: string) {
  return apiFetch<void>(apiEndpoints.cafes.follows(cafePageId), {
    method: "DELETE",
  });
}

export function getCafePageById(
  cafePageId: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<CafePageResponse>(apiEndpoints.cafes.byId(cafePageId), {
    headers: options.headers,
    method: "GET",
  });
}

export function getCafePagesByOwnerId(
  ownerUserId: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<CafePageResponse[]>(
    withQuery(apiEndpoints.cafes.list, {
      ownerUserId,
    }),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}

export function createCafePage(request: CafePageCreateRequest) {
  return apiFetch<CafePageResponse>(apiEndpoints.cafes.list, {
    method: "POST",
    body: request,
  });
}

export function updateCafePage(
  cafePageId: string,
  request: CafePageUpdateRequest,
) {
  return apiFetch<CafePageResponse>(apiEndpoints.cafes.byId(cafePageId), {
    method: "PATCH",
    body: request,
  });
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

export function likeCafePage(
  cafePageId: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<CafePageLikeResponse>(apiEndpoints.cafes.likes(cafePageId), {
    headers: options.headers,
    method: "POST",
  });
}

export function unlikeCafePage(
  cafePageId: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<void>(apiEndpoints.cafes.likes(cafePageId), {
    headers: options.headers,
    method: "DELETE",
  });
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
