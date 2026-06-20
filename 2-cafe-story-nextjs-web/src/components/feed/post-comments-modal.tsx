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
import {
  getFeedPostMediaList,
  PostMediaCarousel,
} from "@/components/feed/post-media-carousel";
import { getPostIdentity } from "@/components/feed/post-identity";
import { getBlogById } from "@/lib/api/blogs";
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

function getPostAuthorUsername(post: FeedPost) {
  return post.authorUsername?.trim() || post.author?.trim() || "cafestory_user";
}

type CommentMappingContext = {
  currentUser: AuthUser | null;
};

function getCurrentUserUsername(currentUser: AuthUser | null) {
  return firstNonEmpty([
    currentUser?.userName,
    (currentUser as (AuthUser & { username?: string }) | null)?.username,
  ]);
}

function getCommentUsername(
  comment: CommentResponse,
  context: CommentMappingContext,
) {
  const currentUserName = getCurrentUserUsername(context.currentUser);

  return (
    firstNonEmpty([
      comment.authorUserName,
      comment.username,
      comment.userName,
      comment.authorUsername,
      context.currentUser?.userId === comment.userId ? currentUserName : undefined,
    ]) ?? "cafestory_user"
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
    authorAvatar:
      comment.authorUserAvatar ??
      (isCurrentUser ? context.currentUser?.userAvatar ?? undefined : undefined),
    body: comment.content,
    isLiked: false,
    likeCount: 0,
    localStatus: "sent",
    replyToAuthor: replyToUsername,
    replyToUsername,
    serverId: comment.id,
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
    const parentUsername = getCommentUsername(parent, context);

    return children.flatMap((child) => [
      mapCommentResponse(child, context, parentUsername),
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
  const currentUsername = getCurrentUserUsername(currentUser) ?? "cafestory_user";

  return {
    id: `optimistic-${crypto.randomUUID()}`,
    author: currentUsername,
    authorUsername: currentUsername,
    authorAvatar: currentUser?.userAvatar ?? undefined,
    body: content,
    isLiked: false,
    likeCount: 0,
    localStatus: "sending",
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

function replaceOptimisticComment(
  comments: FeedPostComment[],
  optimisticCommentId: string,
  nextComment: FeedPostComment,
) {
  return comments.map((comment) => {
    if (comment.id === optimisticCommentId) {
      return nextComment;
    }

    if (!comment.replyItems?.some((reply) => reply.id === optimisticCommentId)) {
      return comment;
    }

    return {
      ...comment,
      replyItems: comment.replyItems.map((reply) =>
        reply.id === optimisticCommentId ? nextComment : reply,
      ),
    };
  });
}

function markOptimisticCommentError(
  comments: FeedPostComment[],
  optimisticCommentId: string,
) {
  return comments.map((comment) => {
    if (comment.id === optimisticCommentId) {
      return {
        ...comment,
        localStatus: "error" as const,
      };
    }

    if (!comment.replyItems?.some((reply) => reply.id === optimisticCommentId)) {
      return comment;
    }

    const replyItems = comment.replyItems.map((reply) =>
      reply.id === optimisticCommentId
        ? {
            ...reply,
            localStatus: "error" as const,
          }
        : reply,
    );
    const sentReplyCount = replyItems.filter(
      (reply) => reply.localStatus !== "error",
    ).length;

    return {
      ...comment,
      replies: String(sentReplyCount),
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
  const [isLoadingCommentPermission, setIsLoadingCommentPermission] = useState(false);
  const [resolvedAllowComment, setResolvedAllowComment] = useState<boolean | null>(
    null,
  );
  const [commentsError, setCommentsError] = useState<string | null>(null);
  const [replyTarget, setReplyTarget] = useState<PostCommentReplyTarget | null>(null);
  const commentInputRef = useRef<HTMLInputElement>(null);
  const commentCountRef = useRef(0);
  const loadedCommentsPostIdRef = useRef<string | null>(null);

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
      });
      setComments(visibleComments);
      commentCountRef.current = response.length;
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
    post?.id,
  ]);

  useEffect(() => {
    if (!post?.id) {
      setComments([]);
      commentCountRef.current = 0;
      loadedCommentsPostIdRef.current = null;
      return;
    }

    if (loadedCommentsPostIdRef.current === post.id) {
      return;
    }

    loadedCommentsPostIdRef.current = post.id;
    void loadComments();
  }, [loadComments, post?.id]);

  useEffect(() => {
    if (typeof post?.commentCount === "number") {
      commentCountRef.current = post.commentCount;
    }
  }, [post?.commentCount, post?.id]);

  useEffect(() => {
    if (!post?.id) {
      setIsLoadingCommentPermission(false);
      setResolvedAllowComment(null);
      return;
    }

    setResolvedAllowComment(post.allowComment ?? null);

    if (post.allowComment === false) {
      setIsLoadingCommentPermission(false);
      return;
    }

    let isActive = true;

    setIsLoadingCommentPermission(true);

    getBlogById(post.id)
      .then((blog) => {
        if (isActive) {
          setResolvedAllowComment(blog.allowComment ?? post.allowComment ?? true);
        }
      })
      .catch(() => {
        if (isActive) {
          setResolvedAllowComment(post.allowComment ?? true);
        }
      })
      .finally(() => {
        if (isActive) {
          setIsLoadingCommentPermission(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [post?.allowComment, post?.id]);

  const isCommentRestricted = resolvedAllowComment === false;
  const isCommentDisabled = isLoadingCommentPermission || isCommentRestricted;

  useEffect(() => {
    if (isCommentRestricted) {
      setDraftComment("");
      setReplyTarget(null);
    }
  }, [isCommentRestricted]);

  if (!post) {
    return null;
  }

  const identity = getPostIdentity(post);
  const authorUsername = getPostAuthorUsername(post);
  const locationLabel = post.locationLabel?.trim() || post.location?.trim();
  const commentCount = getPostCommentCount(post);
  const shareCount = post.shares ?? "0";
  const media = getFeedPostMediaList(post);
  const hasImage = media.length > 0;
  const likeCount =
    typeof post.likeCount === "number" ? formatCount(post.likeCount) : post.likes;
  const postId = post.id;

  function handleReply(target: PostCommentReplyTarget) {
    if (isCommentDisabled) {
      return;
    }

    setReplyTarget(target);
    commentInputRef.current?.focus();
  }

  function handleCommentLike(commentId: string) {
    setComments((currentComments) =>
      updateCommentLikeState(currentComments, commentId),
    );
  }

  async function handleCreateComment() {
    if (isCommentDisabled) {
      return;
    }

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
    const previousCommentCount = commentCountRef.current;
    const nextCommentCount = previousCommentCount + 1;

    commentCountRef.current = nextCommentCount;
    setDraftComment("");
    setReplyTarget(null);
    setCommentsError(null);
    setComments((currentComments) =>
      addOptimisticComment(currentComments, optimisticComment, currentReplyTarget),
    );
    onCommentCountChange(postId, {
      commentCount: nextCommentCount,
      comments: formatCount(nextCommentCount),
    });

    try {
      const createdComment = await createComment({
        blogId: postId,
        content,
        ...(currentReplyTarget
          ? { parentCommentId: currentReplyTarget.commentId }
          : {}),
      });
      const confirmedComment = {
        ...mapCommentResponse(
          createdComment,
          { currentUser },
          currentReplyTarget?.authorUsername ?? currentReplyTarget?.author,
        ),
        localStatus: "sent" as const,
        serverId: createdComment.id,
      };

      setComments((currentComments) =>
        replaceOptimisticComment(
          currentComments,
          optimisticComment.id,
          confirmedComment,
        ),
      );
    } catch {
      commentCountRef.current = previousCommentCount;
      setComments((currentComments) =>
        markOptimisticCommentError(currentComments, optimisticComment.id),
      );
      onCommentCountChange(postId, {
        commentCount: previousCommentCount,
        comments: formatCount(previousCommentCount),
      });
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
            <PostMediaCarousel
              className="h-full min-h-[280px] lg:min-h-0"
              frame="fixed"
              imageClassName="bg-espresso"
              media={media}
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
                aria-label={`View ${identity.primaryName}`}
                className="block size-11 shrink-0 cursor-pointer rounded-full"
                href={identity.primaryHref}
              >
                <img
                  alt={`${identity.primaryName} avatar`}
                  className="size-full rounded-full border border-border object-cover"
                  decoding="async"
                  src={identity.primaryAvatar}
                />
              </Link>
              <div className="min-w-0">
                <h2 className="truncate text-sm font-black text-espresso">
                  <Link className="cursor-pointer" href={identity.primaryHref}>
                    {identity.primaryName}
                  </Link>
                </h2>
                {identity.isPagePost && identity.secondaryHref ? (
                  <Link
                    className="block cursor-pointer truncate text-xs font-bold text-coffee-muted"
                    href={identity.secondaryHref}
                  >
                    {identity.secondaryName}
                  </Link>
                ) : null}
              </div>
            </div>
            <Button
              aria-label="Post options"
              className="cursor-pointer text-coffee-muted hover:bg-transparent hover:text-primary"
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
                aria-label={`View ${identity.primaryName}`}
                className="block size-9 shrink-0 cursor-pointer rounded-full"
                href={identity.primaryHref}
              >
                <img
                  alt={`${identity.primaryName} avatar`}
                  className="size-full rounded-full border border-border object-cover"
                  decoding="async"
                  src={identity.primaryAvatar}
                />
              </Link>
              <div className="min-w-0 flex-1">
                <p className="break-words text-sm leading-6">
                  <Link
                    className="cursor-pointer font-black text-espresso"
                    href={identity.primaryHref}
                  >
                    {identity.primaryName}
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
                    canReply={!isCommentDisabled}
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
                className="h-auto cursor-pointer gap-1.5 px-0 py-0 text-sm font-bold hover:bg-transparent hover:text-primary"
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
                className="h-auto cursor-pointer gap-1.5 px-0 py-0 text-sm font-bold hover:bg-transparent hover:text-primary"
                type="button"
                variant="ghost"
              >
                <MessageCircleIcon className="size-6" strokeWidth={2.2} />
                <span>{commentCount}</span>
              </Button>
              <Button
                aria-label="Share post"
                className="h-auto cursor-pointer gap-1.5 px-0 py-0 text-sm font-bold hover:bg-transparent hover:text-primary"
                type="button"
                variant="ghost"
              >
                <Repeat2Icon className="size-6" strokeWidth={2.2} />
                <span>{shareCount}</span>
              </Button>
              <Button
                aria-label="Bookmark post"
                className="ml-auto h-auto cursor-pointer px-0 py-0 hover:bg-transparent hover:text-primary"
                type="button"
                variant="ghost"
              >
                <BookmarkIcon className="size-7" strokeWidth={2.4} />
              </Button>
            </div>

            {locationLabel ? (
              <p className="mt-1 px-3 text-xs font-semibold text-muted">
                {locationLabel}
              </p>
            ) : null}

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
                  onChange={(event) => {
                    if (isCommentDisabled) {
                      return;
                    }

                    setDraftComment(event.target.value);
                  }}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" && !isCommentDisabled) {
                      void handleCreateComment();
                    }
                  }}
                  placeholder={
                    isCommentRestricted
                      ? "Comment restricted"
                      : replyTarget
                      ? `Reply to ${replyTarget.authorUsername ?? replyTarget.author}...`
                      : "Add a comment..."
                  }
                  type="text"
                  disabled={isCommentDisabled}
                  readOnly={isCommentDisabled}
                  value={isCommentDisabled ? "" : draftComment}
                />
              </label>
              <Button
                className="h-9 shrink-0 px-3 text-sm font-black"
                disabled={isCommentDisabled || draftComment.trim().length === 0}
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
