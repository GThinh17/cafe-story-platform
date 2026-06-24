import Link from "next/link";
import { Award } from "lucide-react";
import { AvatarImage } from "@/components/ui/avatar-image";
import type { RecommendationCardResponse } from "@/types/recommendation";

type ExploreReviewerListProps = {
  emptyDescription?: string;
  emptyTitle?: string;
  error?: string;
  isLoading: boolean;
  items: RecommendationCardResponse[];
};

function getHref(item: RecommendationCardResponse) {
  if (item.username) return `/users/${item.username}`;
  if (item.userId) return `/users/${item.userId}`;
  return `/users/${item.targetId}`;
}

function ReviewerListItem({ item }: { item: RecommendationCardResponse }) {
  const name = item.fullName || item.username || "Reviewer";
  const handle = item.username ? `@${item.username}` : null;
  const sub = handle ?? item.reason ?? "Reviewer";

  return (
    <Link
      className="group flex items-center gap-3 py-3 no-underline"
      href={getHref(item)}
    >
      <div className="shrink-0 size-11 overflow-hidden rounded-full border border-border bg-surface-muted">
        <AvatarImage alt={name} src={item.avatar} />
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-1.5">
          <span className="truncate text-sm font-black text-foreground group-hover:text-primary transition-colors">
            {name}
          </span>
          {item.targetType === "REVIEWER" && (
            <Award className="shrink-0 size-3.5 text-primary" strokeWidth={2.5} />
          )}
        </div>
        <p className="truncate text-xs text-muted">{sub}</p>
      </div>
    </Link>
  );
}

function SkeletonRow() {
  return (
    <div className="flex items-center gap-3 py-3 animate-pulse">
      <div className="shrink-0 size-11 rounded-full bg-surface-muted" />
      <div className="flex-1 space-y-2">
        <div className="h-3 w-28 rounded bg-surface-muted" />
        <div className="h-2.5 w-20 rounded bg-surface-muted" />
      </div>
    </div>
  );
}

export function ExploreReviewerList({
  emptyDescription,
  emptyTitle = "No reviewers yet",
  error,
  isLoading,
  items,
}: ExploreReviewerListProps) {
  if (isLoading) {
    return (
      <div className="mx-auto max-w-sm">
        {Array.from({ length: 8 }).map((_, i) => (
          <SkeletonRow key={i} />
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
    <div className="mx-auto max-w-sm">
      {items.map((item) => (
        <ReviewerListItem item={item} key={item.targetId} />
      ))}
    </div>
  );
}
