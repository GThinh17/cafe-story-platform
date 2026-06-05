"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useParams, usePathname } from "next/navigation";
import { ProfileHeader } from "@/components/profile/profile-header";
import { ProfileReviewGrid } from "@/components/profile/profile-review-grid";
import { useBfcacheRestoreEffect } from "@/hooks/use-bfcache-restore";
import { useCurrentUser } from "@/hooks/use-current-user";
import { ApiError } from "@/lib/api/client";
import { followUser, getUserByUsername, unfollowUser } from "@/lib/api/users";
import {
  DEFAULT_AVATAR_IMAGE,
  getUserAvatarImage,
  getUserDisplayName,
  getUserEmail,
  getUserHandle,
  getUserInitials,
} from "@/lib/avatar";
import { mockProfileReviews } from "@/mocks/reviews";
import type { AuthUser } from "@/types/auth";
import type { UserProfile, UserResponse } from "@/types/user";

function mapAuthUserToProfile(user: AuthUser | null): UserProfile {
  const handle = getUserHandle(user);
  const displayName = getUserDisplayName(user);
  const email = getUserEmail(user);

  return {
    avatarImage: getUserAvatarImage(user),
    avatarInitials: getUserInitials(user),
    bio: email ? `Email: ${email}` : "",
    displayName,
    email,
    location: "",
    stats: {
      posts: "0",
      cafes: "0",
      followers: "0",
    },
    username: handle,
    website: `cafestory.vn/${handle}`,
  };
}

function mapUserResponseToProfile(user: UserResponse): UserProfile {
  const location = [
    user.regionStreet,
    user.regionWard,
    user.regionArea,
    user.regionCity,
    user.regionProvince,
  ]
    .filter(Boolean)
    .join(", ");
  const handle = user.userName || "cafestory_user";
  const displayName = user.userFullName?.trim() || handle;

  return {
    avatarImage: getProfileAvatarImage(user),
    avatarInitials: getInitials(displayName),
    bio: user.userEmail ? `Email: ${user.userEmail}` : "",
    displayName,
    email: user.userEmail ?? "",
    location,
    stats: {
      posts: "0",
      cafes: "0",
      followers: String(user.userFollower ?? 0),
    },
    username: handle,
    website: `cafestory.vn/${handle}`,
  };
}

function getProfileAvatarImage(user: UserResponse) {
  return firstNonEmpty([
    user.userAvatar,
    user.avatar,
    user.profileImage,
    user.imageUrl,
  ]) ?? DEFAULT_AVATAR_IMAGE;
}

function firstNonEmpty(values: Array<string | null | undefined>) {
  return values
    .map((value) => value?.trim())
    .find(
      (value) =>
        value &&
        value.toLowerCase() !== "null" &&
        value.toLowerCase() !== "undefined",
    );
}

function getInitials(source: string) {
  const words = source.split(/\s+/).filter(Boolean);

  return words.length > 0
    ? words
        .slice(0, 2)
        .map((word) => word[0])
        .join("")
        .toUpperCase()
    : "CS";
}

function normalizeUsername(username: string | null | undefined) {
  return username?.trim().toLowerCase() ?? "";
}

function isCurrentUserProfile(routeUsername: string, user: AuthUser | null) {
  const normalizedRouteUsername = normalizeUsername(routeUsername);

  return Boolean(
    normalizedRouteUsername &&
      [
        user?.userName,
        user?.userEmail,
      ].some((value) => normalizeUsername(value) === normalizedRouteUsername),
  );
}

function getUsernameFromPathname(pathname: string | null) {
  const firstSegment = pathname?.split("/").filter(Boolean)[0] ?? "";

  return firstSegment ? decodeURIComponent(firstSegment) : "";
}

