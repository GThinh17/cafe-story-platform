import type {
  CafePageBlogPageResponse,
  CafePageResponse,
  PageFollowResponse,
  PageLikeResponse,
} from "../../types";
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
  return apiFetch<CafePageResponse[]>(
    withQuery(apiEndpoints.cafePages.list, { ownerUserId }),
    {
      method: "GET",
    },
  );
}

export function getCafePageById(cafePageId: string) {
  return apiFetch<CafePageResponse>(apiEndpoints.cafePages.byId(cafePageId), {
    method: "GET",
  });
}

export function getCafePageBlogs(
  cafePageId: string,
  params: { cursor?: string; size?: number } = {},
) {
  return apiFetch<CafePageBlogPageResponse>(
    withQuery(apiEndpoints.cafePages.blogs(cafePageId), {
      cursor: params.cursor,
      size: params.size,
    }),
    {
      method: "GET",
    },
  );
}

export function followCafePage(cafePageId: string) {
  return apiFetch<PageFollowResponse>(apiEndpoints.cafePages.follows(cafePageId), {
    method: "POST",
  });
}

export function unfollowCafePage(cafePageId: string) {
  return apiFetch<void>(apiEndpoints.cafePages.follows(cafePageId), {
    method: "DELETE",
  });
}

export function likeCafePage(cafePageId: string) {
  return apiFetch<PageLikeResponse>(apiEndpoints.cafePages.likes(cafePageId), {
    method: "POST",
  });
}

export function unlikeCafePage(cafePageId: string) {
  return apiFetch<void>(apiEndpoints.cafePages.likes(cafePageId), {
    method: "DELETE",
  });
}
