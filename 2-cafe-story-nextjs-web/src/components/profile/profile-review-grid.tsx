"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  BookmarkIcon,
  CameraIcon,
  EyeOffIcon,
  Grid3X3Icon,
  HeartIcon,
  MessageCircleIcon,
  Repeat2Icon,
} from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCurrentUser } from "@/hooks/use-current-user";
import { cn } from "@/lib/utils";
import {
  getBlogLikesByUser,
  likeBlog,
  saveBlog,
  unlikeBlog,
  unsaveBlog,
} from "@/lib/api/blogs";
import type { FeedPost } from "@/types/feed";
import type { ProfileReview } from "@/types/review";

type ProfileTab = "posts" | "shared" | "saved";

type ProfileReviewGridProps = {
  canCreatePost?: boolean;
  errorMessage?: string | null;
  hasLoadedPosts?: boolean;
  isLoading?: boolean;
  isOwnProfile?: boolean;
  onCreatePostClick?: () => void;
  onRetry?: () => void;
  posts?: FeedPost[];
  reviews?: ProfileReview[];
  sharedPosts?: FeedPost[];
  savedPosts?: FeedPost[];
  isLoadingSaved?: boolean;
};

const tabTriggerClassName =
  "relative z-10 -mb-px h-14 -translate-y-3 rounded-none border-0 border-b-2 border-transparent bg-background p-0 text-muted shadow-none after:hidden data-active:bg-transparent data-active:text-primary data-active:shadow-none data-[state=active]:border-primary data-[state=active]:bg-background data-[state=active]:shadow-none hover:text-none hover:cursor-pointer";

function formatCount(value: number) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}

function getPostLikeCount(post: FeedPost) {
  return typeof post.likeCount === "number" ? formatCount(post.likeCount) : post.likes;
}

function getPostCommentCount(post: FeedPost) {
  if (typeof post.commentCount === "number") {
    return formatCount(post.commentCount);
  }

  return Array.isArray(post.comments)
    ? formatCount(post.comments.length)
    : post.comments;
}

function filterPostsByVisibility(allPosts: FeedPost[], isOwn: boolean): FeedPost[] {
  if (isOwn) {
    return allPosts.filter((p) => p.status !== "REMOVED");
  }
  return allPosts.filter((p) => p.status === "PUBLISHED" || !p.status);
}

