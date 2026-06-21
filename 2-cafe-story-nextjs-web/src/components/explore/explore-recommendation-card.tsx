import Link from "next/link";
import { Award, Coffee, Sparkles, UserRound } from "lucide-react";
import { AvatarImage } from "@/components/ui/avatar-image";
import type { RecommendationCardResponse } from "@/types/recommendation";

type ExploreRecommendationCardProps = {
  item: RecommendationCardResponse;
};

function getTitle(item: RecommendationCardResponse) {
  if (item.targetType === "REVIEWER") {
    return item.username ? `@${item.username}` : (item.fullName ?? "Reviewer");
  }
  return item.fullName || item.username || "CafeStory pick";
}

function getSubtitle(item: RecommendationCardResponse) {
  if (item.targetType === "CAFE_PAGE") {
    return item.city ?? "Cafe suggestion";
  }
  if (item.targetType === "REVIEWER") {
    return item.fullName ?? "Reviewer";
  }
  return item.username ? `@${item.username}` : "CafeStory user";
}

function getIcon(item: RecommendationCardResponse) {
  if (item.targetType === "CAFE_PAGE") return Coffee;
  if (item.targetType === "REVIEWER") return Award;
  return UserRound;
}

function getHref(item: RecommendationCardResponse) {
  if (item.targetType === "CAFE_PAGE") {
    return `/cafes/${item.targetId}`;
  }

  const handle = item.username;
  if (handle) {
    return `/users/${handle}`;
  }

  return `/users/${item.userId ?? item.targetId}`;
}

export function ExploreRecommendationCard({ item }: ExploreRecommendationCardProps) {
  const Icon = getIcon(item);
  const reason = item.reason ?? "Recommended for you";

  return (
    <Link
      className="group flex items-center gap-4 rounded-md border border-border bg-surface p-4 no-underline transition-colors hover:border-primary/50"
      href={getHref(item)}
    >
      <div className="shrink-0 size-[52px] overflow-hidden rounded-full border border-border">
        <AvatarImage alt={getTitle(item)} src={item.avatar} />
      </div>

      <div className="min-w-0 flex-1 space-y-0.5">
        <div className="flex items-center gap-2">
          <span className="truncate text-sm font-black text-foreground group-hover:text-primary transition-colors">
            {getTitle(item)}
          </span>
          <span className="shrink-0 grid size-[22px] place-items-center rounded-full bg-primary/10">
            <Icon className="size-3 text-primary" strokeWidth={2.5} />
          </span>
        </div>

        <p className="truncate text-xs font-bold text-muted">
          {getSubtitle(item)}
        </p>

        <div className="flex items-start gap-1.5 pt-0.5">
          <Sparkles className="mt-px shrink-0 size-3 text-accent" strokeWidth={2.4} />
          <p className="line-clamp-2 text-xs font-bold leading-[1.4] text-accent/90">
            {reason}
          </p>
        </div>
      </div>
    </Link>
  );
}
