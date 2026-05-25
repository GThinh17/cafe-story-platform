import { ExploreCafes } from "@/components/cafe/explore-cafes";
import {
  mockCafeEditorialCollections,
  mockCafeSummaries,
} from "@/mocks/cafes";

export default function ExplorePage() {
  return (
    <main className="-ml-8 min-h-screen w-[calc(100vw-64px)] max-w-none touch-pan-y overflow-x-clip bg-background sm:-ml-14 sm:w-[calc(100vw-72px)] xl:-ml-[248px]">
      <ExploreCafes
        cafes={mockCafeSummaries}
        collections={mockCafeEditorialCollections}
      />
    </main>
  );
}
