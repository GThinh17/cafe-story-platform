"use client";

import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import type { FeedPost } from "@/types/feed";

type PostActionConfirmDialogProps = {
  cancelLabel: string;
  confirmLabel: string;
  errorMessage?: string | null;
  isPending?: boolean;
  notice: string;
  onCancel: () => void;
  onConfirm: (post: FeedPost) => void;
  pendingLabel: string;
  post: FeedPost | null;
  title: string;
};

/**
 * Destructive confirmation over a single post — deleting your own post, removing a
 * share. Shows who wrote it and the first line of the caption so the author can
 * tell two similar posts apart before committing.
 */
export function PostActionConfirmDialog({
  cancelLabel,
  confirmLabel,
  errorMessage,
  isPending = false,
  notice,
  onCancel,
  onConfirm,
  pendingLabel,
  post,
  title,
}: PostActionConfirmDialogProps) {
  return (
    <Dialog
      open={Boolean(post)}
      onOpenChange={(open) => {
        if (!open) onCancel();
      }}
    >
      <DialogContent className="max-w-sm overflow-hidden rounded-2xl p-0">
        <div className="px-6 pb-2 pt-6">
          <DialogTitle className="text-base font-bold">{title}</DialogTitle>
        </div>
        {post ? (
          <div className="mx-6 mb-4 rounded-xl border border-border bg-surface-muted p-4">
            <div className="flex items-center gap-3">
              <img
                alt={post.author}
                className="size-9 rounded-full border border-border/40 object-cover"
                src={post.authorAvatar ?? "/images/default-avatar.svg"}
              />
              <div className="min-w-0">
                <p className="truncate text-sm font-bold leading-tight">
                  {post.author}
                </p>
                <p className="line-clamp-1 text-sm text-muted">{post.caption}</p>
              </div>
            </div>
          </div>
        ) : null}
        <p className="px-6 pb-1 text-xs text-muted">{notice}</p>
        {errorMessage ? (
          <p className="px-6 pt-2 text-xs font-semibold text-destructive">
            {errorMessage}
          </p>
        ) : null}
        <div className="flex gap-2 px-6 pb-6 pt-3">
          <Button className="flex-1" onClick={onCancel} variant="outline">
            {cancelLabel}
          </Button>
          <Button
            className="flex-1"
            disabled={isPending}
            onClick={() => post && onConfirm(post)}
            variant="destructive"
          >
            {isPending ? pendingLabel : confirmLabel}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
