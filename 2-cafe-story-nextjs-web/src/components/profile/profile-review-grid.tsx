import type { ProfileReview } from "@/types/review";

type ProfileReviewGridProps = {
  reviews: ProfileReview[];
};

export function ProfileReviewGrid({ reviews }: ProfileReviewGridProps) {
  return (
    <section className="space-y-1">
      <div className="grid grid-cols-3 border-t border-border text-center text-xs font-black uppercase tracking-[0.1em] text-muted">
        <button
          className="border-t-2 border-foreground py-4 text-foreground"
          type="button"
        >
          Posts
        </button>
        <button className="py-4 transition hover:text-foreground" type="button">
          Saved
        </button>
        <button className="py-4 transition hover:text-foreground" type="button">
          Tagged
        </button>
      </div>

      <div className="grid grid-cols-3 gap-1">
        {reviews.map((review) => (
          <article
            className="group relative aspect-square overflow-hidden bg-surface-muted"
            key={review.id}
          >
            <img
              alt={`${review.cafe} review`}
              className="h-full w-full object-cover transition duration-200 group-hover:scale-105"
              decoding="async"
              loading="lazy"
              src={review.image}
            />
            <div className="absolute inset-0 flex items-end bg-gradient-to-t from-black/55 via-black/0 to-transparent p-2 opacity-0 transition group-hover:opacity-100">
              <div className="min-w-0 text-white">
                <p className="truncate text-xs font-black">{review.cafe}</p>
                <p className="text-xs">{review.rating}</p>
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
