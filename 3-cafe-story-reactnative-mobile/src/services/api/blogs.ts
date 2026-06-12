import type {
  BlogFeedParams,
  BlogFeedResponse,
  BlogLikeResponse,
  BlogResponse,
  BlogSaveResponse,
  BlogShareRequest,
  BlogShareResponse,
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

export function getBlogFeed(params: BlogFeedParams = {}) {
  return apiFetch<BlogFeedResponse[]>(
    withQuery(apiEndpoints.blogs.feed, {
      page: params.page,
      regionId: params.regionId,
      size: params.size,
      windowType: params.windowType,
    }),
    {
      method: "GET",
    },
  );
}

export function getBlogsByUser(userId: string) {
  return apiFetch<BlogResponse[]>(apiEndpoints.blogs.byUser(userId), {
    method: "GET",
  });
}

export function likeBlog(blogId: string) {
  return apiFetch<BlogLikeResponse>(apiEndpoints.blogs.likes(blogId), {
    method: "POST",
  });
}

export function unlikeBlog(blogId: string) {
  return apiFetch<void>(apiEndpoints.blogs.likes(blogId), {
    method: "DELETE",
  });
}

export function getBlogLikesByUser(userId: string) {
  return apiFetch<BlogLikeResponse[]>(apiEndpoints.blogs.likesByUser(userId), {
    method: "GET",
  });
}

export function saveBlog(blogId: string) {
  return apiFetch<BlogSaveResponse>(apiEndpoints.blogs.saves(blogId), {
    method: "POST",
  });
}

export function unsaveBlog(blogId: string) {
  return apiFetch<void>(apiEndpoints.blogs.saves(blogId), {
    method: "DELETE",
  });
}

export function getBlogSavesByUser(userId: string) {
  return apiFetch<BlogSaveResponse[]>(apiEndpoints.blogs.savesByUser(userId), {
    method: "GET",
  });
}

export function getMyBlogSaves() {
  return apiFetch<BlogSaveResponse[]>(apiEndpoints.blogs.savesMe, {
    method: "GET",
  });
}

export function shareBlog(
  blogId: string,
  request: BlogShareRequest = { shareType: "PUBLIC" },
) {
  return apiFetch<BlogShareResponse>(apiEndpoints.blogs.shares(blogId), {
    body: request,
    method: "POST",
  });
}
