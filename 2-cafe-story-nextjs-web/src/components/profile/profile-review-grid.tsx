"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  CameraIcon,
  Grid3X3Icon,
  HeartIcon,
  MessageCircleIcon,
  Repeat2Icon,
  UserSquare2Icon,
} from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getBlogLikesByUser, likeBlog, unlikeBlog } from "@/lib/api/blogs";
import type { FeedPost } from "@/types/feed";
import type { ProfileReview } from "@/types/review";

type ProfileReviewGridProps = {
  canCreatePost?: boolean;
  errorMessage?: string | null;
  hasLoadedPosts?: boolean;
  isLoading?: boolean;
  onCreatePostClick?: () => void;
  onRetry?: () => void;
  posts?: FeedPost[];
  reviews?: ProfileReview[];
  sharedPosts?: FeedPost[];
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

export function ProfileReviewGrid({
  canCreatePost = false,
  errorMessage,
  hasLoadedPosts = false,
  isLoading = false,
  onCreatePostClick,
  onRetry,
  posts = [],
  reviews = [],
  sharedPosts = [],
}: ProfileReviewGridProps) {
  const [activeTab, setActiveTab] = useState<"posts" | "shared">("posts");
  const [gridPosts, setGridPosts] = useState(posts);
  const [gridSharedPosts, setGridSharedPosts] = useState(sharedPosts);
  const [selectedPostId, setSelectedPostId] = useState<string | null>(null);
  const { user } = useCurrentUser();
  const activePosts = activeTab === "posts" ? posts : sharedPosts;
  const shouldShowEmptyState =
    hasLoadedPosts && !isLoading && !errorMessage && activePosts.length === 0;
  const selectedPost = useMemo(
    () => gridPosts.find((post) => post.id === selectedPostId) ?? null,
    [gridPosts, selectedPostId],
  );

  const updatePost = useCallback(
    (postId: string, updater: (post: FeedPost) => FeedPost) => {
      setGridPosts((currentPosts) =>
        currentPosts.map((post) => (post.id === postId ? updater(post) : post)),
      );
    },
    [],
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
        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as "posts" | "shared")}>
          <TabsList
            className="relative z-10 grid h-16 w-full grid-cols-3 overflow-visible rounded-none bg-background p-0 text-muted"
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
            <TabsTrigger
              aria-label="Tagged"
              className={tabTriggerClassName}
              value="tagged"
            >
              <UserSquare2Icon className="size-6" strokeWidth={1.75} />
            </TabsTrigger>
          </TabsList>
        </Tabs>

        <div className="relative z-0 mt-3 border-t border-border pt-1">
          {isLoading ? (
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
          ) : shouldShowEmptyState ? (
            <div className="grid min-h-[360px] place-items-center px-6 py-12 text-center">
              <div className="flex max-w-[360px] flex-col items-center">
                <div className="grid size-20 place-items-center rounded-full border-2 border-foreground text-foreground">
                  <CameraIcon className="size-10" strokeWidth={1.8} />
                </div>
                <h2 className="mt-5 text-3xl font-black text-foreground">
                  Share Photos
                </h2>
                <p className="mt-3 text-sm leading-6 text-muted">
                  When you share photos, they will appear on your profile.
                </p>
                {canCreatePost ? (
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
          ) : (activeTab === "posts" ? posts.length > 0 : sharedPosts.length > 0) ? (
            <div className="grid grid-cols-3 gap-1">
              {(activeTab === "posts" ? gridPosts : gridSharedPosts).map((post) => (
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
                    className="h-full w-full object-cover transition duration-200 group-hover:scale-105"
                    decoding="async"
                    loading="lazy"
                    src={post.image}
                  />
                  <div className="absolute inset-0 grid place-items-center bg-black/45 text-white opacity-0 transition group-hover:opacity-100">
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
              ))}
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
        post={selectedPost}
      />
    </>
  );
}
