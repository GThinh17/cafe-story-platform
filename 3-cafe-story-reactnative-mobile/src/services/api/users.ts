import type { UserFollowResponse } from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

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
