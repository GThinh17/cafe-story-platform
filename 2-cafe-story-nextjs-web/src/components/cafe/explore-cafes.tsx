import { CafeCategoryList } from "@/components/cafe/cafe-category-list";
import { CafeFooter } from "@/components/cafe/cafe-footer";
import { EditorialCollectionsSection } from "@/components/cafe/editorial-collections-section";
import { ExploreSearchHeader } from "@/components/cafe/explore-search-header";
import { TrendingCafeSection } from "@/components/cafe/trending-cafe-section";
import { mockCafeCategories } from "@/mocks/cafes";
import type { CafeEditorialCollection, CafeSummary } from "@/types/cafe";

type ExploreCafesProps = {
  cafes: CafeSummary[];
  collections: CafeEditorialCollection[];
};

export function ExploreCafes({ cafes, collections }: ExploreCafesProps) {
  const trendingCafes = cafes.slice(0, 3);

  return (
    <div className="w-full overflow-x-clip px-4 py-12 sm:px-8 xl:px-12">
      <div className="mx-auto max-w-[1140px] space-y-16">
        <ExploreSearchHeader />

        <section className="space-y-14">
          <CafeCategoryList categories={mockCafeCategories} />
          <TrendingCafeSection cafes={trendingCafes} />
        </section>

        <EditorialCollectionsSection collections={collections} />
        <CafeFooter />
      </div>
    </div>
  );
}
