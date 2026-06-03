"use client";

import { useState } from "react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { PostCard } from "@/components/feed/post-card";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import type { FeedPost } from "@/types/feed";

type FeedPostListProps = {
  errorMessage?: string;
  posts: FeedPost[];
};

export function FeedPostList({ errorMessage, posts }: FeedPostListProps) {
  const [selectedPost, setSelectedPost] = useState<FeedPost | null>(null);

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
        {posts.map((post) => (
          <PostCard
            key={post.id ?? post.cafe}
            onCommentClick={setSelectedPost}
            post={post}
          />
        ))}
      </div>

      <PostCommentsModal
        onOpenChange={(open) => {
          if (!open) {
            setSelectedPost(null);
          }
        }}
        post={selectedPost}
      />
    </>
  );
}
