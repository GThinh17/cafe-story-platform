import { ExploreSectionHeader } from "@/components/cafe/explore-section-header";
import { TrendingCafeCard } from "@/components/cafe/trending-cafe-card";
import type { CafeSummary } from "@/types/cafe";

type TrendingCafeSectionProps = {
  cafes: CafeSummary[];
};

export function TrendingCafeSection({ cafes }: TrendingCafeSectionProps) {
  return (
    <section className="space-y-7">
      <ExploreSectionHeader
        actionHref="/explore"
        actionLabel="View all"
        title="Trending Near You"
      />

      <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
        {cafes.map((cafe) => (
          <TrendingCafeCard cafe={cafe} key={cafe.id} />
        ))}
      </div>
    </section>
  );
}
