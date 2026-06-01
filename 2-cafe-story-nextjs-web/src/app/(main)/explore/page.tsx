import { cookies } from "next/headers";
import { ExploreCafes } from "@/components/cafe/explore-cafes";
import { mapTopCafePagesToCafeSummaries } from "@/features/cafes/cafe-ranking-adapter";
import { ApiError } from "@/lib/api/client";
import { getTopCafePages } from "@/lib/api/cafes";
import { ACCESS_TOKEN_COOKIE } from "@/lib/routes";
import {
  mockCafeEditorialCollections,
  mockCafeSummaries,
} from "@/mocks/cafes";
import type { CafeSummary } from "@/types/cafe";

export const dynamic = "force-dynamic";

type ExplorePageProps = {
  searchParams: Promise<{
    city?: string | string[];
    regionId?: string | string[];
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
    return mockCafeSummaries;
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

    const mappedCafes = mapTopCafePagesToCafeSummaries(cafes);

    return mappedCafes.length > 0 ? mappedCafes : mockCafeSummaries;
  } catch (error) {
    if (error instanceof ApiError) {
      return mockCafeSummaries;
    }

    return mockCafeSummaries;
  }
}

export default async function ExplorePage({ searchParams }: ExplorePageProps) {
  const params = await searchParams;
  const city = firstParam(params.city);
  const regionId = firstParam(params.regionId);
  const cafes = await loadTopCafes(city, regionId);

  return (
    <main className="-ml-20 min-h-screen w-screen max-w-none touch-pan-y overflow-x-clip bg-background sm:-ml-28 xl:-ml-80">
      <ExploreCafes
        cafes={cafes}
        collections={mockCafeEditorialCollections}
      />
    </main>
  );
}
