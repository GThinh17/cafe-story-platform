import type {
  BlogFeedParams,
  BlogFeedResponse,
  BlogCreateRequest,
  BlogLikeResponse,
  BlogResponse,
  BlogSaveResponse,
  BlogShareRequest,
  BlogShareResponse,
  BlogTrendingResponse,
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

export function getBlogFeed(params: BlogFeedParams = {}) {
  const path = withQuery(apiEndpoints.blogs.feed, {
      page: params.page,
      regionId: params.regionId,
      size: params.size,
      windowType: params.windowType,
    });
  return cachedApiCall(`feed:personalized:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<BlogFeedResponse[]>(path, {
      method: "GET",
    }),
  );
}

export async function createBlog(request: BlogCreateRequest) {
  const response = await apiFetch<BlogResponse>(apiEndpoints.blogs.list, {
    body: request,
    method: "POST",
  });
  invalidateApiCache("feed:");
  invalidateApiCache("blogs:");
  invalidateApiCache("cafe-pages:");
  invalidateApiCache("recommendations:");
  return response;
}

export function getBlogs() {
  return cachedApiCall("blogs:list", apiCacheTtl.dynamic, () =>
    apiFetch<BlogResponse[]>(apiEndpoints.blogs.list, {
      method: "GET",
    }),
  );
}

export function getBlogById(blogId: string) {
  return cachedApiCall(`blogs:detail:${blogId}`, apiCacheTtl.dynamic, () =>
    apiFetch<BlogResponse>(apiEndpoints.blogs.byId(blogId), {
      method: "GET",
    }),
  );
}

export function getTrendingBlogs(params: Omit<BlogFeedParams, "regionId"> = {}) {
  const path = withQuery(apiEndpoints.blogs.trending, {
      page: params.page,
      size: params.size,
      windowType: params.windowType,
    });
  return cachedApiCall(`blogs:trending:${path}`, apiCacheTtl.dynamic, () =>
    apiFetch<BlogTrendingResponse[]>(path, {
      method: "GET",
    }),
  );
}

export function getBlogsByUser(userId: string) {
  return cachedApiCall(`blogs:user:${userId}`, apiCacheTtl.dynamic, () =>
    apiFetch<BlogResponse[]>(apiEndpoints.blogs.byUser(userId), {
      method: "GET",
    }),
  );
}

export function getSavedBlogsByUser(userId: string) {
  return cachedApiCall(`blogs:user:${userId}:saved`, apiCacheTtl.comments, () =>
    apiFetch<BlogResponse[]>(apiEndpoints.blogs.savedByUser(userId), {
      method: "GET",
    }),
  );
}

export function getSharedBlogsByUser(
  userId: string,
  params: { sort?: "recent" | "shareCount" } = {},
) {
  const path = withQuery(apiEndpoints.blogs.sharedByUser(userId), {
      sort: params.sort === "shareCount" ? "shareCount" : undefined,
    });
  return cachedApiCall(`blogs:user:${userId}:shared:${params.sort ?? "recent"}`, apiCacheTtl.dynamic, () =>
    apiFetch<BlogResponse[]>(path, {
      method: "GET",
    }),
  );
}

export function getTaggedBlogsByUser(userId: string) {
  return cachedApiCall(`blogs:user:${userId}:tagged`, apiCacheTtl.dynamic, () =>
    apiFetch<BlogResponse[]>(apiEndpoints.blogs.taggedByUser(userId), {
      method: "GET",
    }),
  );
}

export async function likeBlog(blogId: string) {
  const response = await apiFetch<BlogLikeResponse>(apiEndpoints.blogs.likes(blogId), {
    method: "POST",
  });
  invalidateBlogInteractionCache(blogId);
  return response;
}

export async function unlikeBlog(blogId: string) {
  const response = await apiFetch<void>(apiEndpoints.blogs.likes(blogId), {
    method: "DELETE",
  });
  invalidateBlogInteractionCache(blogId);
  return response;
}

export function getBlogLikesByUser(userId: string) {
  return cachedApiCall(`blogs:likes:user:${userId}`, apiCacheTtl.dynamic, () =>
    apiFetch<BlogLikeResponse[]>(apiEndpoints.blogs.likesByUser(userId), {
      method: "GET",
    }),
  );
}

export async function saveBlog(blogId: string) {
  const response = await apiFetch<BlogSaveResponse>(apiEndpoints.blogs.saves(blogId), {
    method: "POST",
  });
  invalidateBlogInteractionCache(blogId);
  invalidateApiCache("blogs:user:");
  return response;
}

export async function unsaveBlog(blogId: string) {
  const response = await apiFetch<void>(apiEndpoints.blogs.saves(blogId), {
    method: "DELETE",
  });
  invalidateBlogInteractionCache(blogId);
  invalidateApiCache("blogs:user:");
  return response;
}

export function getBlogSavesByUser(userId: string) {
  return cachedApiCall(`blogs:saves:user:${userId}`, apiCacheTtl.comments, () =>
    apiFetch<BlogSaveResponse[]>(apiEndpoints.blogs.savesByUser(userId), {
      method: "GET",
    }),
  );
}

export function getMyBlogSaves() {
  return cachedApiCall("blogs:saves:me", apiCacheTtl.comments, () =>
    apiFetch<BlogSaveResponse[]>(apiEndpoints.blogs.savesMe, {
      method: "GET",
    }),
  );
}

export async function shareBlog(
  blogId: string,
  request: BlogShareRequest = { shareType: "PUBLIC" },
) {
  const response = await apiFetch<BlogShareResponse>(apiEndpoints.blogs.shares(blogId), {
    body: request,
    method: "POST",
  });
  invalidateBlogInteractionCache(blogId);
  invalidateApiCache("blogs:user:");
  return response;
}

function invalidateBlogInteractionCache(blogId: string) {
  invalidateApiCache(`blogs:detail:${blogId}`);
  invalidateApiCache("feed:");
}
