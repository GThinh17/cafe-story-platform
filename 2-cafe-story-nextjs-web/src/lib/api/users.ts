import {
  apiCacheTtl,
  cachedApiCall,
  invalidateApiCache,
} from "@/lib/api/api-cache";
import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  FollowTargetResponse,
  UserFollowResponse,
  UserResponse,
} from "@/types/user";

type ApiRequestOptions = {
  headers?: HeadersInit;
};

export type UpdateMeRequest = {
  userName?: string;
  userFullName?: string;
  userEmail?: string;
  userPhone?: number;
  userAvatar?: string;
  hideCafePageOnProfile?: boolean;
};

export type UpdateMeRegionRequest = {
  cityCode?: string;
  city?: string;
  provinceCode?: string;
  province?: string;
  wardCode?: string;
  district?: string;
  ward?: string;
  area?: string;
  street?: string;
};

export function getUserByUsername(
  username: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(
    `users:by-username:${username}`,
    apiCacheTtl.shortUser,
    () =>
      apiFetch<UserResponse>(apiEndpoints.users.byUsername(username), {
        headers: options.headers,
        method: "GET",
      }),
  );
}

export function getUserById(
  userId: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(`users:detail:${userId}`, apiCacheTtl.shortUser, () =>
    apiFetch<UserResponse>(apiEndpoints.users.byId(userId), {
      headers: options.headers,
      method: "GET",
    }),
  );
}

export function getFollowingByUser(
  userId: string,
  options: ApiRequestOptions = {},
) {
  return getFollowingByUserId(userId, options);
}

export function getFollowersByUserId(
  userId: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(`users:followers:${userId}`, apiCacheTtl.dynamic, () =>
    apiFetch<UserFollowResponse[]>(apiEndpoints.users.followers(userId), {
      headers: options.headers,
      method: "GET",
    }),
  );
}

export function getFollowingByUserId(
  userId: string,
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(`users:following:${userId}`, apiCacheTtl.dynamic, () =>
    apiFetch<UserFollowResponse[]>(apiEndpoints.users.following(userId), {
      headers: options.headers,
      method: "GET",
    }),
  );
}

export function getFollowingTargetsByUserId(
  userId: string,
  type = "ALL",
  options: ApiRequestOptions = {},
) {
  return cachedApiCall(
    `users:following-targets:${userId}:${type}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<FollowTargetResponse[]>(
        apiEndpoints.users.followingTargets(userId, type),
        {
          headers: options.headers,
          method: "GET",
        },
      ),
  );
}

export async function updateMe(request: UpdateMeRequest) {
  const response = await apiFetch<UserResponse>(apiEndpoints.users.me, {
    method: "PATCH",
    body: request,
  });
  invalidateApiCache("users:");
  invalidateApiCache("auth:me");
  return response;
}

export async function updateMeRegion(request: UpdateMeRegionRequest) {
  const response = await apiFetch<UserResponse>(apiEndpoints.users.meRegion, {
    method: "PATCH",
    body: request,
  });
  invalidateApiCache("users:");
  invalidateApiCache("auth:me");
  return response;
}

export async function followUser(
  followingUserId: string,
  options: ApiRequestOptions = {},
) {
  const response = await apiFetch<UserFollowResponse>(
    apiEndpoints.users.follow(followingUserId),
    {
      headers: options.headers,
      method: "POST",
    },
  );
  invalidateFollowCache(followingUserId);
  return response;
}

export async function unfollowUser(
  followingUserId: string,
  options: ApiRequestOptions = {},
) {
  await apiFetch<void>(apiEndpoints.users.follow(followingUserId), {
    headers: options.headers,
    method: "DELETE",
  });
  invalidateFollowCache(followingUserId);
}

function invalidateFollowCache(userId: string) {
  invalidateApiCache(`users:detail:${userId}`);
  invalidateApiCache("users:by-username:");
  invalidateApiCache("users:following:");
  invalidateApiCache("users:following-targets:");
  invalidateApiCache("users:followers:");
  invalidateApiCache("auth:me");
}
