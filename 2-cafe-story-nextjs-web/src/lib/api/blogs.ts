import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  BlogCreateRequest,
  BlogFeedParams,
  BlogFeedResponse,
  BlogLikeResponse,
  BlogResponse,
  BlogTrendingResponse,
} from "@/types/blog";

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

export function getBlogFeed(
  params: BlogFeedParams = {},
  options: ApiRequestOptions = {},
) {
  return apiFetch<BlogFeedResponse[]>(
    withQuery(apiEndpoints.blogs.feed, {
      page: params.page,
      regionId: params.regionId,
      size: params.size,
      windowType: params.windowType,
    }),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}

export function getBlogs(options: ApiRequestOptions = {}) {
  return apiFetch<BlogResponse[]>(apiEndpoints.blogs.list, {
    headers: options.headers,
    method: "GET",
  });
}

export function createBlog(
  request: BlogCreateRequest,
  options: ApiRequestOptions = {},
) {
  return apiFetch<BlogResponse>(apiEndpoints.blogs.list, {
    body: request,
    headers: options.headers,
    method: "POST",
  });
}

export function getBlogById(blogId: string, options: ApiRequestOptions = {}) {
  return apiFetch<BlogResponse>(apiEndpoints.blogs.byId(blogId), {
    headers: options.headers,
    method: "GET",
  });
}

export function getBlogsByUser(userId: string, options: ApiRequestOptions = {}) {
  return apiFetch<BlogResponse[]>(apiEndpoints.blogs.byUser(userId), {
    headers: options.headers,
    method: "GET",
  });
}

export function getTrendingBlogs(
  params: Omit<BlogFeedParams, "regionId"> = {},
  options: ApiRequestOptions = {},
) {
  return apiFetch<BlogTrendingResponse[]>(
    withQuery(apiEndpoints.blogs.trending, {
      page: params.page,
      size: params.size,
      windowType: params.windowType,
    }),
    {
      headers: options.headers,
      method: "GET",
    },
  );
}

export function likeBlog(blogId: string, options: ApiRequestOptions = {}) {
  return apiFetch<BlogLikeResponse>(apiEndpoints.blogs.likes(blogId), {
    headers: options.headers,
    method: "POST",
  });
}

export function unlikeBlog(blogId: string, options: ApiRequestOptions = {}) {
  return apiFetch<void>(apiEndpoints.blogs.likes(blogId), {
    headers: options.headers,
    method: "DELETE",
  });
}

export function getBlogLikesByUser(userId: string, options: ApiRequestOptions = {}) {
  return apiFetch<BlogLikeResponse[]>(apiEndpoints.blogs.likesByUser(userId), {
    headers: options.headers,
    method: "GET",
  });
}
