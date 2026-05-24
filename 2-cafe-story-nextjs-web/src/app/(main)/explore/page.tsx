import { ExploreCafes } from "@/components/cafe/explore-cafes";
import { mockCafeSummaries, mockFeaturedCafe } from "@/mocks/cafes";

export default function ExplorePage() {
  return (
    <ExploreCafes cafes={mockCafeSummaries} featuredCafe={mockFeaturedCafe} />
  );
}
