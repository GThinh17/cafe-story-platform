"use client";

import {
  BookmarkIcon,
  HeartIcon,
  MessageCircleIcon,
  MoreHorizontalIcon,
  Repeat2Icon,
  SmileIcon,
} from "lucide-react";
import Link from "next/link";
import { useCallback, useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import {
  PostCommentItem,
  type PostCommentReplyTarget,
} from "@/components/feed/post-comment-item";
import { PostCommentSkeleton } from "@/components/feed/post-comment-skeleton";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import { createComment, getCommentsByBlog } from "@/lib/api/comments";
import { cn } from "@/lib/utils";
import type { AuthUser } from "@/types/auth";
import type { CommentResponse } from "@/types/blog";
import type { FeedPost, FeedPostComment } from "@/types/feed";

type PostCommentsModalProps = {
  currentUser: AuthUser | null;
  onCommentCountChange: (
    postId: string,
    patch: Pick<FeedPost, "commentCount" | "comments">,
  ) => void;
  onOpenChange: (open: boolean) => void;
  onPostLikeClick: (post: FeedPost) => void;
  post: FeedPost | null;
};

function getAuthorAvatar(post: FeedPost) {
  return post.authorAvatar?.trim() || DEFAULT_AVATAR_IMAGE;
}

function getPrimaryMedia(post: FeedPost) {
  const firstMedia = post.media?.find((media) => media.src.trim());

  return {
    alt: firstMedia?.alt ?? `${post.cafe} post media`,
    src: firstMedia?.src ?? post.image,
    type: firstMedia?.type ?? "image",
  };
}

function formatCount(value: number) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}

function getPostCommentCount(post: FeedPost) {
  if (typeof post.commentCount === "number") {
    return formatCount(post.commentCount);
  }

  return Array.isArray(post.comments)
    ? formatCount(post.comments.length)
    : post.comments;
}

function firstNonEmpty(values: Array<string | null | undefined>) {
  return values.find((value) => value?.trim())?.trim();
}

function getUserProfileHref(username: string) {
  return `/${encodeURIComponent(username)}`;
}

function getPostAuthorUsername(post: FeedPost) {
  return post.authorUsername?.trim() || post.author?.trim() || "cafestory_user";
}

type CommentMappingContext = {
  currentUser: AuthUser | null;
  postAuthorUserId?: string;
  postAuthorUsername?: string;
};

function getCommentUsername(
  comment: CommentResponse,
  context: CommentMappingContext,
) {
  const response = comment as CommentResponse & {
    authorUserName?: string | null;
    username?: string | null;
  };
  const currentUserName =
    (context.currentUser as (AuthUser & { username?: string }) | null)?.username ??
    context.currentUser?.userName;

  return (
    firstNonEmpty([
      response.username,
      response.userName,
      response.authorUsername,
      response.authorUserName,
      context.currentUser?.userId === comment.userId ? currentUserName : undefined,
      context.postAuthorUserId === comment.userId
        ? context.postAuthorUsername
        : undefined,
    ]) ?? "CafeStory User"
  );
}

