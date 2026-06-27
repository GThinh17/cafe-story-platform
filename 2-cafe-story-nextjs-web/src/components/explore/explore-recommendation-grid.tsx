import { ExploreRecommendationCard } from "@/components/explore/explore-recommendation-card";
import type { RecommendationCardResponse } from "@/types/recommendation";

type ExploreRecommendationGridProps = {
  emptyDescription?: string;
  emptyTitle?: string;
  error?: string;
  isLoading: boolean;
  items: RecommendationCardResponse[];
};

function SkeletonCard() {
  return (
    <div className="flex items-center gap-4 rounded-md border border-border bg-surface p-4 animate-pulse">
      <div className="shrink-0 size-[52px] rounded-full bg-surface-muted" />
      <div className="flex-1 space-y-2">
        <div className="h-3 w-2/3 rounded bg-surface-muted" />
        <div className="h-2.5 w-1/2 rounded bg-surface-muted" />
        <div className="h-2.5 w-3/4 rounded bg-surface-muted" />
      </div>
    </div>
  );
}

export function ExploreRecommendationGrid({
  emptyDescription,
  emptyTitle = "No recommendations yet",
  error,
  isLoading,
  items,
}: ExploreRecommendationGridProps) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <SkeletonCard key={i} />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center gap-2 py-16 text-center">
        <p className="text-sm font-semibold text-foreground">{error}</p>
        <p className="text-xs text-muted">Try refreshing the page.</p>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="flex flex-col items-center gap-2 py-16 text-center">
        <p className="text-sm font-semibold text-foreground">{emptyTitle}</p>
        {emptyDescription ? (
          <p className="max-w-xs text-xs text-muted">{emptyDescription}</p>
        ) : null}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
      {items.map((item) => (
        <ExploreRecommendationCard item={item} key={item.targetId} />
      ))}
    </div>
  );
}
