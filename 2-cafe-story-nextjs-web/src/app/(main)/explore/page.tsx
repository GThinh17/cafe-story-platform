import { ExploreContent } from "@/components/explore/explore-content";

export const dynamic = "force-dynamic";

export default function ExplorePage() {
  return (
    <main className="min-h-screen w-screen max-w-none touch-pan-y overflow-x-clip bg-background sm:-ml-28 xl:-ml-80">
      <ExploreContent />
    </main>
  );
}