function formatRelativeTime(value?: string) {
  if (!value) {
    return "now";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const diffMinutes = Math.max(
    0,
    Math.floor((Date.now() - date.getTime()) / 60000),
  );

  if (diffMinutes < 1) {
    return "now";
  }

  if (diffMinutes < 60) {
    return `${diffMinutes}m`;
  }

  const diffHours = Math.floor(diffMinutes / 60);

  if (diffHours < 24) {
    return `${diffHours}h`;
  }

  const diffDays = Math.floor(diffHours / 24);

  if (diffDays < 7) {
    return `${diffDays}d`;
  }

  return `${Math.floor(diffDays / 7)}w`;
}

function mapCommentResponse(
  comment: CommentResponse,
  context: CommentMappingContext,
  replyToUsername?: string,
): FeedPostComment {
  const authorUsername = getCommentUsername(comment, context);
  const isCurrentUser = context.currentUser?.userId === comment.userId;

  return {
    id: comment.id,
    author: authorUsername,
    authorUsername,
    authorAvatar: isCurrentUser
      ? context.currentUser?.userAvatar ?? undefined
      : undefined,
    body: comment.content,
    isLiked: false,
    likeCount: 0,
    replyToAuthor: replyToUsername,
    replyToUsername,
    time: formatRelativeTime(comment.createdAt ?? undefined),
  };
}

function mapCommentsToThread(
  comments: CommentResponse[],
  context: CommentMappingContext,
): FeedPostComment[] {
  const childrenByParentId = new Map<string, CommentResponse[]>();

  for (const comment of comments) {
    if (!comment.parentCommentId) {
      continue;
    }

    const parentChildren = childrenByParentId.get(comment.parentCommentId) ?? [];
    parentChildren.push(comment);
    childrenByParentId.set(comment.parentCommentId, parentChildren);
  }

  function flattenDescendants(parent: CommentResponse): FeedPostComment[] {
    const children = childrenByParentId.get(parent.id) ?? [];

    return children.flatMap((child) => [
      mapCommentResponse(
        child,
        context,
        firstNonEmpty([
          child.replyToUsername,
          mapCommentResponse(parent, context).authorUsername,
        ]),
      ),
      ...flattenDescendants(child),
    ]);
  }

  return comments
    .filter((comment) => !comment.parentCommentId)
    .map((comment) => {
      const replyItems = flattenDescendants(comment);

      return {
        ...mapCommentResponse(comment, context),
        replies: String(replyItems.length),
        replyItems,
      };
    });
}

function createOptimisticComment(
  content: string,
  currentUser: AuthUser | null,
  replyTarget: PostCommentReplyTarget | null,
): FeedPostComment {
  const currentUsername =
    firstNonEmpty([
      (currentUser as (AuthUser & { username?: string }) | null)?.username,
      currentUser?.userName,
    ]) ?? "CafeStory User";

  return {
    id: `optimistic-${crypto.randomUUID()}`,
    author: currentUsername,
    authorUsername: currentUsername,
    authorAvatar: currentUser?.userAvatar ?? undefined,
    body: content,
    isLiked: false,
    likeCount: 0,
    replyToAuthor: replyTarget?.authorUsername ?? replyTarget?.author,
    replyToUsername: replyTarget?.authorUsername ?? replyTarget?.author,
    time: "now",
  };
}

function addOptimisticComment(
  comments: FeedPostComment[],
  comment: FeedPostComment,
  replyTarget: PostCommentReplyTarget | null,
) {
  if (!replyTarget) {
    return [comment, ...comments];
  }

  return comments.map((item) => {
    if (item.id !== replyTarget.rootCommentId) {
      return item;
    }

    const replyItems = [...(item.replyItems ?? []), comment];

    return {
      ...item,
      replies: String(replyItems.length),
      replyItems,
    };
  });
}

function removeOptimisticComment(
  comments: FeedPostComment[],
  optimisticCommentId: string,
) {
  return comments
    .filter((comment) => comment.id !== optimisticCommentId)
    .map((comment) => {
      if (!comment.replyItems?.some((reply) => reply.id === optimisticCommentId)) {
        return comment;
      }

      const replyItems = comment.replyItems.filter(
        (reply) => reply.id !== optimisticCommentId,
      );

      return {
        ...comment,
        replies: String(replyItems.length),
        replyItems,
      };
    });
}

function updateCommentLikeState(
  comments: FeedPostComment[],
  commentId: string,
): FeedPostComment[] {
  return comments.map((comment) => {
    if (comment.id === commentId) {
      const wasLiked = Boolean(comment.isLiked);
      const currentLikeCount = comment.likeCount ?? 0;

      return {
        ...comment,
        isLiked: !wasLiked,
        likeCount: Math.max(0, currentLikeCount + (wasLiked ? -1 : 1)),
      };
    }

    return {
      ...comment,
      replyItems: comment.replyItems
        ? updateCommentLikeState(comment.replyItems, commentId)
        : comment.replyItems,
    };
  });
}

export function PostCommentsModal({
  currentUser,
  onCommentCountChange,
  onOpenChange,
  onPostLikeClick,
  post,
}: PostCommentsModalProps) {
  const [draftComment, setDraftComment] = useState("");
  const [comments, setComments] = useState<FeedPostComment[]>([]);
  const [isLoadingComments, setIsLoadingComments] = useState(false);
  const [commentsError, setCommentsError] = useState<string | null>(null);
  const [replyTarget, setReplyTarget] = useState<PostCommentReplyTarget | null>(null);
  const commentInputRef = useRef<HTMLInputElement>(null);

  const loadComments = useCallback(async () => {
    if (!post?.id) {
      return;
    }

    setIsLoadingComments(true);
    setCommentsError(null);

    try {
      const response = await getCommentsByBlog(post.id);
      const visibleComments = mapCommentsToThread(response, {
        currentUser,
        postAuthorUserId: post.authorUserId,
        postAuthorUsername: getPostAuthorUsername(post),
      });
      setComments(visibleComments);
      onCommentCountChange(post.id, {
        commentCount: response.length,
        comments: formatCount(response.length),
      });
    } catch {
      setCommentsError("Unable to load comments.");
    } finally {
      setIsLoadingComments(false);
    }
  }, [
    currentUser,
    onCommentCountChange,
    post?.author,
    post?.authorUserId,
    post?.authorUsername,
    post?.id,
  ]);

  useEffect(() => {
    if (!post?.id) {
      setComments([]);
      return;
    }

    void loadComments();
  }, [loadComments, post?.id]);

  if (!post) {
    return null;
  }

  const authorAvatar = getAuthorAvatar(post);
  const authorUsername = getPostAuthorUsername(post);
  const authorHref = getUserProfileHref(authorUsername);
  const commentCount = getPostCommentCount(post);
  const shareCount = post.shares ?? "0";
  const media = getPrimaryMedia(post);
  const hasImage = media.type === "image" && media.src.trim().length > 0;
  const likeCount =
    typeof post.likeCount === "number" ? formatCount(post.likeCount) : post.likes;
  const postId = post.id;
  const postCommentCount = post.commentCount;

  function handleReply(target: PostCommentReplyTarget) {
    setReplyTarget(target);
    commentInputRef.current?.focus();
  }

  function handleCommentLike(commentId: string) {
    setComments((currentComments) =>
      updateCommentLikeState(currentComments, commentId),
    );
  }

  async function handleCreateComment() {
    const content = draftComment.trim();

    if (!postId || !content) {
      return;
    }

    const currentReplyTarget = replyTarget;
    const optimisticComment = createOptimisticComment(
      content,
      currentUser,
      currentReplyTarget,
    );
    const previousCommentCount = postCommentCount ?? comments.length;
    const nextCommentCount = previousCommentCount + 1;

    setDraftComment("");
    setReplyTarget(null);
    setComments((currentComments) =>
      addOptimisticComment(currentComments, optimisticComment, currentReplyTarget),
    );
    onCommentCountChange(postId, {
      commentCount: nextCommentCount,
      comments: formatCount(nextCommentCount),
    });

    try {
      await createComment({
        blogId: postId,
        content,
        ...(currentReplyTarget
          ? { parentCommentId: currentReplyTarget.commentId }
          : {}),
      });

      await loadComments();
    } catch {
      setComments((currentComments) =>
        removeOptimisticComment(currentComments, optimisticComment.id),
      );
      onCommentCountChange(postId, {
        commentCount: previousCommentCount,
        comments: formatCount(previousCommentCount),
      });
      setDraftComment(content);
      setReplyTarget(currentReplyTarget);
      setCommentsError("Unable to post comment.");
    }
  }

  return (
    <Dialog
      onOpenChange={(open) => {
        if (!open) {
          setDraftComment("");
          setReplyTarget(null);
        }
        onOpenChange(open);
      }}
      open={Boolean(post)}
    >
      <DialogContent className="h-[min(95vh,860px)] max-h-[860px] w-[min(96vw,1000px)] max-w-[1000px] grid-cols-1 overflow-hidden p-0 lg:grid-cols-2">
        <DialogTitle className="sr-only">
          Post details and comments for {authorUsername}
        </DialogTitle>

        <section className="relative min-h-[280px] overflow-hidden bg-espresso lg:min-h-0">
          {hasImage ? (
            <img
              alt={media.alt}
              className="h-full w-full object-cover"
              decoding="async"
              src={media.src}
            />
          ) : (
            <div className="grid h-full min-h-[280px] place-items-center bg-surface-muted px-8 text-center text-sm font-semibold text-coffee-muted">
              No image available
            </div>
          )}
        </section>

        <aside className="flex min-h-0 flex-col bg-surface">
          <header className="flex items-center justify-between gap-4 border-b border-line-soft px-5 py-4">
            <div className="flex min-w-0 items-center gap-3">
              <Link
                aria-label={`View ${authorUsername}'s profile`}
                className="block size-11 shrink-0 rounded-full"
                href={authorHref}
              >
                <img
                  alt={`${authorUsername} avatar`}
                  className="size-full rounded-full border border-border object-cover"
                  decoding="async"
                  src={authorAvatar}
                />
              </Link>
              <div className="min-w-0">
                <h2 className="truncate text-sm font-black text-espresso">
                  <Link className="hover:text-primary" href={authorHref}>
                    {authorUsername}
                  </Link>
                </h2>
                <p className="truncate text-xs font-medium text-coffee-muted">
                  {post.cafe} - {post.location}
                </p>
              </div>
            </div>
            <Button
              aria-label="Post options"
              className="text-coffee-muted hover:bg-transparent hover:text-primary"
              size="icon-sm"
              type="button"
              variant="ghost"
            >
              <MoreHorizontalIcon />
            </Button>
          </header>

          <div className="min-h-0 flex-1 overflow-y-auto px-5 py-5">
            <div className="flex min-w-0 gap-3">
              <Link
                aria-label={`View ${authorUsername}'s profile`}
                className="block size-9 shrink-0 rounded-full"
                href={authorHref}
              >
                <img
                  alt={`${authorUsername} avatar`}
                  className="size-full rounded-full border border-border object-cover"
                  decoding="async"
                  src={authorAvatar}
                />
              </Link>
              <div className="min-w-0 flex-1">
                <p className="break-words text-sm leading-6">
                  <Link
                    className="font-black text-espresso hover:text-primary"
                    href={authorHref}
                  >
                    {authorUsername}
                  </Link>{" "}
                  <span className="text-coffee-muted">{post.caption}</span>
                </p>
                <p className="mt-1 text-xs font-semibold text-muted">
                  {post.time}
                </p>
              </div>
            </div>

            <section className="mt-6 flex flex-col gap-5">
              {isLoadingComments ? (
                <>
                  <PostCommentSkeleton />
                  <PostCommentSkeleton />
                  <PostCommentSkeleton />
                </>
              ) : commentsError ? (
                <div className="rounded-md border border-dashed border-line-soft bg-surface-muted/45 px-5 py-8 text-center">
                  <p className="text-sm font-black text-espresso">
                    {commentsError}
                  </p>
                  <Button
                    className="mt-3 h-9 px-4 text-sm font-black"
                    onClick={() => void loadComments()}
                    type="button"
                    variant="ghost"
                  >
                    Retry
                  </Button>
                </div>
              ) : comments.length > 0 ? (
                comments.map((comment) => (
                  <PostCommentItem
                    comment={comment}
                    key={comment.id}
                    onLike={handleCommentLike}
                    onReply={handleReply}
                  />
                ))
              ) : (
                <div className="rounded-md border border-dashed border-line-soft bg-surface-muted/45 px-5 py-8 text-center">
                  <p className="text-sm font-black text-espresso">
                    No comments yet
                  </p>
                  <p className="mt-2 text-sm leading-6 text-coffee-muted">
                    Start the conversation with your first thought.
                  </p>
                </div>
              )}
            </section>
          </div>

          <footer className="border-t border-line-soft bg-surface py-2">
            <div className="flex items-center gap-4 px-3 text-foreground">
              <Button
                aria-label="Like post"
                className="h-auto gap-1.5 px-0 py-0 text-sm font-bold hover:bg-transparent hover:text-accent"
                onClick={() => onPostLikeClick(post)}
                type="button"
                variant="ghost"
              >
                <HeartIcon
                  className={cn("size-6", post.isLiked && "fill-accent text-accent")}
                  strokeWidth={2.2}
                />
                <span>{likeCount}</span>
              </Button>
              <Button
                aria-label="Comment on post"
                className="h-auto gap-1.5 px-0 py-0 text-sm font-bold hover:bg-transparent hover:text-primary"
                type="button"
                variant="ghost"
              >
                <MessageCircleIcon className="size-6" strokeWidth={2.2} />
                <span>{commentCount}</span>
              </Button>
              <Button
                aria-label="Share post"
                className="h-auto gap-1.5 px-0 py-0 text-sm font-bold hover:bg-transparent hover:text-primary"
                type="button"
                variant="ghost"
              >
                <Repeat2Icon className="size-6" strokeWidth={2.2} />
                <span>{shareCount}</span>
              </Button>
              <Button
                aria-label="Bookmark post"
                className="ml-auto h-auto px-0 py-0 hover:bg-transparent hover:text-primary"
                type="button"
                variant="ghost"
              >
                <BookmarkIcon className="size-7" strokeWidth={2.4} />
              </Button>
            </div>

            <p className="mt-1 px-3 text-xs font-semibold text-muted">
              {post.time}
            </p>

            <div className="mt-4 flex min-w-0 items-center gap-3 border-t border-line-soft px-3 pt-2">
              <SmileIcon className="size-5 shrink-0 text-coffee-muted" />
              <label className="min-w-0 flex-1">
                <span className="sr-only">Add a comment</span>
                <input
                  ref={commentInputRef}
                  className="h-10 w-full min-w-0 bg-transparent text-sm outline-none placeholder:text-muted"
                  onChange={(event) => setDraftComment(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter") {
                      void handleCreateComment();
                    }
                  }}
                  placeholder={
                    replyTarget
                      ? `Reply to ${replyTarget.authorUsername ?? replyTarget.author}...`
                      : "Add a comment..."
                  }
                  type="text"
                  value={draftComment}
                />
              </label>
              <Button
                className="h-9 shrink-0 px-3 text-sm font-black"
                disabled={draftComment.trim().length === 0}
                onClick={() => void handleCreateComment()}
                type="button"
                variant="ghost"
              >
                Post
              </Button>
            </div>
          </footer>
        </aside>
      </DialogContent>
    </Dialog>
  );
}
