import type { UserFollowResponse, UserResponse } from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getMyProfile() {
  return apiFetch<UserResponse>(apiEndpoints.users.me, {
    method: "GET",
  });
}

export function getUserProfile(userId: string) {
  return apiFetch<UserResponse>(apiEndpoints.users.byId(userId), {
    method: "GET",
  });
}

export function getUserProfileByUsername(username: string) {
  return apiFetch<UserResponse>(apiEndpoints.users.byUsername(username), {
    method: "GET",
  });
}

export function followUser(followingUserId: string) {
  return apiFetch<UserFollowResponse>(apiEndpoints.users.follow(followingUserId), {
    method: "POST",
  });
}

export function unfollowUser(followingUserId: string) {
  return apiFetch<void>(apiEndpoints.users.follow(followingUserId), {
    method: "DELETE",
  });
}

export function getFollowingByUserId(userId: string) {
  return apiFetch<UserFollowResponse[]>(apiEndpoints.users.following(userId), {
    method: "GET",
  });
}
