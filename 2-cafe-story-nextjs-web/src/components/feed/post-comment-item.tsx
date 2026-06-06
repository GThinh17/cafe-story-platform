"use client";

import { HeartIcon, MoreHorizontalIcon } from "lucide-react";
import Link from "next/link";
import { useState } from "react";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import { cn } from "@/lib/utils";
import type { FeedPostComment } from "@/types/feed";

export type PostCommentReplyTarget = {
  author: string;
  authorUsername?: string;
  commentId: string;
  rootCommentId: string;
};

type PostCommentItemProps = {
  canReply?: boolean;
  comment: FeedPostComment;
  isReply?: boolean;
  onLike: (commentId: string) => void;
  onReply: (target: PostCommentReplyTarget) => void;
  rootCommentId?: string;
};

function formatCount(value: number) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}

function getReplyCount(comment: FeedPostComment) {
  if (!comment.replies) {
    return 0;
  }

  const count = Number.parseInt(comment.replies, 10);

  return Number.isFinite(count) ? count : 0;
}

function getUserProfileHref(username: string) {
  return `/${encodeURIComponent(username)}`;
}

export function PostCommentItem({
  canReply = true,
  comment,
  isReply = false,
  onLike,
  onReply,
  rootCommentId,
}: PostCommentItemProps) {
  const [areRepliesVisible, setAreRepliesVisible] = useState(false);
  const [isBodyExpanded, setIsBodyExpanded] = useState(false);
  const avatar = comment.authorAvatar?.trim() || DEFAULT_AVATAR_IMAGE;
  const authorUsername =
    comment.authorUsername?.trim() || comment.author.trim() || "cafestory_user";
  const authorHref = getUserProfileHref(authorUsername);
  const replyToUsername =
    comment.replyToUsername?.trim() || comment.replyToAuthor?.trim();
  const replyToHref = replyToUsername ? getUserProfileHref(replyToUsername) : undefined;
  const likeCount = comment.likeCount ?? 0;
  const replyCount = getReplyCount(comment);
  const replyItems = comment.replyItems ?? [];
  const shouldClampBody = comment.body.length > 220;
  const shouldShowReplyToggle = !isReply && (replyCount > 0 || replyItems.length > 0);
  const visibleReplyCount = Math.max(replyCount, replyItems.length);
  const threadRootId = rootCommentId ?? comment.id;

  return (
    <article className={cn("min-w-0", isReply && "pl-12")}>
      <div className="flex min-w-0 gap-3">
        <Link
          aria-label={`View ${authorUsername}'s profile`}
          className={cn(
            "block shrink-0 rounded-full",
            isReply ? "size-8" : "size-9",
          )}
          href={authorHref}
        >
          <img
            alt={`${authorUsername} avatar`}
            className="size-full rounded-full border border-border object-cover"
            decoding="async"
            loading="lazy"
            src={avatar}
          />
        </Link>
        <div className="min-w-0 flex-1">
          <p
            className={cn(
              "break-words text-sm leading-5 text-foreground",
              shouldClampBody &&
                !isBodyExpanded &&
                "overflow-hidden [display:-webkit-box] [-webkit-box-orient:vertical] [-webkit-line-clamp:4]",
            )}
          >
            <Link className="font-black text-espresso hover:text-primary" href={authorHref}>
              {authorUsername}
            </Link>{" "}
            {isReply && replyToUsername && replyToHref ? (
              <>
                <Link className="text-primary hover:underline" href={replyToHref}>
                  @{replyToUsername}
                </Link>{" "}
              </>
            ) : null}
            <span className="text-foreground">{comment.body}</span>
          </p>
          {shouldClampBody ? (
            <button
              className="mt-3 flex items-center gap-5 text-xs font-bold text-muted transition hover:text-primary"
              onClick={() => setIsBodyExpanded((isExpanded) => !isExpanded)}
              type="button"
            >
              <span className="h-px w-8 bg-muted/70" />
              <span className="text-xs font-normal text-muted-foreground">
                {isBodyExpanded ? "Hide" : "More"}
              </span>
            </button>
          ) : null}
          <div className="mt-2 flex flex-wrap items-center gap-4 text-xs font-semibold text-muted">
            {comment.time ? <span>{comment.time}</span> : null}
            <button className="cursor-pointer transition hover:text-primary" type="button">
              {formatCount(likeCount)} likes
            </button>
            {canReply ? (
              <button
                className="cursor-pointer transition hover:text-primary"
                onClick={() =>
                  onReply({
                    author: comment.author,
                    authorUsername,
                    commentId: comment.id,
                    rootCommentId: threadRootId,
                  })
                }
                type="button"
              >
                Reply
              </button>
            ) : null}
            <button
              aria-label="More comment options"
              className="cursor-pointer transition hover:text-primary"
              type="button"
            >
              <MoreHorizontalIcon className="size-4" />
            </button>
          </div>
        </div>
        <button
          aria-label="Like comment"
          className="mt-0.5 shrink-0 cursor-pointer text-coffee-muted transition hover:text-primary"
          onClick={() => onLike(comment.id)}
          type="button"
        >
          <HeartIcon
            className={cn("size-5", comment.isLiked && "fill-accent text-accent")}
            strokeWidth={2.2}
          />
        </button>
      </div>

      {shouldShowReplyToggle ? (
        <button
          className="ml-12 mt-4 flex items-center gap-5 text-xs font-bold text-muted transition hover:text-primary"
          onClick={() => setAreRepliesVisible((isVisible) => !isVisible)}
          type="button"
        >
          <span className="h-px w-8 bg-muted/70" />
          <span className="text-xs font-normal text-muted-foreground">
            {areRepliesVisible
              ? "Hide replies"
              : `View replies (${visibleReplyCount})`}
          </span>
        </button>
      ) : null}

      {areRepliesVisible && replyItems.length > 0 ? (
        <div className="mt-5 flex flex-col gap-5">
          {replyItems.map((reply) => (
            <PostCommentItem
              comment={reply}
              canReply={canReply}
              isReply
              key={reply.id}
              onLike={onLike}
              onReply={onReply}
              rootCommentId={comment.id}
            />
          ))}
        </div>
      ) : null}
    </article>
  );
}
