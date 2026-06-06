"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { PostCard } from "@/components/feed/post-card";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import { mapBlogFeedToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import {
  getBlogFeed,
  getBlogLikesByUser,
  likeBlog,
  unlikeBlog,
} from "@/lib/api/blogs";
import { useCurrentUser } from "@/hooks/use-current-user";
import type { TrendWindowType } from "@/types/blog";
import type { FeedPost } from "@/types/feed";

type FeedPostListProps = {
  errorMessage?: string;
  initialPage?: number;
  pageSize?: number;
  posts: FeedPost[];
  windowType?: TrendWindowType;
};

export function FeedPostList({
  errorMessage,
  initialPage = 0,
  pageSize = 20,
  posts,
  windowType = "HOUR_24",
}: FeedPostListProps) {
  const [feedPosts, setFeedPosts] = useState(posts);
  const [nextPage, setNextPage] = useState(initialPage + 1);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(posts.length >= pageSize);
  const [loadMoreError, setLoadMoreError] = useState<string | null>(null);
  const [likedPostIds, setLikedPostIds] = useState<Set<string> | null>(null);
  const isLoadingMoreRef = useRef(false);
  const loadMoreSentinelRef = useRef<HTMLDivElement | null>(null);
  const [selectedPostId, setSelectedPostId] = useState<string | null>(null);
  const { user } = useCurrentUser();

  const selectedPost = useMemo(
    () => feedPosts.find((post) => post.id === selectedPostId) ?? null,
    [feedPosts, selectedPostId],
  );

  const updatePost = useCallback(
    (postId: string, updater: (post: FeedPost) => FeedPost) => {
      setFeedPosts((currentPosts) =>
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

  const applyLikedState = useCallback(
    (nextPosts: FeedPost[]) => {
      if (!likedPostIds) {
        return nextPosts;
      }

      return nextPosts.map((post) => ({
        ...post,
        isLiked: post.id ? likedPostIds.has(post.id) : false,
      }));
    },
    [likedPostIds],
  );

  const loadMorePosts = useCallback(async () => {
    if (!hasMore || isLoadingMoreRef.current) {
      return;
    }

    isLoadingMoreRef.current = true;
    setIsLoadingMore(true);
    setLoadMoreError(null);

    try {
      const response = await getBlogFeed({
        page: nextPage,
        size: pageSize,
        windowType,
      });
      const nextPosts = applyLikedState(mapBlogFeedToFeedPosts(response));

      setFeedPosts((currentPosts) => [...currentPosts, ...nextPosts]);
      setNextPage((currentPage) => currentPage + 1);
      setHasMore(response.length >= pageSize);
    } catch {
      setLoadMoreError("Unable to load more posts.");
    } finally {
      isLoadingMoreRef.current = false;
      setIsLoadingMore(false);
    }
  }, [applyLikedState, hasMore, nextPage, pageSize, windowType]);

  useEffect(() => {
    setFeedPosts(posts);
    setNextPage(initialPage + 1);
    setHasMore(posts.length >= pageSize);
    setLoadMoreError(null);
    isLoadingMoreRef.current = false;
    setIsLoadingMore(false);
  }, [initialPage, pageSize, posts]);

  useEffect(() => {
    if (!user?.userId) {
      return;
    }

    let isActive = true;
    const userId = user.userId;

    async function syncLikedPosts() {
      try {
        const likes = await getBlogLikesByUser(userId);
        const nextLikedPostIds = new Set(likes.map((like) => like.blogId));

        if (!isActive) {
          return;
        }

        setLikedPostIds(nextLikedPostIds);
        setFeedPosts((currentPosts) =>
          currentPosts.map((post) => ({
            ...post,
            isLiked: post.id ? nextLikedPostIds.has(post.id) : false,
          })),
        );
      } catch {
        if (!isActive) {
          return;
        }

        setFeedPosts((currentPosts) =>
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
  }, [user?.userId]);

  useEffect(() => {
    if (!user?.userId) {
      setLikedPostIds(null);
    }
  }, [user?.userId]);

  useEffect(() => {
    const sentinel = loadMoreSentinelRef.current;

    if (!sentinel || errorMessage || !hasMore) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          void loadMorePosts();
        }
      },
      {
        rootMargin: "480px 0px",
        threshold: 0,
      },
    );

    observer.observe(sentinel);

    return () => {
      observer.disconnect();
    };
  }, [errorMessage, hasMore, loadMorePosts]);

  if (errorMessage) {
    return (
      <Alert>
        <AlertTitle>Feed unavailable</AlertTitle>
        <AlertDescription>{errorMessage}</AlertDescription>
      </Alert>
    );
  }

  if (posts.length === 0) {
    return (
      <Alert>
        <AlertTitle>No posts yet</AlertTitle>
        <AlertDescription>
          Your personalized Cafe Story feed will appear here once there are posts
          to recommend.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <>
      <div className="flex flex-col gap-6">
        {feedPosts.map((post) => (
          <PostCard
            key={post.id ?? post.cafe}
            onCommentClick={(selectedPost) => {
              if (selectedPost.id) {
                setSelectedPostId(selectedPost.id);
              }
            }}
            onLikeClick={handleLikeClick}
            post={post}
          />
        ))}

        {hasMore ? (
          <div
            aria-hidden="true"
            className="h-8"
            ref={loadMoreSentinelRef}
          />
        ) : null}
      </div>

      {isLoadingMore ? (
        <p className="py-2 text-center text-sm font-semibold text-muted">
          Loading more posts...
        </p>
      ) : null}

      {loadMoreError ? (
        <Alert>
          <AlertTitle>Unable to load more posts</AlertTitle>
          <AlertDescription>{loadMoreError}</AlertDescription>
        </Alert>
      ) : null}

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

function formatCount(value: number) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}
