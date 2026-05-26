"use client";

import { useEffect } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { ReviewComposerModel, ReviewDraftHint } from "@/types/review";

type CreatePostModalProps = {
  composer: ReviewComposerModel;
  hints: ReviewDraftHint[];
  isOpen: boolean;
  onClose: () => void;
};

export function CreatePostModal({
  composer,
  hints: _hints,
  isOpen,
  onClose,
}: CreatePostModalProps) {
  useEffect(() => {
    if (!isOpen) {
      return;
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose();
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) {
    return null;
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="flex h-[80vh] flex-col p-0">
        <form
          className="flex h-full w-full flex-col overflow-hidden rounded-md border border-line-soft bg-surface shadow-[0_26px_80px_rgba(39,19,16,0.22)]"
          onSubmit={(event) => {
            event.preventDefault();
            onClose();
          }}
        >
          <header className="flex h-[72px] items-center justify-between border-b border-line-soft px-6">
            <div className="flex items-center gap-4">
              <DialogTitle className="font-serif text-xl font-semibold text-espresso">
                {composer.title}
              </DialogTitle>
            </div>

            <div className="flex items-center gap-4">
              <Badge className="hidden items-center gap-2 rounded-full bg-primary/15 px-4 py-1.5 text-[10px] font-black uppercase tracking-[0.08em] text-primary-strong sm:flex">
                <span className="grid h-3.5 w-3.5 place-items-center rounded-full border border-[#486058] text-[9px] leading-none">
                  ok
                </span>
                Ready to post
              </Badge>
              <Button
                className="h-10 bg-espresso px-6 text-sm font-black hover:bg-primary-container"
                type="submit"
              >
                Post
              </Button>
            </div>
          </header>

          <div className="flex-1 overflow-y-auto px-6 py-6">
            <section className="space-y-4">
              <div className="flex items-center justify-between text-sm text-espresso">
                <h3>Selected Photos ({composer.selectedPhotos.length})</h3>
                <Button
                  className="h-auto p-0 text-sm font-medium text-espresso"
                  type="button"
                  variant="link"
                >
                  Add more
                </Button>
              </div>

              <div className="grid grid-cols-3 gap-4">
                {composer.selectedPhotos.map((photo) => (
                  <div
                    className="relative aspect-square overflow-hidden rounded-md bg-surface-muted"
                    key={photo.id}
                  >
                    {photo.image ? (
                      <img
                        alt={photo.alt}
                        className="h-full w-full object-cover"
                        decoding="async"
                        src={photo.image}
                      />
                    ) : (
                      <div className="grid h-full w-full place-items-center bg-surface-muted text-muted">
                        <svg
                          aria-hidden="true"
                          className="size-7"
                          fill="none"
                          stroke="currentColor"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth="1.5"
                          viewBox="0 0 24 24"
                        >
                          <rect height="14" rx="1.5" width="16" x="4" y="5" />
                          <path d="m4 16 4.5-4.5 3.5 3.5 2-2 6 6" />
                          <path d="M14.5 9.5h.01" />
                        </svg>
                      </div>
                    )}
                    <Button
                      aria-label={`Remove ${photo.id}`}
                      className="absolute right-2 top-2 size-7 rounded-full bg-black/45 text-base leading-none text-white hover:bg-accent"
                      size="icon"
                      type="button"
                    >
                      x
                    </Button>
                  </div>
                ))}
              </div>
            </section>

            <label className="mt-8 flex flex-col gap-4">
              <span className="block text-sm text-espresso">Caption</span>
              <Textarea
                className="min-h-32 resize-none rounded-none border-line-soft bg-surface-muted px-4 py-4 focus:border-espresso"
                placeholder="Share your experience..."
              />
            </label>

            <section className="mt-8 space-y-4">
              <h3 className="text-sm text-espresso">Add People</h3>
              <div className="flex min-h-16 flex-wrap items-center gap-2 rounded-md bg-surface-muted px-4 py-3">
                {composer.taggedPeople.map((person) => (
                  <Badge
                    className="inline-flex items-center gap-2 rounded-full border border-line-soft bg-secondary px-3 py-1.5 text-sm font-semibold text-espresso"
                    key={person}
                  >
                    {person}
                    <Button
                      aria-label={`Remove ${person}`}
                      className="h-auto p-0 text-muted"
                      type="button"
                      variant="ghost"
                    >
                      x
                    </Button>
                  </Badge>
                ))}
                <Input
                  className="min-w-[160px] flex-1 border-0 bg-transparent p-0 text-sm outline-none placeholder:text-muted focus:ring-0"
                  placeholder="Search friends..."
                  type="text"
                />
              </div>
            </section>

            <section className="mt-8 space-y-4">
              <div className="flex items-center gap-2">
                <span className="text-lg text-espresso">*</span>
                <h3 className="text-sm text-espresso">Suggested Tags</h3>
              </div>
              <div className="flex flex-wrap gap-3">
                {composer.suggestedTags.map((tag) => (
                  <Button
                    className={`rounded-full px-4 py-1.5 text-sm font-black transition active:scale-95 ${
                      tag.selected
                        ? "bg-espresso text-white"
                        : "bg-secondary text-espresso hover:bg-line-soft"
                    }`}
                    key={tag.label}
                    type="button"
                    variant="ghost"
                  >
                    {tag.label}
                  </Button>
                ))}
              </div>
            </section>

            <footer className="mt-8 flex items-center justify-between border-t border-line-soft pt-5 text-xs text-muted">
              <p className="flex items-center gap-2">
                <span className="grid size-4 place-items-center rounded-full border border-muted text-[10px]">
                  i
                </span>
                Posts are visible to your followers immediately.
              </p>
              <p className="flex items-center gap-2">
                <span aria-hidden="true">o</span>
                Public
              </p>
            </footer>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
