"use client";

import { useState } from "react";
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
  searchQuery?: string;
};

const CATEGORY_KEYWORDS: Record<string, string[]> = {
  "vintage-vibes": ["vintage", "retro", "cozy", "aesthetic"],
  "workspace-ready": ["work", "wifi", "laptop", "quiet"],
  "specialty-brews": ["specialty", "brew", "espresso", "roast", "pour", "filter"],
  "hidden-gems": ["hidden", "gem", "local"],
  "outdoor-patios": ["outdoor", "patio", "garden", "terrace", "seating"],
};

function cafeMatchesCategory(cafe: CafeSummary, categoryId: string): boolean {
  const keywords = CATEGORY_KEYWORDS[categoryId];
  if (!keywords) return true;
  const haystack = [...cafe.tags, cafe.type, cafe.name].join(" ").toLowerCase();
  return keywords.some((kw) => haystack.includes(kw));
}

export function ExploreCafes({ cafes, collections, searchQuery = "" }: ExploreCafesProps) {
  const [activeCategoryId, setActiveCategoryId] = useState<string | undefined>(undefined);

  const filteredCafes = cafes.filter((cafe) => {
    const matchesSearch = !searchQuery || cafe.name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = !activeCategoryId || cafeMatchesCategory(cafe, activeCategoryId);
    return matchesSearch && matchesCategory;
  });

  const trendingCafes = filteredCafes.slice(0, 3);

  return (
    <div className="w-full overflow-x-clip px-4 py-12 sm:px-8 xl:px-12">
      <div className="mx-auto max-w-[1140px] space-y-16">
        <ExploreSearchHeader />

        <section className="space-y-14">
          <CafeCategoryList
            activeCategoryId={activeCategoryId}
            categories={mockCafeCategories}
            onSelect={setActiveCategoryId}
          />
          <TrendingCafeSection cafes={trendingCafes} />
        </section>

        <EditorialCollectionsSection collections={collections} />
        <CafeFooter />
      </div>
    </div>
  );
}
