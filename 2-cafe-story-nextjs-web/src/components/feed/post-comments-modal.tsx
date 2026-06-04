"use client";

import {
  BookmarkIcon,
  HeartIcon,
  MessageCircleIcon,
  MoreHorizontalIcon,
  Repeat2Icon,
  SmileIcon,
} from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogTitle,
} from "@/components/ui/dialog";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import type { FeedPost, FeedPostComment } from "@/types/feed";

type PostCommentsModalProps = {
  onOpenChange: (open: boolean) => void;
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

function getPostComments(post: FeedPost) {
  return Array.isArray(post.comments) ? post.comments : (post.commentItems ?? []);
}

function getPostCommentCount(post: FeedPost) {
  return Array.isArray(post.comments)
    ? new Intl.NumberFormat("en", {
        notation: "compact",
        maximumFractionDigits: 1,
      }).format(post.comments.length)
    : post.comments;
}

function CommentItem({ comment }: { comment: FeedPostComment }) {
  const avatar = comment.authorAvatar?.trim() || DEFAULT_AVATAR_IMAGE;

  return (
    <article className="flex min-w-0 gap-3">
      <img
        alt={`${comment.author} avatar`}
        className="size-9 shrink-0 rounded-full border border-border object-cover"
        decoding="async"
        loading="lazy"
        src={avatar}
      />
      <div className="min-w-0 flex-1">
        <p className="break-words text-sm leading-6">
          <span className="font-black text-espresso">{comment.author}</span>{" "}
          <span className="text-coffee-muted">{comment.body}</span>
        </p>
        <div className="mt-1 flex flex-wrap gap-3 text-xs font-semibold text-muted">
          {comment.time ? <span>{comment.time}</span> : null}
          {comment.likes ? <span>{comment.likes} likes</span> : null}
          {comment.replies ? <span>{comment.replies} replies</span> : null}
          <button className="transition hover:text-primary" type="button">
            Reply
          </button>
        </div>
      </div>
    </article>
  );
}

export function PostCommentsModal({
  onOpenChange,
  post,
}: PostCommentsModalProps) {
  const [draftComment, setDraftComment] = useState("");

  if (!post) {
    return null;
  }

  const authorAvatar = getAuthorAvatar(post);
  const comments = getPostComments(post);
  const commentCount = getPostCommentCount(post);
  const shareCount = post.shares ?? "0";
  const media = getPrimaryMedia(post);
  const hasImage = media.type === "image" && media.src.trim().length > 0;

  return (
    <Dialog
      onOpenChange={(open) => {
        if (!open) {
          setDraftComment("");
        }
        onOpenChange(open);
      }}
      open={Boolean(post)}
    >
      <DialogContent className="h-[min(95vh,860px)] max-h-[860px] w-[min(96vw,1000px)] max-w-[1000px] grid-cols-1 overflow-hidden p-0 lg:grid-cols-2">
        <DialogTitle className="sr-only">
          Post details and comments for {post.author}
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
              Không có hình ảnh
            </div>
          )}
        </section>

        <aside className="flex min-h-0 flex-col bg-surface">
          <header className="flex items-center justify-between gap-4 border-b border-line-soft px-5 py-4">
            <div className="flex min-w-0 items-center gap-3">
              <img
                alt={`${post.author} avatar`}
                className="size-11 shrink-0 rounded-full border border-border object-cover"
                decoding="async"
                src={authorAvatar}
              />
              <div className="min-w-0">
                <h2 className="truncate text-sm font-black text-espresso">
                  {post.author || "CafeStory User"}
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
              <img
                alt={`${post.author} avatar`}
                className="size-9 shrink-0 rounded-full border border-border object-cover"
                decoding="async"
                src={authorAvatar}
              />
              <div className="min-w-0 flex-1">
                <p className="break-words text-sm leading-6">
                  <span className="font-black text-espresso">
                    {post.author || "CafeStory User"}
                  </span>{" "}
                  <span className="text-coffee-muted">{post.caption}</span>
                </p>
                <p className="mt-1 text-xs font-semibold text-muted">
                  {post.time}
                </p>
              </div>
            </div>

            <section className="mt-6 flex flex-col gap-5">
              {comments.length > 0 ? (
                comments.map((comment) => (
                  <CommentItem comment={comment} key={comment.id} />
                ))
              ) : (
                <div className="rounded-md border border-dashed border-line-soft bg-surface-muted/45 px-5 py-8 text-center">
                  <p className="text-sm font-black text-espresso">
                    Chưa có bình luận nào
                  </p>
                  <p className="mt-2 text-sm leading-6 text-coffee-muted">
                    Hãy là người đầu tiên chia sẻ cảm nhận.
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
                type="button"
                variant="ghost"
              >
                <HeartIcon className="size-6" strokeWidth={2.2} />
                <span>{post.likes}</span>
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

            <div className="px-3 mt-4 flex min-w-0 items-center gap-3 border-t border-line-soft pt-2">
              <SmileIcon className="size-5 shrink-0 text-coffee-muted" />
              <label className="min-w-0 flex-1">
                <span className="sr-only">Add a comment</span>
                <input
                  className="h-10 w-full min-w-0 bg-transparent text-sm outline-none placeholder:text-muted"
                  onChange={(event) => setDraftComment(event.target.value)}
                  placeholder="Add a comment..."
                  type="text"
                  value={draftComment}
                />
              </label>
              <Button
                className="h-9 shrink-0 px-3 text-sm font-black"
                disabled={draftComment.trim().length === 0}
                type="button"
                variant="ghost"
              >
                Gửi
              </Button>
            </div>
          </footer>
        </aside>
      </DialogContent>
    </Dialog>
  );
}
