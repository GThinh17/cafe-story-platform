import {
  apiCacheTtl,
  cachedApiCall,
  invalidateApiCache,
} from "@/lib/api/api-cache";
import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  BlogCreateRequest,
  BlogFeedParams,
  BlogFeedResponse,
  BlogLikeResponse,
  BlogPostStatus,
  BlogResponse,
  BlogSaveResponse,
  BlogTrendingResponse,
} from "@/types/blog";

type ApiRequestOptions = {
  headers?: HeadersInit;
};

type ActorContextOptions = ApiRequestOptions & {
  actorCafePageId?: string;
  actorContextType?: "USER" | "CAFE_PAGE";
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

export async function createBlog(
  request: BlogCreateRequest,
  options: ApiRequestOptions = {},
) {
  const response = await apiFetch<BlogResponse>(apiEndpoints.blogs.list, {
    body: request,
    headers: options.headers,
    method: "POST",
  });
  invalidateApiCache("blogs:by-user:");
  return response;
}

export async function createModeratedBlog(
  request: BlogCreateRequest,
  options: ApiRequestOptions = {},
) {
  const response = await apiFetch<BlogResponse>(apiEndpoints.blogs.moderated, {
    body: request,
    headers: options.headers,
    method: "POST",
  });
  invalidateApiCache("blogs:by-user:");
  return response;
}

export function getBlogById(blogId: string, options: ApiRequestOptions = {}) {
  return apiFetch<BlogResponse>(apiEndpoints.blogs.byId(blogId), {
    headers: options.headers,
    method: "GET",
  });
}

export function getBlogsByUser(
  userId: string,
  status?: BlogPostStatus,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(
    `blogs:by-user:${userId}:${status ?? "ALL"}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<BlogResponse[]>(
        withQuery(apiEndpoints.blogs.byUser(userId), { status }),
        {
          headers: options.headers,
          method: "GET",
        },
      ),
  );
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

export async function likeBlog(blogId: string, options: ActorContextOptions = {}) {
  const response = await apiFetch<BlogLikeResponse>(
    withQuery(apiEndpoints.blogs.likes(blogId), {
      actorCafePageId: options.actorCafePageId,
      actorContextType: options.actorContextType,
    }),
    {
    headers: options.headers,
    method: "POST",
    },
  );
  invalidateApiCache("blogs:likes-by-user:");
  return response;
}

export async function unlikeBlog(blogId: string, options: ActorContextOptions = {}) {
  await apiFetch<void>(withQuery(apiEndpoints.blogs.likes(blogId), {
    actorCafePageId: options.actorCafePageId,
    actorContextType: options.actorContextType,
  }), {
    headers: options.headers,
    method: "DELETE",
  });
  invalidateApiCache("blogs:likes-by-user:");
}

export function getBlogLikesByUser(userId: string, options: ApiRequestOptions = {}) {
  return cachedApiCall(
    `blogs:likes-by-user:${userId}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<BlogLikeResponse[]>(apiEndpoints.blogs.likesByUser(userId), {
        headers: options.headers,
        method: "GET",
      }),
  );
}
export function getSharedBlogsByUser(userId: string, options: ApiRequestOptions = {}) {
  return cachedApiCall(
    `blogs:shared-by-user:${userId}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<BlogResponse[]>(apiEndpoints.blogs.sharedByUser(userId), {
        headers: options.headers,
        method: "GET",
      }),
  );
}

export type BlogShareRequest = {
  actorCafePageId?: string;
  actorContextType?: "USER" | "CAFE_PAGE";
  shareType: "PUBLIC" | "PRIVATE" | "PAGE_ONLY";
};

export type BlogShareResponse = {
  id: string;
  blogId: string;
  userId: string;
  actorContextType?: "USER" | "CAFE_PAGE" | string | null;
  actorCafePageId?: string | null;
  actorDisplayName?: string | null;
  actorAvatarUrl?: string | null;
  shareType: string;
  createdAt: string | null;
};

export async function shareBlog(
  blogId: string,
  request: BlogShareRequest = { shareType: "PUBLIC" },
) {
  const response = await apiFetch<BlogShareResponse>(
    apiEndpoints.blogs.shares(blogId),
    {
      body: request,
      method: "POST",
    },
  );
  invalidateApiCache("blogs:shared-by-user:");
  return response;
}

export async function unshareBlog(blogId: string) {
  await apiFetch<void>(apiEndpoints.blogs.shares(blogId), {
    method: "DELETE",
  });
  invalidateApiCache("blogs:shared-by-user:");
}

export async function saveBlog(blogId: string, options: ApiRequestOptions = {}) {
  const response = await apiFetch<BlogSaveResponse>(
    apiEndpoints.blogs.saves(blogId),
    {
      headers: options.headers,
      method: "POST",
    },
  );
  invalidateBlogSaveCache();
  return response;
}

export async function unsaveBlog(blogId: string, options: ApiRequestOptions = {}) {
  await apiFetch<void>(apiEndpoints.blogs.saves(blogId), {
    headers: options.headers,
    method: "DELETE",
  });
  invalidateBlogSaveCache();
}

function invalidateBlogSaveCache() {
  invalidateApiCache("blogs:saves-by-user:");
  invalidateApiCache("blogs:saved-by-user:");
}

export function getBlogSavesByUser(
  userId: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(
    `blogs:saves-by-user:${userId}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<BlogSaveResponse[]>(apiEndpoints.blogs.savesByUser(userId), {
        headers: options.headers,
        method: "GET",
      }),
  );
}

export function getSavedBlogsByUserId(
  userId: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(
    `blogs:saved-by-user:${userId}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<BlogResponse[]>(apiEndpoints.blogs.savedByUser(userId), {
        headers: options.headers,
        method: "GET",
      }),
  );
}
