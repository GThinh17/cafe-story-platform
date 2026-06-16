import type {
  UserFollowResponse,
  UserRegionUpdateRequest,
  UserResponse,
  UserUpdateRequest,
} from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export type AvatarUploadFile = {
  name: string;
  type: string;
  uri: string;
};

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

export function updateMyProfile(request: UserUpdateRequest) {
  return apiFetch<UserResponse>(apiEndpoints.users.me, {
    body: request,
    method: "PATCH",
  });
}

export function uploadMyAvatar(file: AvatarUploadFile) {
  const formData = new FormData();
  formData.append("avatar", file as unknown as Blob);

  return apiFetch<UserResponse>(apiEndpoints.users.meAvatar, {
    body: formData,
    method: "PATCH",
  });
}

export function updateMyRegion(request: UserRegionUpdateRequest) {
  return apiFetch<UserResponse>(apiEndpoints.users.meRegion, {
    body: request,
    method: "PATCH",
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

export function getFollowersByUserId(userId: string) {
  return apiFetch<UserFollowResponse[]>(apiEndpoints.users.followers(userId), {
    method: "GET",
  });
}
