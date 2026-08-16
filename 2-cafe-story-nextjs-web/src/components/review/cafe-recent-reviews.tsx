"use client";

import {
  BookmarkIcon,
  HeartIcon,
  MessageCircleIcon,
  Repeat2Icon,
} from "lucide-react";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  getFeedPostMediaList,
} from "@/components/feed/post-media-carousel";
import { getPostIdentity } from "@/components/feed/post-identity";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import { useCurrentUser } from "@/hooks/use-current-user";
import { likeBlog, unlikeBlog } from "@/lib/api/blogs";
import { cn } from "@/lib/utils";
import type { FeedPost } from "@/types/feed";
import { useI18n } from "@/components/providers/locale-provider";

type CafeRecentReviewsProps = {
  emptyDescription?: string;
  emptyTitle?: string;
  errorMessage?: string | null;
  onMapViewClick: () => void;
  posts: FeedPost[];
};

const postActions = [
  {
    label: "Like",
    icon: HeartIcon,
  },
  {
    label: "Comment",
    icon: MessageCircleIcon,
  },
  {
    label: "Share",
    icon: Repeat2Icon,
  },
];

function formatCount(value: number) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}

function formatPostCommentCount(post: FeedPost) {
  return typeof post.commentCount === "number"
    ? formatCount(post.commentCount)
    : Array.isArray(post.comments)
      ? formatCount(post.comments.length)
      : post.comments;
}

function formatPostLikeCount(post: FeedPost) {
  return typeof post.likeCount === "number"
    ? formatCount(post.likeCount)
    : post.likes;
}

function getPostImage(post: FeedPost) {
  return getFeedPostMediaList(post)[0]?.src ?? post.image;
}

export function CafeRecentReviews({
  emptyDescription,
  emptyTitle,
  errorMessage,
  onMapViewClick,
  posts,
}: CafeRecentReviewsProps) {
  const { t } = useI18n();
  const { user } = useCurrentUser();
  const [gridPosts, setGridPosts] = useState(posts);
  const [selectedPostId, setSelectedPostId] = useState<string | null>(null);
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
    setGridPosts(posts);
  }, [posts]);

  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-end justify-between gap-4">
        <h2 className="text-2xl font-black text-espresso">
          Recent Posts
        </h2>
        <div className="flex items-center gap-5 text-xs font-medium text-coffee-muted">
          <Button
            className="h-auto cursor-pointer bg-transparent p-0 text-xs font-black text-primary hover:bg-transparent hover:text-primary"
            type="button"
            variant="ghost"
          >
            Feed
          </Button>
          <Button
            className="h-auto cursor-pointer bg-transparent p-0 text-xs font-black text-coffee-muted hover:bg-transparent hover:text-primary"
            onClick={onMapViewClick}
            type="button"
            variant="ghost"
          >
            Map View
          </Button>
        </div>
      </div>

      {errorMessage && gridPosts.length === 0 ? (
        <div className="rounded-md border border-line-soft bg-surface-muted px-5 py-6 text-sm font-semibold text-muted">
          {errorMessage}
        </div>
      ) : gridPosts.length === 0 ? (
        <div className="rounded-md border border-line-soft bg-surface-muted px-5 py-6">
          <p className="text-sm font-black text-espresso">
            {emptyTitle ?? t("cafeReviews.emptyTitle")}
          </p>
          <p className="mt-2 text-sm leading-6 text-coffee-muted">
            {emptyDescription ?? t("cafeReviews.emptyDescription")}
          </p>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-3">
          {gridPosts.map((post) => {
            const identity = getPostIdentity(post);
            const image = getPostImage(post);
            const commentCount = formatPostCommentCount(post);
            const actionCounts: Record<string, string> = {
              Comment: commentCount,
              Like: formatPostLikeCount(post),
              Share: post.shares ?? "0",
            };

            return (
              <Card
                className="overflow-hidden border-line-soft bg-surface shadow-none"
                key={post.id ?? post.cafe}
              >
                <div className="relative">
                  <img
                    alt={t("cafeReviews.postAlt", { name: identity.primaryName })}
                    className="aspect-square w-full object-cover"
                    decoding="async"
                    loading="lazy"
                    src={image}
                  />
                  <Badge className="absolute right-3 top-3 rounded-sm bg-surface px-2 py-1 text-xs font-black text-espresso shadow-sm">
                    {post.rating}
                  </Badge>
                </div>

                <CardContent className="flex flex-col gap-4 p-4">
                  <div className="flex min-w-0 items-center gap-3">
                    <Link
                      aria-label={t("post.viewProfile", {
                        name: identity.primaryName,
                      })}
                      className="block size-10 shrink-0 cursor-pointer overflow-hidden rounded-full border border-border/40 bg-surface-muted"
                      href={identity.primaryHref}
                    >
                      <img
                        alt={t("post.avatarAlt", { name: identity.primaryName })}
                        className="size-full object-cover"
                        decoding="async"
                        loading="lazy"
                        src={identity.primaryAvatar}
                      />
                    </Link>
                    <div className="min-w-0">
                      <Link
                        className="block cursor-pointer truncate text-base font-bold text-espresso hover:text-primary"
                        href={identity.primaryHref}
                      >
                        {identity.primaryName}
                      </Link>
                      <p className="truncate text-xs font-medium uppercase tracking-[0.03em] text-coffee-muted">
                        {post.locationLabel || post.location || post.time}
                      </p>
                    </div>
                  </div>

                  <p className="h-12 overflow-hidden text-sm leading-6 text-coffee-muted">
                    {post.caption}
                  </p>

                  <div className="flex items-center justify-between pt-1 text-espresso">
                    <div className="flex items-center gap-4">
                      {postActions.map(({ icon: Icon, label }) => (
                        <Button
                          aria-label={label}
                          className="h-auto cursor-pointer gap-1 px-0 py-0 text-espresso hover:bg-transparent hover:text-primary"
                          key={label}
                          onClick={
                            label === "Comment"
                              ? () => {
                                  if (post.id) {
                                    setSelectedPostId(post.id);
                                  }
                                }
                              : label === "Like"
                                ? () => void handleLikeClick(post)
                                : undefined
                          }
                          type="button"
                          variant="ghost"
                        >
                          <Icon
                            className={cn(
                              "size-5",
                              label === "Like" &&
                                post.isLiked &&
                                "fill-primary text-primary",
                            )}
                            strokeWidth={2.2}
                          />
                          <span className="text-xs font-black">
                            {actionCounts[label]}
                          </span>
                        </Button>
                      ))}
                    </div>
                    <Button
                      aria-label={t("cafeReviews.bookmark")}
                      className="h-auto cursor-pointer px-0 py-0 text-espresso hover:bg-transparent hover:text-primary"
                      type="button"
                      variant="ghost"
                    >
                      <BookmarkIcon className="size-5" strokeWidth={2.2} />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {errorMessage && gridPosts.length > 0 ? (
        <div className="rounded-md border border-line-soft bg-surface-muted px-4 py-3 text-sm font-semibold text-muted">
          {errorMessage}
        </div>
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
    </section>
  );
}
