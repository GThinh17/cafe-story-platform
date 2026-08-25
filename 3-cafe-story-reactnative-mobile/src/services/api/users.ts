import type {
  FollowTargetResponse,
  UserFollowResponse,
  UserRegionUpdateRequest,
  UserResponse,
  UserUpdateRequest,
} from "../../types";
import { apiCacheTtl, cachedApiCall, invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export type AvatarUploadFile = {
  name: string;
  type: string;
  uri: string;
};

export function getMyProfile() {
  return cachedApiCall("users:me", apiCacheTtl.shortUser, () =>
    apiFetch<UserResponse>(apiEndpoints.users.me, {
      method: "GET",
    }),
  );
}

export function getUserProfile(userId: string) {
  return cachedApiCall(`users:detail:${userId}`, apiCacheTtl.shortUser, () =>
    apiFetch<UserResponse>(apiEndpoints.users.byId(userId), {
      method: "GET",
    }),
  );
}

export function getUserProfileByUsername(username: string) {
  return cachedApiCall(`users:username:${username}`, apiCacheTtl.dynamic, () =>
    apiFetch<UserResponse>(apiEndpoints.users.byUsername(username), {
      method: "GET",
    }),
  );
}

export function getUsers() {
  return cachedApiCall("users:list", apiCacheTtl.dynamic, () =>
    apiFetch<UserResponse[]>(apiEndpoints.users.list, {
      method: "GET",
    }),
  );
}

export async function updateMyProfile(request: UserUpdateRequest) {
  const response = await apiFetch<UserResponse>(apiEndpoints.users.me, {
    body: request,
    method: "PATCH",
  });
  invalidateApiCache("users:");
  invalidateApiCache("blogs:");
  return response;
}

export async function uploadMyAvatar(file: AvatarUploadFile) {
  const formData = new FormData();
  formData.append("avatar", file as unknown as Blob);

  const response = await apiFetch<UserResponse>(apiEndpoints.users.meAvatar, {
    body: formData,
    method: "PATCH",
  });
  invalidateApiCache("users:");
  invalidateApiCache("blogs:");
  return response;
}

export async function updateMyRegion(request: UserRegionUpdateRequest) {
  const response = await apiFetch<UserResponse>(apiEndpoints.users.meRegion, {
    body: request,
    method: "PATCH",
  });
  invalidateApiCache("users:");
  invalidateApiCache("recommendations:");
  invalidateApiCache("feed:");
  return response;
}

export async function followUser(followingUserId: string) {
  const response = await apiFetch<UserFollowResponse>(apiEndpoints.users.follow(followingUserId), {
    method: "POST",
  });
  invalidateFollowCache(followingUserId);
  return response;
}

export async function unfollowUser(followingUserId: string) {
  const response = await apiFetch<void>(apiEndpoints.users.follow(followingUserId), {
    method: "DELETE",
  });
  invalidateFollowCache(followingUserId);
  return response;
}

export function getFollowingByUserId(userId: string) {
  return cachedApiCall(`users:following:${userId}`, apiCacheTtl.dynamic, () =>
    apiFetch<UserFollowResponse[]>(apiEndpoints.users.following(userId), {
      method: "GET",
    }),
  );
}

export function getFollowingTargetsByUserId(userId: string, type = "ALL") {
  return cachedApiCall(
    `users:following-targets:${userId}:${type}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<FollowTargetResponse[]>(apiEndpoints.users.followingTargets(userId, type), {
        method: "GET",
      }),
  );
}

export function getFollowersByUserId(userId: string) {
  return cachedApiCall(`users:followers:${userId}`, apiCacheTtl.dynamic, () =>
    apiFetch<UserFollowResponse[]>(apiEndpoints.users.followers(userId), {
      method: "GET",
    }),
  );
}

function invalidateFollowCache(userId: string) {
  invalidateApiCache(`users:detail:${userId}`);
  invalidateApiCache("users:me");
  invalidateApiCache("users:following:");
  invalidateApiCache("users:following-targets:");
  invalidateApiCache("users:followers:");
  invalidateApiCache("recommendations:");
  invalidateApiCache("feed:");
}
