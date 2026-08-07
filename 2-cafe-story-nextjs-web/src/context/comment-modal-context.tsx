"use client";

import { createContext, useCallback, useContext, useState } from "react";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getBlogById } from "@/lib/api/blogs";
import { mapBlogResponsesToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import type { FeedPost } from "@/types/feed";
import { useI18n } from "@/components/providers/locale-provider";

type CommentModalContextValue = {
  openByBlogId: (blogId: string) => void;
  close: () => void;
};

const CommentModalContext = createContext<CommentModalContextValue | null>(null);

export function useCommentModal() {
  const ctx = useContext(CommentModalContext);
  if (!ctx) throw new Error("useCommentModal must be used inside CommentModalProvider");
  return ctx;
}

export function CommentModalProvider({ children }: { children: React.ReactNode }) {
  const { t } = useI18n();
  const { user } = useCurrentUser();
  const [post, setPost] = useState<FeedPost | null>(null);

  const close = useCallback(() => setPost(null), []);

  const openByBlogId = useCallback(async (blogId: string) => {
    try {
      const blog = await getBlogById(blogId);
      const mapped = mapBlogResponsesToFeedPosts([blog], t)[0];
      if (mapped) setPost(mapped);
    } catch {
      setPost(null);
    }
  }, []);

  return (
    <CommentModalContext.Provider value={{ openByBlogId, close }}>
      {children}
      <PostCommentsModal
        post={post}
        currentUser={user}
        onOpenChange={(open) => { if (!open) close(); }}
        onCommentCountChange={() => {}}
        onPostLikeClick={() => {}}
      />
    </CommentModalContext.Provider>
  );
}
