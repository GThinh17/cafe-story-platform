import { BookmarkIcon, Grid3X3Icon, UserSquare2Icon } from "lucide-react";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import type { ProfileReview } from "@/types/review";

type ProfileReviewGridProps = {
  reviews: ProfileReview[];
};

const tabTriggerClassName =
  "relative z-10 -mb-px h-14 -translate-y-3 rounded-none border-0 border-b-2 border-transparent bg-background p-0 text-muted shadow-none after:hidden data-active:bg-transparent data-active:text-primary data-active:shadow-none data-[state=active]:border-primary data-[state=active]:bg-background data-[state=active]:shadow-none hover:text-none hover:cursor-pointer";

export function ProfileReviewGrid({ reviews }: ProfileReviewGridProps) {
  return (
    <section className="flex flex-col">
      <Tabs defaultValue="posts">
        <TabsList
          className="relative z-10 grid h-16 w-full grid-cols-3 overflow-visible rounded-none bg-background p-0 text-muted"
          variant="line"
        >
          <TabsTrigger
            aria-label="Posts"
            className={tabTriggerClassName}
            value="posts"
          >
            <Grid3X3Icon className="size-6" strokeWidth={1.75} />
          </TabsTrigger>
          <TabsTrigger
            aria-label="Saved"
            className={tabTriggerClassName}
            value="saved"
          >
            <BookmarkIcon className="size-6" strokeWidth={1.75} />
          </TabsTrigger>
          <TabsTrigger
            aria-label="Tagged"
            className={tabTriggerClassName}
            value="tagged"
          >
            <UserSquare2Icon className="size-6" strokeWidth={1.75} />
          </TabsTrigger>
        </TabsList>
      </Tabs>

      <div className="relative z-0 mt-3 grid grid-cols-3 gap-1 border-t border-border pt-1">
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
