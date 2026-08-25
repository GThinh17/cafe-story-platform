"use client";

import { StarIcon } from "lucide-react";
import { useState } from "react";
import { useI18n } from "@/components/providers/locale-provider";
import { rateCafePage } from "@/lib/api/cafes";
import { cn } from "@/lib/utils";

const STARS = [1, 2, 3, 4, 5] as const;

type CafeRatingControlProps = {
  cafeId: string;
  myRating?: number | null;
  ratingCount?: number | null;
  ratingScore?: number | null;
  onRatingChange?: (next: {
    isRating: boolean;
    myRating: number;
    ratingCount: number | null;
    ratingScore: number | null;
  }) => void;
};

function formatScore(score: number | null | undefined, fallback: string) {
  return typeof score === "number" && score > 0 ? score.toFixed(1) : fallback;
}

/**
 * Interactive 1-5 rating. `PUT /api/cafe-pages/{id}/rating` is an upsert and
 * returns the recomputed average and count, so the aggregate updates from the
 * response rather than a refetch. Caramel Crack is the palette's rating-only
 * colour, so it is the correct signal here.
 */
export function CafeRatingControl({
  cafeId,
  myRating = null,
  ratingCount = null,
  ratingScore = null,
  onRatingChange,
}: CafeRatingControlProps) {
  const { t } = useI18n();
  const [hovered, setHovered] = useState<number | null>(null);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState(false);

  // Hover preview wins, then the viewer's own rating; the average is only the
  // headline number, never the star fill.
  const filledUpTo = hovered ?? myRating ?? 0;

  async function submit(rating: number) {
    if (isPending) return;

    setIsPending(true);
    setError(false);
    try {
      const response = await rateCafePage(cafeId, rating);
      onRatingChange?.({
        isRating: true,
        myRating: response.rating,
        ratingCount: response.ratingCount,
        ratingScore: response.ratingAverage,
      });
    } catch {
      setError(true);
    } finally {
      setIsPending(false);
    }
  }

  return (
    <div className="flex h-12 flex-col justify-center gap-0.5 px-2">
      <div
        className="flex items-center gap-1"
        onMouseLeave={() => setHovered(null)}
      >
        {STARS.map((star) => (
          <button
            aria-label={t("cafe.rating.rateStars", { count: String(star) })}
            aria-pressed={myRating === star}
            className="cursor-pointer rounded-sm p-0.5 outline-none transition disabled:cursor-not-allowed disabled:opacity-50"
            disabled={isPending}
            key={star}
            onClick={() => void submit(star)}
            onFocus={() => setHovered(star)}
            onMouseEnter={() => setHovered(star)}
            type="button"
          >
            <StarIcon
              className={cn(
                "size-5 transition",
                star <= filledUpTo
                  ? "fill-rating text-rating"
                  : "text-muted",
              )}
            />
          </button>
        ))}
        <span className="ml-1 text-sm font-black text-espresso">
          {formatScore(ratingScore, t("cafe.newRating"))}
        </span>
      </div>
      <p className="px-0.5 text-xs font-medium text-muted">
        {error
          ? t("cafe.rating.error")
          : myRating
            ? t("cafe.rating.yours", { count: String(myRating) })
            : t("cafe.ratingCount", {
                count: new Intl.NumberFormat("en", {
                  notation: "compact",
                  maximumFractionDigits: 1,
                }).format(ratingCount ?? 0),
              })}
      </p>
    </div>
  );
}
