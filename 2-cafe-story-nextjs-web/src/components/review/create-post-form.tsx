import type { ReviewComposerModel, ReviewDraftHint } from "@/types/review";

type CreatePostFormProps = {
  composer: ReviewComposerModel;
  hints: ReviewDraftHint[];
};

const ratingValues = [1, 2, 3, 4, 5];

export function CreatePostForm({ composer, hints }: CreatePostFormProps) {
  return (
    <form className="overflow-hidden rounded-md border border-border bg-surface shadow-sm">
      <header className="flex items-start justify-between gap-4 border-b border-border px-6 py-5">
        <div className="min-w-0">
          <p className="text-xs font-black uppercase tracking-[0.12em] text-primary">
            Reviewer
          </p>
          <h1 className="mt-2 text-2xl font-black text-foreground">
            {composer.title}
          </h1>
          <p className="mt-1 max-w-[440px] text-sm leading-6 text-muted">
            {composer.subtitle}
          </p>
        </div>
        <span className="shrink-0 rounded-md bg-surface-muted px-3 py-2 text-xs font-black text-primary-strong">
          Draft
        </span>
      </header>

      <section className="space-y-6 p-6">
        <div className="overflow-hidden rounded-md border border-border bg-background">
          <img
            alt={`${composer.selectedCafe} cafe preview`}
            className="aspect-[16/10] w-full object-cover"
            decoding="async"
            src={composer.previewImage}
          />
          <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
            <div className="min-w-0">
              <p className="truncate text-sm font-black">
                {composer.selectedCafe}
              </p>
              <p className="text-xs text-muted">{composer.location}</p>
            </div>
            <button
              className="h-9 rounded-md border border-border bg-surface px-3 text-xs font-black transition hover:border-primary hover:text-primary"
              type="button"
            >
              Change photo
            </button>
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <label className="block space-y-2 sm:col-span-2">
            <span className="text-sm font-black">Cafe</span>
            <input
              className="h-12 w-full rounded-md border border-border bg-white px-4 text-sm outline-none focus:border-primary"
              defaultValue={composer.selectedCafe}
              type="text"
            />
          </label>

          <label className="block space-y-2">
            <span className="text-sm font-black">Visit type</span>
            <input
              className="h-12 w-full rounded-md border border-border bg-white px-4 text-sm outline-none focus:border-primary"
              defaultValue={composer.visitType}
              type="text"
            />
          </label>

          <label className="block space-y-2">
            <span className="text-sm font-black">Spend</span>
            <input
              className="h-12 w-full rounded-md border border-border bg-white px-4 text-sm outline-none focus:border-primary"
              defaultValue={composer.spend}
              type="text"
            />
          </label>
        </div>

        <section className="space-y-3">
          <p className="text-sm font-black">Rating</p>
          <div className="grid grid-cols-5 gap-2">
            {ratingValues.map((rating) => (
              <button
                className={`h-11 rounded-md border text-sm font-black transition ${
                  rating <= composer.rating
                    ? "border-rating bg-[#fff4d6] text-rating"
                    : "border-border bg-white text-muted hover:border-rating"
                }`}
                key={rating}
                type="button"
              >
                {rating}
              </button>
            ))}
          </div>
        </section>

        <label className="block space-y-2">
          <span className="text-sm font-black">Review</span>
          <textarea
            className="min-h-36 w-full resize-y rounded-md border border-border bg-white px-4 py-3 text-sm leading-6 outline-none focus:border-primary"
            defaultValue={composer.caption}
          />
        </label>

        <section className="space-y-3">
          <div className="flex items-center gap-2">
            <span className="grid h-6 w-6 place-items-center rounded-md bg-primary/10 text-xs font-black text-primary">
              AI
            </span>
            <h2 className="text-sm font-black">AI tags</h2>
          </div>
          <div className="flex flex-wrap gap-2">
            {composer.aiTags.map((tag) => (
              <button
                className="rounded-md bg-surface-muted px-3 py-2 text-xs font-black text-primary-strong transition hover:bg-border"
                key={tag}
                type="button"
              >
                {tag}
              </button>
            ))}
          </div>
        </section>

        <div className="grid grid-cols-2 gap-3">
          {hints.map((hint) => (
            <label className="block space-y-2" key={hint.label}>
              <span className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                {hint.label}
              </span>
              <input
                className="h-11 w-full rounded-md border border-border bg-white px-3 text-sm outline-none focus:border-primary"
                defaultValue={hint.value}
                type="text"
              />
            </label>
          ))}
        </div>

        <section className="flex flex-wrap items-center justify-between gap-3 rounded-md border border-border bg-background px-4 py-3">
          <div className="min-w-0">
            <p className="text-sm font-black">{composer.moderation.label}</p>
            <p className="text-xs text-muted">{composer.moderation.status}</p>
          </div>
          <span className="rounded-md bg-primary/10 px-3 py-2 text-xs font-black text-primary">
            Public
          </span>
        </section>
      </section>

      <footer className="flex flex-wrap items-center justify-end gap-3 border-t border-border bg-background px-6 py-4">
        <button
          className="h-11 rounded-md border border-border bg-surface px-4 text-sm font-black transition hover:border-primary hover:text-primary"
          type="button"
        >
          Save draft
        </button>
        <button
          className="h-11 rounded-md bg-primary px-5 text-sm font-black text-white hover:bg-primary-strong"
          type="submit"
        >
          Publish review
        </button>
      </footer>
    </form>
  );
}
