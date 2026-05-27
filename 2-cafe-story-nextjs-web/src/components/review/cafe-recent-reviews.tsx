import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import type { CafeReviewPost } from "@/types/review";

type CafeRecentReviewsProps = {
  reviews: CafeReviewPost[];
};

function ReviewActionIcon({ name }: { name: "heart" | "comment" | "save" }) {
  const paths: Record<typeof name, string[]> = {
    heart: [
      "M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8Z",
    ],
    comment: [
      "M4 5.5h16v11H8l-4 4v-15Z",
      "M8 9h8",
      "M8 13h5",
    ],
    save: ["M6 4h12v17l-6-3-6 3V4Z"],
  };

  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.6"
      viewBox="0 0 24 24"
    >
      {paths[name].map((path) => (
        <path d={path} key={path} />
      ))}
    </svg>
  );
}

export function CafeRecentReviews({ reviews }: CafeRecentReviewsProps) {
  if (reviews.length === 0) {
    return null;
  }

  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-end justify-between gap-4">
        <h2 className="font-serif text-2xl font-medium text-espresso">
          Recent Reviews
        </h2>
        <div className="flex items-center gap-5 text-xs font-medium text-coffee-muted">
          <Button
            className="h-auto border-b border-espresso p-0 pb-1 text-xs font-medium text-espresso"
            type="button"
            variant="ghost"
          >
            Feed
          </Button>
          <Button
            className="h-auto p-0 pb-1 text-xs font-medium text-coffee-muted hover:text-espresso"
            type="button"
            variant="ghost"
          >
            Map View
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        {reviews.map((review) => (
          <Card
            className="overflow-hidden border-line-soft bg-surface shadow-none"
            key={review.id}
          >
            <div className="relative">
              <img
                alt={`${review.cafe} review`}
                className="aspect-square w-full object-cover"
                decoding="async"
                loading="lazy"
                src={review.image}
              />
              <Badge className="absolute right-3 top-3 rounded-sm bg-surface px-2 py-1 text-xs font-black text-espresso shadow-sm">
                * {review.rating}
              </Badge>
            </div>

            <CardContent className="flex flex-col gap-4 p-4">
              <div className="flex flex-col gap-1">
                <h3 className="font-serif text-base font-medium text-espresso">
                  {review.cafe}
                </h3>
                <p className="text-xs font-medium uppercase tracking-[0.03em] text-coffee-muted">
                  {review.neighborhood}
                </p>
              </div>

              <p className="h-12 overflow-hidden text-sm italic leading-6 text-coffee-muted">
                "{review.excerpt}"
              </p>

              <div className="flex items-center justify-between pt-1 text-espresso">
                <div className="flex items-center gap-4">
                  <Button
                    aria-label="Like review"
                    className="h-auto p-0 text-espresso"
                    type="button"
                    variant="ghost"
                  >
                    <ReviewActionIcon name="heart" />
                  </Button>
                  <Button
                    aria-label="Comment on review"
                    className="h-auto p-0 text-espresso"
                    type="button"
                    variant="ghost"
                  >
                    <ReviewActionIcon name="comment" />
                  </Button>
                </div>
                <Button
                  aria-label="Save review"
                  className="h-auto p-0 text-espresso"
                  type="button"
                  variant="ghost"
                >
                  <ReviewActionIcon name="save" />
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
