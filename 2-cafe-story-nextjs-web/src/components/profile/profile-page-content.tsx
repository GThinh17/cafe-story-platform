"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useParams, usePathname, useRouter } from "next/navigation";
import { ProfileCafeSection } from "@/components/profile/profile-cafe-section";
import { ProfileHeader } from "@/components/profile/profile-header";
import { ProfileReviewGrid } from "@/components/profile/profile-review-grid";
import { ProfileUserListModal } from "@/components/profile/profile-user-list-modal";
import { CreatePostModal } from "@/components/review/create-post-modal";
import {
  mapBlogResponsesToFeedPosts,
  mapSharedBlogResponsesToFeedPosts,
} from "@/features/blogs/blog-feed-adapter";
import { useBfcacheRestoreEffect } from "@/hooks/use-bfcache-restore";
import { useCurrentUser } from "@/hooks/use-current-user";
import {
  getBlogsByUser,
  getSavedBlogsByUserId,
  getSharedBlogsByUser,
} from "@/lib/api/blogs";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { createDirectConversation } from "@/lib/api/chat";
import { ApiError } from "@/lib/api/client";
import { getReviewerByUserId } from "@/lib/api/reviewers";
import { getUserByUsername } from "@/lib/api/users";
import {
  DEFAULT_AVATAR_IMAGE,
  getUserAvatarImage,
  getUserDisplayName,
  getUserEmail,
  getUserHandle,
  getUserInitials,
} from "@/lib/avatar";
import { mockReviewComposer, mockReviewDraftHints } from "@/mocks/reviews";
import type { AuthUser } from "@/types/auth";
import { isCurrentUserProfile } from "@/lib/profile/is-current-user-profile";
import type { CafePageResponse } from "@/types/cafe";
import type { FeedPost } from "@/types/feed";
import type { UserProfile, UserResponse } from "@/types/user";

