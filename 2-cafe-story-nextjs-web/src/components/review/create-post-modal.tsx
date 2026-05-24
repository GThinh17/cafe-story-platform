"use client";

import { useEffect } from "react";
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
    <div
      aria-modal="true"
      className="fixed inset-0 z-[70] grid place-items-start justify-center overflow-y-auto bg-black/20 px-4 py-12"
      role="dialog"
    >
      <button
        aria-label="Close create post modal"
        className="absolute inset-0 h-full w-full cursor-default"
        onClick={onClose}
        type="button"
      />

      <form
        className="relative z-10 flex w-full max-w-[672px] flex-col overflow-hidden rounded-md border border-line-soft bg-white shadow-[0_26px_80px_rgba(39,19,16,0.22)]"
        onSubmit={(event) => {
          event.preventDefault();
          onClose();
        }}
      >
        <header className="flex h-[72px] items-center justify-between border-b border-line-soft px-6">
          <div className="flex items-center gap-4">
            <button
              aria-label="Close"
              className="grid h-8 w-8 place-items-center rounded-full text-xl leading-none transition hover:bg-surface-muted"
              onClick={onClose}
              type="button"
            >
              x
            </button>
            <h2 className="font-serif text-xl font-semibold text-espresso">
              {composer.title}
            </h2>
          </div>

          <div className="flex items-center gap-4">
            <span className="hidden items-center gap-2 rounded-full bg-[#d0e8de] px-4 py-1.5 text-[10px] font-black uppercase tracking-[0.08em] text-[#486058] sm:flex">
              <span className="grid h-3.5 w-3.5 place-items-center rounded-full border border-[#486058] text-[9px] leading-none">
                ✓
              </span>
              Ready to post
            </span>
            <button
              className="h-10 rounded-md bg-espresso px-6 text-sm font-black text-white transition hover:bg-primary-container"
              type="submit"
            >
              Post
            </button>
          </div>
        </header>

        <div className="max-h-[calc(100vh-144px)] overflow-y-auto px-6 py-6">
          <section className="space-y-4">
            <div className="flex items-center justify-between text-sm text-espresso">
              <h3>Selected Photos ({composer.selectedPhotos.length})</h3>
              <button
                className="text-sm font-medium text-espresso hover:underline"
                type="button"
              >
                Add more
              </button>
            </div>

            <div className="grid grid-cols-3 gap-4">
              {composer.selectedPhotos.map((photo) => (
                <div
                  className="relative aspect-square overflow-hidden rounded-md bg-[#f3f3f3]"
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
                    <div className="grid h-full w-full place-items-center bg-[#f3f3f3] text-[#b7b7b7]">
                      <svg
                        aria-hidden="true"
                        className="h-7 w-7"
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
                  <button
                    aria-label={`Remove ${photo.id}`}
                    className="absolute right-2 top-2 grid h-7 w-7 place-items-center rounded-full bg-black/45 text-base leading-none text-white transition hover:bg-error"
                    type="button"
                  >
                    x
                  </button>
                </div>
              ))}
            </div>
          </section>

          <label className="mt-8 block space-y-4">
            <span className="block text-sm text-espresso">Caption</span>
            <textarea
              className="min-h-32 w-full resize-none rounded-none border border-line-soft bg-[#f3f3f3] px-4 py-4 text-sm leading-6 outline-none transition placeholder:text-muted focus:border-espresso"
              placeholder="Share your experience..."
            />
          </label>

          <section className="mt-8 space-y-4">
            <h3 className="text-sm text-espresso">Add People</h3>
            <div className="flex min-h-16 flex-wrap items-center gap-2 rounded-md bg-[#f3f3f3] px-4 py-3">
              {composer.taggedPeople.map((person) => (
                <span
                  className="inline-flex items-center gap-2 rounded-full border border-line-soft bg-[#e8e8e8] px-3 py-1.5 text-sm font-semibold text-espresso"
                  key={person}
                >
                  {person}
                  <button
                    aria-label={`Remove ${person}`}
                    className="text-muted"
                    type="button"
                  >
                    x
                  </button>
                </span>
              ))}
              <input
                className="min-w-[160px] flex-1 border-0 bg-transparent p-0 text-sm outline-none placeholder:text-muted focus:ring-0"
                placeholder="Search friends..."
                type="text"
              />
            </div>
          </section>

          <section className="mt-8 space-y-4">
            <div className="flex items-center gap-2">
              <span className="text-lg text-espresso">✦</span>
              <h3 className="text-sm text-espresso">Suggested Tags</h3>
            </div>
            <div className="flex flex-wrap gap-3">
              {composer.suggestedTags.map((tag) => (
                <button
                  className={`rounded-full px-4 py-1.5 text-sm font-black transition active:scale-95 ${
                    tag.selected
                      ? "bg-espresso text-white"
                      : "bg-[#e8e8e8] text-espresso hover:bg-line-soft"
                  }`}
                  key={tag.label}
                  type="button"
                >
                  {tag.label}
                </button>
              ))}
            </div>
          </section>

          <footer className="mt-8 flex items-center justify-between border-t border-line-soft pt-5 text-xs text-muted">
            <p className="flex items-center gap-2">
              <span className="grid h-4 w-4 place-items-center rounded-full border border-muted text-[10px]">
                i
              </span>
              Posts are visible to your followers immediately.
            </p>
            <p className="flex items-center gap-2">
              <span aria-hidden="true">⊙</span>
              Public
            </p>
          </footer>
        </div>
      </form>
    </div>
  );
}
