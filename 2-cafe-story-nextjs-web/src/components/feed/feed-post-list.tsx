"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { PostCard } from "@/components/feed/post-card";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import { getBlogLikesByUser, likeBlog, unlikeBlog } from "@/lib/api/blogs";
import { useCurrentUser } from "@/hooks/use-current-user";
import type { FeedPost } from "@/types/feed";

type FeedPostListProps = {
  errorMessage?: string;
  posts: FeedPost[];
};

export function FeedPostList({ errorMessage, posts }: FeedPostListProps) {
  const [feedPosts, setFeedPosts] = useState(posts);
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

  useEffect(() => {
    setFeedPosts(posts);
  }, [posts]);

  useEffect(() => {
    if (!user?.userId) {
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

        setFeedPosts((currentPosts) =>
          currentPosts.map((post) => ({
            ...post,
            isLiked: post.id ? likedPostIds.has(post.id) : false,
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
      </div>

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