export function ProfileReviewGrid({
  canCreatePost = false,
  errorMessage,
  hasLoadedPosts = false,
  isLoading = false,
  isOwnProfile = false,
  onCreatePostClick,
  onRetry,
  posts = [],
  reviews = [],
  sharedPosts = [],
  savedPosts = [],
  isLoadingSaved = false,
}: ProfileReviewGridProps) {
  const [activeTab, setActiveTab] = useState<ProfileTab>("posts");
  const [gridPosts, setGridPosts] = useState(posts);
  const [gridSharedPosts, setGridSharedPosts] = useState(sharedPosts);
  const [gridSavedPosts, setGridSavedPosts] = useState(savedPosts);
  const [selectedPostId, setSelectedPostId] = useState<string | null>(null);
  const { user } = useCurrentUser();
  const visiblePosts = useMemo(() => filterPostsByVisibility(posts, isOwnProfile), [posts, isOwnProfile]);
  const visibleSharedPosts = useMemo(() => filterPostsByVisibility(sharedPosts, isOwnProfile), [sharedPosts, isOwnProfile]);
  const activePosts =
    activeTab === "posts"
      ? visiblePosts
      : activeTab === "shared"
        ? visibleSharedPosts
        : activeTab === "saved"
          ? gridSavedPosts
          : [];
  const shouldShowEmptyState =
    activeTab === "saved"
      ? !isLoadingSaved && !errorMessage && gridSavedPosts.length === 0
      : hasLoadedPosts && !isLoading && !errorMessage && activePosts.length === 0;
  const selectedPost = useMemo(() => {
    const pool =
      activeTab === "saved"
        ? gridSavedPosts
        : activeTab === "shared"
          ? gridSharedPosts
          : gridPosts;
    return pool.find((post) => post.id === selectedPostId) ?? null;
  }, [activeTab, gridPosts, gridSavedPosts, gridSharedPosts, selectedPostId]);

  const updatePost = useCallback(
    (postId: string, updater: (post: FeedPost) => FeedPost) => {
      setGridPosts((currentPosts) =>
        currentPosts.map((post) => (post.id === postId ? updater(post) : post)),
      );
      setGridSharedPosts((currentPosts) =>
        currentPosts.map((post) => (post.id === postId ? updater(post) : post)),
      );
      setGridSavedPosts((currentPosts) =>
        currentPosts.map((post) => (post.id === postId ? updater(post) : post)),
      );
    },
    [],
  );

  const handleSaveClick = useCallback(
    async (post: FeedPost) => {
      if (!post.id) return;
      const wasSaved = Boolean(post.isSaved);

      updatePost(post.id, (currentPost) => ({
        ...currentPost,
        isSaved: !wasSaved,
      }));

      // Remove from saved tab when unsaving from own profile
      if (wasSaved && isOwnProfile) {
        setGridSavedPosts((current) => current.filter((p) => p.id !== post.id));
      }

      try {
        if (wasSaved) {
          await unsaveBlog(post.id);
        } else {
          await saveBlog(post.id);
        }
      } catch {
        updatePost(post.id, (currentPost) => ({
          ...currentPost,
          isSaved: wasSaved,
        }));
        if (wasSaved && isOwnProfile) {
          setGridSavedPosts((current) =>
            current.some((p) => p.id === post.id) ? current : [...current, post],
          );
        }
      }
    },
    [isOwnProfile, updatePost],
  );

  const syncPostCounts = useCallback(
    (postId: string, patch: Pick<FeedPost, "commentCount" | "comments">) => {
      updatePost(postId, (post) => ({
        ...post,
        ...patch,
      }));
    },
    [updatePost],
  );

  const handleLikeClick = useCallback(
    async (post: FeedPost) => {
      if (!post.id) {
        return;
      }

      const wasLiked = Boolean(post.isLiked);
      const previousLikeCount =
        typeof post.likeCount === "number" ? post.likeCount : 0;
      const nextLikeCount = Math.max(0, previousLikeCount + (wasLiked ? -1 : 1));

      updatePost(post.id, (currentPost) => ({
        ...currentPost,
        isLiked: !wasLiked,
        likeCount: nextLikeCount,
        likes: formatCount(nextLikeCount),
      }));

      try {
        if (wasLiked) {
          await unlikeBlog(post.id);
        } else {
          await likeBlog(post.id);
        }
      } catch {
        updatePost(post.id, (currentPost) => ({
          ...currentPost,
          isLiked: wasLiked,
          likeCount: previousLikeCount,
          likes: formatCount(previousLikeCount),
        }));
      }
    },
    [updatePost],
  );

  useEffect(() => {
    const seen = new Set<string>();
    setGridPosts(posts.filter((p) => (p.id ? !seen.has(p.id) && seen.add(p.id) : true)));
  }, [posts]);

  useEffect(() => {
    const seen = new Set<string>();
    setGridSharedPosts(sharedPosts.filter((p) => (p.id ? !seen.has(p.id) && seen.add(p.id) : true)));
  }, [sharedPosts]);

  useEffect(() => {
    const seen = new Set<string>();
    setGridSavedPosts(
      savedPosts
        .filter((p) => (p.id ? !seen.has(p.id) && seen.add(p.id) : true))
        .map((p) => ({ ...p, isSaved: true })),
    );
  }, [savedPosts]);

  useEffect(() => {
    if (!user?.userId || posts.length === 0) {
      return;
    }

    let isActive = true;
    const userId = user.userId;

    async function syncLikedPosts() {
      try {
        const likes = await getBlogLikesByUser(userId);
        const likedPostIds = new Set(likes.map((like) => like.blogId));

        if (!isActive) {
          return;
        }

        setGridPosts((currentPosts) =>
          currentPosts.map((post) => ({
            ...post,
            isLiked: post.id ? likedPostIds.has(post.id) : false,
          })),
        );
      } catch {
        if (!isActive) {
          return;
        }

        setGridPosts((currentPosts) =>
          currentPosts.map((post) => ({
            ...post,
            isLiked: post.isLiked ?? false,
          })),
        );
      }
    }

    void syncLikedPosts();

    return () => {
      isActive = false;
    };
  }, [posts.length, user?.userId]);

  return (
    <>
      <section className="flex flex-col">
        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as ProfileTab)}>
          <TabsList
            className={cn(
              "relative z-10 grid h-16 w-full overflow-visible rounded-none bg-background p-0 text-muted",
              isOwnProfile ? "grid-cols-3" : "grid-cols-2",
            )}
            variant="line"
          >
            <TabsTrigger
              aria-label="Posts"
              className={tabTriggerClassName}
              value="posts"
            >
              <Grid3X3Icon className="size-6" strokeWidth={1.75} />
            </TabsTrigger>
            <TabsTrigger
              aria-label="Shared"
              className={tabTriggerClassName}
              value="shared"
            >
              <Repeat2Icon className="size-6" strokeWidth={1.75} />
            </TabsTrigger>
            {isOwnProfile ? (
              <TabsTrigger
                aria-label="Saved"
                className={tabTriggerClassName}
                value="saved"
              >
                <BookmarkIcon className="size-6" strokeWidth={1.75} />
              </TabsTrigger>
            ) : null}
          </TabsList>
        </Tabs>

        <div className="relative z-0 mt-3 border-t border-border pt-1">
          {(activeTab === "saved" ? isLoadingSaved : isLoading) ? (
            <div className="grid grid-cols-3 gap-1">
              {Array.from({ length: 6 }, (_, index) => (
                <Skeleton className="aspect-square rounded-none" key={index} />
              ))}
            </div>
          ) : errorMessage ? (
            <Alert className="mx-auto mt-5 w-[85%] max-w-full">
              <AlertTitle>Posts unavailable</AlertTitle>
              <AlertDescription>{errorMessage}</AlertDescription>
              {onRetry ? (
                <Button
                  className="mt-3 h-auto p-0 text-sm font-black text-primary hover:bg-transparent"
                  onClick={onRetry}
                  type="button"
                  variant="ghost"
                >
                  Retry
                </Button>
              ) : null}
            </Alert>
          ) : shouldShowEmptyState && activeTab === "saved" ? (
            <div className="grid min-h-[360px] place-items-center px-6 py-12 text-center">
              <div className="flex max-w-[360px] flex-col items-center">
                <div className="grid size-20 place-items-center rounded-full border-2 border-foreground text-foreground">
                  <BookmarkIcon className="size-10" strokeWidth={1.8} />
                </div>
                <h2 className="mt-5 text-3xl font-black text-foreground">
                  No saved posts yet
                </h2>
                <p className="mt-3 text-sm leading-6 text-muted">
                  Tap the bookmark icon on any post to save it here. Only you can see saved posts.
                </p>
              </div>
            </div>
          ) : shouldShowEmptyState && activeTab === "shared" ? (
            <div className="grid min-h-[360px] place-items-center px-6 py-12 text-center">
              <div className="flex max-w-[360px] flex-col items-center">
                <div className="grid size-20 place-items-center rounded-full border-2 border-foreground text-foreground">
                  <CameraIcon className="size-10" strokeWidth={1.8} />
                </div>
                <h2 className="mt-5 text-3xl font-black text-foreground">
                  {isOwnProfile ? "No shared posts yet" : "Nothing shared yet"}
                </h2>
                <p className="mt-3 text-sm leading-6 text-muted">
                  {isOwnProfile
                    ? "Posts you share will show up here."
                    : "When this user shares posts, they will appear here."}
                </p>
              </div>
            </div>
          ) : shouldShowEmptyState ? (
            <div className="grid min-h-[360px] place-items-center px-6 py-12 text-center">
              <div className="flex max-w-[360px] flex-col items-center">
                <div className="grid size-20 place-items-center rounded-full border-2 border-foreground text-foreground">
                  <CameraIcon className="size-10" strokeWidth={1.8} />
                </div>
                <h2 className="mt-5 text-3xl font-black text-foreground">
                  {isOwnProfile ? "Share Photos" : "No posts yet"}
                </h2>
                <p className="mt-3 text-sm leading-6 text-muted">
                  {isOwnProfile
                    ? "When you share photos, they will appear on your profile."
                    : "This user hasn't shared any posts yet."}
                </p>
                {isOwnProfile && canCreatePost ? (
                  <Button
                    className="mt-5 h-auto p-0 text-sm font-black text-primary hover:bg-transparent hover:text-primary-strong"
                    onClick={onCreatePostClick}
                    type="button"
                    variant="ghost"
                  >
                    Share your first photo
                  </Button>
                ) : null}
              </div>
            </div>
          ) : activePosts.length > 0 ? (
            <div className="grid grid-cols-3 gap-1">
              {(activeTab === "posts"
                ? gridPosts.filter((p) => filterPostsByVisibility([p], isOwnProfile).length > 0)
                : activeTab === "shared"
                  ? gridSharedPosts.filter((p) => filterPostsByVisibility([p], isOwnProfile).length > 0)
                  : activeTab === "saved"
                    ? gridSavedPosts
                    : []
              ).map((post) => {
                const isHidden = post.status === "HIDDEN";

                return (
                  <button
                    aria-label={`Open ${post.cafe} post`}
                    className="group relative aspect-square overflow-hidden bg-surface-muted text-left"
                    key={post.id ?? post.image}
                    onClick={() => {
                      if (post.id) {
                        setSelectedPostId(post.id);
                      }
                    }}
                    type="button"
                  >
                    <img
                      alt={`${post.cafe} post`}
                      className={`h-full w-full object-cover transition duration-200 group-hover:scale-105 ${isHidden ? "opacity-40" : ""}`}
                      decoding="async"
                      loading="lazy"
                      src={post.image}
                    />
                    {isHidden && (
                      <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/30 text-white pointer-events-none">
                        <EyeOffIcon className="size-6 mb-1" strokeWidth={1.8} />
                        <span className="text-xs font-semibold">Đã ẩn</span>
                      </div>
                    )}
                    <div className={`absolute inset-0 grid place-items-center bg-black/45 text-white opacity-0 transition group-hover:opacity-100 ${isHidden ? "bg-black/55" : ""}`}>
                      <div className="flex items-center gap-5 text-sm font-black">
                        <span className="inline-flex items-center gap-1.5">
                          <HeartIcon
                            className={post.isLiked ? "size-5 fill-white" : "size-5"}
                            strokeWidth={2.4}
                          />
                          {getPostLikeCount(post)}
                        </span>
                        <span className="inline-flex items-center gap-1.5">
                          <MessageCircleIcon className="size-5" strokeWidth={2.4} />
                          {getPostCommentCount(post)}
                        </span>
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>
          ) : reviews.length > 0 ? (
            <div className="grid grid-cols-3 gap-1">
              {reviews.map((review) => (
                <article
                  className="group relative aspect-square overflow-hidden bg-surface-muted"
                  key={review.id}
                >
                  <img
                    alt={`${review.cafe} review`}
                    className="h-full w-full object-cover transition duration-200 group-hover:scale-105"
                    decoding="async"
                    loading="lazy"
                    src={review.image}
                  />
                  <div className="absolute inset-0 flex items-end bg-gradient-to-t from-black/55 via-black/0 to-transparent p-2 opacity-0 transition group-hover:opacity-100">
                    <div className="min-w-0 text-white">
                      <p className="truncate text-xs font-black">{review.cafe}</p>
                      <p className="text-xs">{review.rating}</p>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          ) : null}
        </div>
      </section>

      <PostCommentsModal
        currentUser={user}
        onCommentCountChange={syncPostCounts}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedPostId(null);
          }
        }}
        onPostLikeClick={handleLikeClick}
        onPostSaveClick={handleSaveClick}
        post={selectedPost}
      />
    </>
  );
}