function mapAuthUserToProfile(user: AuthUser | null): UserProfile {
  const handle = getUserHandle(user);
  const displayName = getUserDisplayName(user);
  const email = getUserEmail(user);

  return {
    avatarImage: getUserAvatarImage(user),
    avatarInitials: getUserInitials(user),
    bio: "",
    displayName,
    email,
    location: "",
    stats: {
      posts: "0",
      following: String(user?.followingCount ?? 0),
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
    bio: user.userDescription?.trim() ?? "",
    displayName,
    email: user.userEmail ?? "",
    location,
    stats: {
      posts: "0",
      following: String(user.followingCount ?? 0),
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

type ProfileUserListModalType = "followers" | "following";

function isActiveCafePage(cafe: CafePageResponse) {
  return cafe.pageActive === true || cafe.status === "ACTIVE";
}

export function ProfilePageContent({ username }: ProfilePageContentProps) {
  const router = useRouter();
  const pathname = usePathname();
  const params = useParams<{ username?: string | string[] }>();
  const { user, isLoading } = useCurrentUser();
  const profileRequestIdRef = useRef(0);
  const postsRequestIdRef = useRef(0);
  const cafesRequestIdRef = useRef(0);
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
  const [reviewerBadge, setReviewerBadge] = useState<string | null>(null);
  const [isProfileLoading, setIsProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [isMessageLoading, setIsMessageLoading] = useState(false);
  const [followerCount, setFollowerCount] = useState(0);
  const [ownPosts, setOwnPosts] = useState<FeedPost[]>([]);
  const [sharedPosts, setSharedPosts] = useState<FeedPost[]>([]);
  const [savedPosts, setSavedPosts] = useState<FeedPost[]>([]);
  const [isSavedLoading, setIsSavedLoading] = useState(false);
  const [isPostsLoading, setIsPostsLoading] = useState(false);
  const [postsError, setPostsError] = useState<string | null>(null);
  const [hasLoadedPosts, setHasLoadedPosts] = useState(false);
  const [isCreatePostOpen, setIsCreatePostOpen] = useState(false);
  const [activeUserListModal, setActiveUserListModal] =
    useState<ProfileUserListModalType | null>(null);
  const [activeCafePages, setActiveCafePages] = useState<CafePageResponse[]>([]);

  const loadProfile = useCallback(async (usernameOverride?: string) => {
    const usernameToFetch = usernameOverride || routeUsername;
    const requestId = profileRequestIdRef.current + 1;
    profileRequestIdRef.current = requestId;

    setViewedUser(null);
    setReviewerBadge(null);
    setIsFollowing(false);
    setFollowerCount(0);
    setOwnPosts([]);
    setSharedPosts([]);
    setIsPostsLoading(false);
    setPostsError(null);
    setHasLoadedPosts(false);
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

      getReviewerByUserId(response.userId)
        .then((reviewer) => {
          if (profileRequestIdRef.current === requestId) {
            setReviewerBadge(reviewer.badge ?? null);
          }
        })
        .catch(() => {
          if (profileRequestIdRef.current === requestId) {
            setReviewerBadge(null);
          }
        });
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

  const loadProfilePosts = useCallback(async (userId: string) => {
    const requestId = postsRequestIdRef.current + 1;
    postsRequestIdRef.current = requestId;

    setIsPostsLoading(true);
    setPostsError(null);
    setHasLoadedPosts(false);

    try {
      const [ownBlogsRes, sharedBlogsRes] = await Promise.allSettled([
        getBlogsByUser(userId, "PUBLISHED"),
        getSharedBlogsByUser(userId),
      ]);

      if (postsRequestIdRef.current !== requestId) {
        return;
      }

      if (ownBlogsRes.status === "rejected") {
        throw ownBlogsRes.reason;
      }

      const nextOwnPosts = mapBlogResponsesToFeedPosts(
        ownBlogsRes.value.filter((b) => !b.pageId),
      );
      const nextSharedPosts =
        sharedBlogsRes.status === "fulfilled"
          ? mapSharedBlogResponsesToFeedPosts(sharedBlogsRes.value)
          : [];

      setOwnPosts(nextOwnPosts);
      setSharedPosts(nextSharedPosts);
      setHasLoadedPosts(true);
    } catch (requestError) {
      if (postsRequestIdRef.current !== requestId) {
        return;
      }

      setOwnPosts([]);
      setSharedPosts([]);
      setPostsError(
        requestError instanceof ApiError
          ? requestError.message
          : "Unable to load profile posts.",
      );
      setHasLoadedPosts(false);
    } finally {
      if (postsRequestIdRef.current === requestId) {
        setIsPostsLoading(false);
      }
    }
  }, []);

  const loadProfileCafePages = useCallback(async (userId: string) => {
    const requestId = cafesRequestIdRef.current + 1;
    cafesRequestIdRef.current = requestId;

    try {
      const cafes = await getCafePagesByOwnerId(userId);

      if (cafesRequestIdRef.current !== requestId) {
        return;
      }

      setActiveCafePages(cafes.filter(isActiveCafePage));
    } catch {
      if (cafesRequestIdRef.current === requestId) {
        setActiveCafePages([]);
      }
    }
  }, []);

  useEffect(() => {
    void loadProfile();
  }, [loadProfile]);

  const hideCafeSection = viewedUser?.hideCafePageOnProfile === true;

  useEffect(() => {
    if (!viewedUser?.userId) {
      setOwnPosts([]);
      setSharedPosts([]);
      setActiveCafePages([]);
      setIsPostsLoading(false);
      setPostsError(null);
      setHasLoadedPosts(false);
      return;
    }

    void loadProfilePosts(viewedUser.userId);

    if (hideCafeSection) {
      setActiveCafePages([]);
      return;
    }

    void loadProfileCafePages(viewedUser.userId);
  }, [hideCafeSection, loadProfileCafePages, loadProfilePosts, viewedUser?.userId]);

  useEffect(() => {
    if (!isOwnProfile || !user?.userId) {
      setSavedPosts([]);
      setIsSavedLoading(false);
      return;
    }

    let isActive = true;
    const viewerId = user.userId;
    setIsSavedLoading(true);

    getSavedBlogsByUserId(viewerId)
      .then((blogs) => {
        if (!isActive) return;
        setSavedPosts(mapBlogResponsesToFeedPosts(blogs));
      })
      .catch(() => {
        if (!isActive) return;
        setSavedPosts([]);
      })
      .finally(() => {
        if (isActive) setIsSavedLoading(false);
      });

    return () => {
      isActive = false;
    };
  }, [isOwnProfile, user?.userId]);

  const handleBfcacheRestore = useCallback(() => {
    void loadProfile(routeUsername);
  }, [loadProfile, routeUsername]);

  useBfcacheRestoreEffect(handleBfcacheRestore);

  const visiblePostCount = useMemo(() => {
    if (isOwnProfile) {
      return ownPosts.filter((p) => p.status !== "REMOVED").length;
    }
    return ownPosts.filter((p) => p.status === "PUBLISHED" || !p.status).length;
  }, [ownPosts, isOwnProfile]);

  const profile = useMemo(() => {
    if (viewedUser) {
      return {
        ...mapUserResponseToProfile(viewedUser),
        badge: reviewerBadge,
        stats: {
          posts: String(visiblePostCount),
          following: String(viewedUser.followingCount ?? 0),
          followers: String(followerCount),
        },
      };
    }

    if (isOwnProfile) {
      return { ...mapAuthUserToProfile(user), badge: reviewerBadge };
    }

    return {
      ...mapAuthUserToProfile(null),
      username: routeUsername,
      website: `cafestory.vn/${routeUsername}`,
    };
  }, [
    followerCount,
    activeCafePages.length,
    isOwnProfile,
    reviewerBadge,
    visiblePostCount,
    routeUsername,
    user,
    viewedUser,
  ]);

  const primaryCafePage = activeCafePages[0] ?? null;
  const cafePageHref = primaryCafePage ? `/cafes/${primaryCafePage.id}` : undefined;
  const profileUserId = viewedUser?.userId ?? (isOwnProfile ? user?.userId : undefined);

  function handleFollowToggle(nextIsFollowing: boolean) {
    setIsFollowing(nextIsFollowing);
    setFollowerCount((prev) => Math.max(0, prev + (nextIsFollowing ? 1 : -1)));
  }

  async function handleMessageClick() {
    if (!viewedUser?.userId || isMessageLoading) {
      return;
    }

    setIsMessageLoading(true);

    try {
      const conversation = await createDirectConversation(viewedUser.userId);

      router.push(`/messages?conversationId=${conversation.id}`);
    } catch (requestError) {
      setProfileError(
        requestError instanceof ApiError
          ? requestError.message
          : "Unable to open conversation.",
      );
    } finally {
      setIsMessageLoading(false);
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
        cafePageHref={isOwnProfile ? cafePageHref : undefined}
        highlights={[]}
        isFollowing={isFollowing}
        isLoading={isProfileLoading && !viewedUser}
        isMessageLoading={isMessageLoading}
        isOwnProfile={isOwnProfile}
        profileUserId={profileUserId}
        onFollowToggle={handleFollowToggle}
        onFollowersClick={
          profileUserId ? () => setActiveUserListModal("followers") : undefined
        }
        onFollowingClick={
          profileUserId ? () => setActiveUserListModal("following") : undefined
        }
        onMessageClick={handleMessageClick}
        profile={profile}
        routeUsername={routeUsername}
      />
      {profileUserId && activeUserListModal ? (
        <ProfileUserListModal
          onOpenChange={(open) => {
            if (!open) {
              setActiveUserListModal(null);
            }
          }}
          open={Boolean(activeUserListModal)}
          profileUserId={profileUserId}
          type={activeUserListModal}
        />
      ) : null}
      {hideCafeSection ? null : (
        <ProfileCafeSection
          cafe={primaryCafePage}
          currentUserId={isOwnProfile ? user?.userId : undefined}
          isOwnProfile={isOwnProfile}
        />
      )}

      <ProfileReviewGrid
        canCreatePost={isOwnProfile}
        errorMessage={postsError}
        hasLoadedPosts={hasLoadedPosts}
        isLoading={(isProfileLoading && !viewedUser) || isPostsLoading}
        isOwnProfile={isOwnProfile}
        onCreatePostClick={() => setIsCreatePostOpen(true)}
        onRetry={() => {
          if (viewedUser?.userId) {
            void loadProfilePosts(viewedUser.userId);
          }
        }}
        posts={ownPosts}
        sharedPosts={sharedPosts}
        savedPosts={savedPosts}
        isLoadingSaved={isSavedLoading}
      />
      <CreatePostModal
        composer={mockReviewComposer}
        hints={mockReviewDraftHints}
        isOpen={isCreatePostOpen}
        ownedCafePage={isOwnProfile ? primaryCafePage : null}
        onCreated={() => {
          if (viewedUser?.userId) {
            void loadProfilePosts(viewedUser.userId);
          }
        }}
        onClose={() => {
          setIsCreatePostOpen(false);
        }}
      />
    </>
  );
}
