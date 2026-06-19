import { cookies } from "next/headers";
import { ExploreCafes } from "@/components/cafe/explore-cafes";
import { mapTopCafePagesToCafeSummaries } from "@/features/cafes/cafe-ranking-adapter";
import { getTopCafePages } from "@/lib/api/cafes";
import { ACCESS_TOKEN_COOKIE } from "@/lib/routes";
import { mockCafeEditorialCollections } from "@/mocks/cafes";
import type { CafeSummary } from "@/types/cafe";

export const dynamic = "force-dynamic";

type ExplorePageProps = {
  searchParams: Promise<{
    city?: string | string[];
    regionId?: string | string[];
    query?: string | string[];
  }>;
};

function firstParam(value: string | string[] | undefined) {
  if (Array.isArray(value)) {
    return value[0];
  }

  return value;
}

async function loadTopCafes(
  city: string | undefined,
  regionId: string | undefined,
): Promise<CafeSummary[]> {
  const cookieStore = await cookies();

  if (!cookieStore.has(ACCESS_TOKEN_COOKIE)) {
    return [];
  }

  try {
    const cafes = await getTopCafePages(
      {
        city,
        regionId,
        size: 6,
      },
      {
        headers: {
          Cookie: cookieStore.toString(),
        },
      },
    );

    return mapTopCafePagesToCafeSummaries(cafes);
  } catch {
    return [];
  }
}

export default async function ExplorePage({ searchParams }: ExplorePageProps) {
  const params = await searchParams;
  const city = firstParam(params.city);
  const regionId = firstParam(params.regionId);
  const query = firstParam(params.query);
  const cafes = await loadTopCafes(city, regionId);

  return (
    <main className="min-h-screen w-screen max-w-none touch-pan-y overflow-x-clip bg-background sm:-ml-28 xl:-ml-80">
      <ExploreCafes
        cafes={cafes}
        collections={mockCafeEditorialCollections}
        searchQuery={query}
      />
    </main>
  );
}
