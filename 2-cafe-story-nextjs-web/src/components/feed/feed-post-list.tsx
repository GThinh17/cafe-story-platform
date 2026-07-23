"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogTitle,
} from "@/components/ui/dialog";
import { PostCard } from "@/components/feed/post-card";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import { ReportPostModal } from "@/components/feed/report-post-modal";
import { SponsoredCafeCard } from "@/components/feed/sponsored-cafe-card";
import { mapMixedFeedToRenderableItems } from "@/features/blogs/blog-feed-adapter";
import {
  likeBlog,
  saveBlog,
  shareBlog,
  unlikeBlog,
  unsaveBlog,
  unshareBlog,
} from "@/lib/api/blogs";
import { getMixedFeed } from "@/lib/api/feed";
import { useCurrentUser } from "@/hooks/use-current-user";
import type { FeedPost, FeedRenderableItem } from "@/types/feed";

type FeedPostListProps = {
  errorMessage?: string;
  initialHasMore?: boolean;
  initialNextCursor?: string | null;
  initialPage?: number;
  pageSize?: number;
  items: FeedRenderableItem[];
};

export function FeedPostList({
  errorMessage,
  initialHasMore,
  initialNextCursor = null,
  initialPage = 0,
  pageSize = 20,
  items,
}: FeedPostListProps) {
  const [feedItems, setFeedItems] = useState(items);
  const [nextCursor, setNextCursor] = useState<string | null>(initialNextCursor);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(
    initialHasMore ?? items.length >= pageSize,
  );
  const [loadMoreError, setLoadMoreError] = useState<string | null>(null);
  const isLoadingMoreRef = useRef(false);
  const loadMoreSentinelRef = useRef<HTMLDivElement | null>(null);
  const [selectedPostId, setSelectedPostId] = useState<string | null>(null);
  const [shareModalPost, setShareModalPost] = useState<FeedPost | null>(null);
  const [unshareConfirmPost, setUnshareConfirmPost] = useState<FeedPost | null>(null);
  const [isSharePending, setIsSharePending] = useState(false);
  const [reportingPost, setReportingPost] = useState<FeedPost | null>(null);
  const { user } = useCurrentUser();

  const selectedPost = useMemo(
    () => feedItems.find(
      (item): item is Extract<FeedRenderableItem, { kind: "post" }> =>
        item.kind === "post" && item.post.id === selectedPostId,
    )?.post ?? null,
    [feedItems, selectedPostId],
  );

  const updatePost = useCallback(
    (postId: string, updater: (post: FeedPost) => FeedPost) => {
      setFeedItems((currentItems) =>
        currentItems.map((item) =>
          item.kind === "post" && item.post.id === postId
            ? { ...item, post: updater(item.post) }
            : item,
        ),
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

  const handleSaveClick = useCallback(
    async (post: FeedPost) => {
      if (!post.id) return;
      const wasSaved = Boolean(post.isSaved);

      updatePost(post.id, (currentPost) => ({
        ...currentPost,
        isSaved: !wasSaved,
      }));

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
      }
    },
    [updatePost],
  );

  const loadMorePosts = useCallback(async () => {
    if (!hasMore || isLoadingMoreRef.current) {
      return;
    }

    isLoadingMoreRef.current = true;
    setIsLoadingMore(true);
    setLoadMoreError(null);

    try {
      const response = await getMixedFeed({
        cursor: nextCursor,
        size: pageSize,
      });
      const nextItems = mapMixedFeedToRenderableItems(response);

      setFeedItems((currentItems) => [...currentItems, ...nextItems]);
      setNextCursor(response.nextCursor ?? null);
      setHasMore(Boolean(response.hasMore && response.nextCursor));
    } catch {
      setLoadMoreError("Unable to load more posts.");
    } finally {
      isLoadingMoreRef.current = false;
      setIsLoadingMore(false);
    }
  }, [hasMore, nextCursor, pageSize]);

  useEffect(() => {
    setFeedItems(items);
    setNextCursor(initialNextCursor);
    setHasMore(initialHasMore ?? items.length >= pageSize);
    setLoadMoreError(null);
    isLoadingMoreRef.current = false;
    setIsLoadingMore(false);
  }, [initialHasMore, initialNextCursor, initialPage, items, pageSize]);

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

  function handleShareClick(post: FeedPost) {
    if (post.isShared) {
      setUnshareConfirmPost(post);
    } else {
      setShareModalPost(post);
    }
  }

  async function handleConfirmShare(post: FeedPost) {
    if (!post.id || isSharePending) {
      return;
    }

    const previousShareCount = typeof post.shareCount === "number" ? post.shareCount : 0;

    setIsSharePending(true);
    updatePost(post.id, (current) => ({
      ...current,
      isShared: true,
      shareCount: previousShareCount + 1,
      shares: formatCount(previousShareCount + 1),
    }));

    try {
      await shareBlog(post.id, { shareType: "PUBLIC" });
      setShareModalPost(null);
    } catch {
      updatePost(post.id, (current) => ({
        ...current,
        isShared: false,
        shareCount: previousShareCount,
        shares: formatCount(previousShareCount),
      }));
    } finally {
      setIsSharePending(false);
    }
  }

  async function handleConfirmUnshare(post: FeedPost) {
    if (!post.id || isSharePending) {
      return;
    }

    const previousShareCount = typeof post.shareCount === "number" ? post.shareCount : 0;

    setIsSharePending(true);
    updatePost(post.id, (current) => ({
      ...current,
      isShared: false,
      shareCount: Math.max(0, previousShareCount - 1),
      shares: formatCount(Math.max(0, previousShareCount - 1)),
    }));

    try {
      await unshareBlog(post.id);
      setUnshareConfirmPost(null);
    } catch {
      updatePost(post.id, (current) => ({
        ...current,
        isShared: true,
        shareCount: previousShareCount,
        shares: formatCount(previousShareCount),
      }));
    } finally {
      setIsSharePending(false);
    }
  }

  if (errorMessage) {
    return (
      <Alert>
        <AlertTitle>Feed unavailable</AlertTitle>
        <AlertDescription>{errorMessage}</AlertDescription>
      </Alert>
    );
  }

  if (items.length === 0) {
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

  const firstPostId = feedItems.find((item) => item.kind === "post")?.id;

  return (
    <>
      <div className="flex flex-col gap-6">
        {feedItems.map((item) =>
          item.kind === "ad" ? (
            <SponsoredCafeCard ad={item.ad} key={item.id} />
          ) : (
            <PostCard
              key={item.id}
              currentUserId={user?.userId}
              eagerMedia={item.id === firstPostId}
              onCommentClick={(selectedPost) => {
                if (selectedPost.id) {
                  setSelectedPostId(selectedPost.id);
                }
              }}
              onLikeClick={handleLikeClick}
              onReportClick={setReportingPost}
              onSaveClick={handleSaveClick}
              onShareClick={handleShareClick}
              post={item.post}
            />
          ),
        )}

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

      <ReportPostModal
        blogId={reportingPost?.id ?? ""}
        onOpenChange={(open) => {
          if (!open) {
            setReportingPost(null);
          }
        }}
        open={Boolean(reportingPost?.id)}
      />

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

      {/* Share modal */}
      <Dialog
        open={Boolean(shareModalPost)}
        onOpenChange={(open) => {
          if (!open) setShareModalPost(null);
        }}
      >
        <DialogContent className="max-w-sm p-0 overflow-hidden rounded-2xl">
          <div className="px-6 pt-6 pb-2">
            <DialogTitle className="text-base font-bold">Chia sẻ bài viết</DialogTitle>
          </div>
          {shareModalPost ? (
            <div className="mx-6 mb-4 rounded-xl border border-border bg-surface-muted p-4">
              <div className="flex items-center gap-3 mb-3">
                <img
                  alt={shareModalPost.author}
                  className="size-9 rounded-full object-cover border border-border/40"
                  src={shareModalPost.authorAvatar ?? "/images/default-avatar.svg"}
                />
                <div className="min-w-0">
                  <p className="truncate text-sm font-bold leading-tight">{shareModalPost.author}</p>
                  <p className="text-xs text-muted">{shareModalPost.time}</p>
                </div>
              </div>
              {shareModalPost.image ? (
                <img
                  alt="post preview"
                  className="w-full rounded-lg object-cover aspect-video mb-3"
                  src={shareModalPost.image}
                />
              ) : null}
              <p className="text-sm text-foreground line-clamp-2 leading-5">
                {shareModalPost.caption}
              </p>
            </div>
          ) : null}
          <p className="px-6 pb-1 text-xs text-muted">
            Bài viết sẽ xuất hiện trên trang cá nhân của bạn.
          </p>
          <div className="flex gap-2 px-6 pb-6 pt-3">
            <Button
              className="flex-1"
              variant="outline"
              onClick={() => setShareModalPost(null)}
            >
              Hủy
            </Button>
            <Button
              className="flex-1"
              disabled={isSharePending}
              onClick={() => shareModalPost && void handleConfirmShare(shareModalPost)}
            >
              {isSharePending ? "Đang chia sẻ..." : "Chia sẻ ngay"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Unshare confirm modal */}
      <Dialog
        open={Boolean(unshareConfirmPost)}
        onOpenChange={(open) => {
          if (!open) setUnshareConfirmPost(null);
        }}
      >
        <DialogContent className="max-w-sm p-0 overflow-hidden rounded-2xl">
          <div className="px-6 pt-6 pb-2">
            <DialogTitle className="text-base font-bold">Gỡ chia sẻ?</DialogTitle>
          </div>
          {unshareConfirmPost ? (
            <div className="mx-6 mb-4 rounded-xl border border-border bg-surface-muted p-4">
              <div className="flex items-center gap-3">
                <img
                  alt={unshareConfirmPost.author}
                  className="size-9 rounded-full object-cover border border-border/40"
                  src={unshareConfirmPost.authorAvatar ?? "/images/default-avatar.svg"}
                />
                <div className="min-w-0">
                  <p className="truncate text-sm font-bold leading-tight">{unshareConfirmPost.author}</p>
                  <p className="text-sm text-muted line-clamp-1">{unshareConfirmPost.caption}</p>
                </div>
              </div>
            </div>
          ) : null}
          <p className="px-6 pb-1 text-xs text-muted">
            Bài viết sẽ bị gỡ khỏi trang cá nhân của bạn.
          </p>
          <div className="flex gap-2 px-6 pb-6 pt-3">
            <Button
              className="flex-1"
              variant="outline"
              onClick={() => setUnshareConfirmPost(null)}
            >
              Không
            </Button>
            <Button
              className="flex-1"
              variant="destructive"
              disabled={isSharePending}
              onClick={() =>
                unshareConfirmPost && void handleConfirmUnshare(unshareConfirmPost)
              }
            >
              {isSharePending ? "Đang gỡ..." : "Gỡ chia sẻ"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}

function formatCount(value: number) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}
