import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { UserFollowResponse, UserResponse } from "@/types/user";

type ApiRequestOptions = {
  headers?: HeadersInit;
};

export type UpdateMeRequest = {
  userName?: string;
  userFullName?: string;
  userEmail?: string;
  userPhone?: number;
  userAvatar?: string;
};

export type UpdateMeRegionRequest = {
  city?: string;
  province?: string;
  district?: string;
  ward?: string;
  area?: string;
  street?: string;
};

export function getUserByUsername(
  username: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<UserResponse>(apiEndpoints.users.byUsername(username), {
    headers: options.headers,
    method: "GET",
  });
}

export function updateMe(request: UpdateMeRequest) {
  return apiFetch<UserResponse>(apiEndpoints.users.me, {
    method: "PATCH",
    body: request,
  });
}

export function updateMeRegion(request: UpdateMeRegionRequest) {
  return apiFetch<UserResponse>(apiEndpoints.users.meRegion, {
    method: "PATCH",
    body: request,
  });
}

export function followUser(
  followingUserId: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<UserFollowResponse>(apiEndpoints.users.follow(followingUserId), {
    headers: options.headers,
    method: "POST",
  });
}

export function unfollowUser(
  followingUserId: string,
  options: ApiRequestOptions = {},
) {
  return apiFetch<void>(apiEndpoints.users.follow(followingUserId), {
    headers: options.headers,
    method: "DELETE",
  });
}