function getUsernameParamValue(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

type ProfilePageContentProps = {
  username: string;
};

export function ProfilePageContent({ username }: ProfilePageContentProps) {
  const pathname = usePathname();
  const params = useParams<{ username?: string | string[] }>();
  const { user, isLoading } = useCurrentUser();
  const profileRequestIdRef = useRef(0);
  const routeParamUsername = getUsernameParamValue(params.username);
  const routeUsername = useMemo(
    () =>
      routeParamUsername ||
      getUsernameFromPathname(pathname) ||
      decodeURIComponent(username),
    [pathname, routeParamUsername, username],
  );
  const isOwnProfile = isCurrentUserProfile(routeUsername, user);
  const [viewedUser, setViewedUser] = useState<UserResponse | null>(null);
  const [isProfileLoading, setIsProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [followerCount, setFollowerCount] = useState(0);

  const loadProfile = useCallback(async (usernameOverride?: string) => {
    const usernameToFetch = usernameOverride || routeUsername;
    const requestId = profileRequestIdRef.current + 1;
    profileRequestIdRef.current = requestId;

    setViewedUser(null);
    setIsFollowing(false);
    setFollowerCount(0);
    setIsProfileLoading(Boolean(usernameToFetch));
    setProfileError(null);

    if (!usernameToFetch) {
      setIsProfileLoading(false);
      return;
    }

    console.log("[profile] fetch", usernameToFetch);

    try {
      const response = await getUserByUsername(usernameToFetch);

      if (profileRequestIdRef.current !== requestId) {
        return;
      }

      setViewedUser(response);
      setIsFollowing(Boolean(response.isFollowing));
      setFollowerCount(response.userFollower ?? 0);
    } catch (requestError) {
      if (profileRequestIdRef.current !== requestId) {
        return;
      }

      setViewedUser(null);
      setProfileError(
        requestError instanceof ApiError
          ? requestError.message
          : "Unable to load profile.",
      );
    } finally {
      if (profileRequestIdRef.current === requestId) {
        setIsProfileLoading(false);
      }
    }
  }, [routeUsername]);

  useEffect(() => {
    void loadProfile();
  }, [loadProfile]);

  const handleBfcacheRestore = useCallback(() => {
    void loadProfile(routeUsername);
  }, [loadProfile, routeUsername]);

  useBfcacheRestoreEffect(handleBfcacheRestore);

  const profile = useMemo(() => {
    if (viewedUser) {
      return {
        ...mapUserResponseToProfile(viewedUser),
        stats: {
          posts: "0",
          cafes: "0",
          followers: String(followerCount),
        },
      };
    }

    if (isOwnProfile) {
      return mapAuthUserToProfile(user);
    }

    return {
      ...mapAuthUserToProfile(null),
      username: routeUsername,
      website: `cafestory.vn/${routeUsername}`,
    };
  }, [followerCount, isOwnProfile, routeUsername, user, viewedUser]);

  async function handleFollowToggle() {
    if (!viewedUser?.userId) {
      return;
    }

    const wasFollowing = isFollowing;
    const previousFollowerCount = followerCount;
    const nextFollowerCount = Math.max(
      0,
      previousFollowerCount + (wasFollowing ? -1 : 1),
    );

    setIsFollowing(!wasFollowing);
    setFollowerCount(nextFollowerCount);

    try {
      if (wasFollowing) {
        await unfollowUser(viewedUser.userId);
      } else {
        await followUser(viewedUser.userId);
      }
    } catch {
      setIsFollowing(wasFollowing);
      setFollowerCount(previousFollowerCount);
    }
  }

  return (
    <>
      {profileError ? (
        <div className="mb-6 rounded-md border border-border bg-surface-muted px-4 py-3 text-sm font-semibold text-muted">
          {profileError}
        </div>
      ) : null}
      <ProfileHeader
        areActionsLoading={isLoading}
        currentUser={user}
        highlights={[]}
        isFollowing={isFollowing}
        isLoading={isProfileLoading && !viewedUser}
        isOwnProfile={isOwnProfile}
        onFollowToggle={handleFollowToggle}
        profile={profile}
      />
      <ProfileReviewGrid reviews={mockProfileReviews} />
    </>
  );
}
