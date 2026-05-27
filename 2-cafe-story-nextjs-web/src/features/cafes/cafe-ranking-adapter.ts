import type { CafePageRankingResponse, CafeSummary } from "@/types/cafe";

const fallbackCafeImages = [
  "/images/cafes/velvet-roast/latte-art.jpg",
  "/images/cafes/velvet-roast/minimal-interior.jpg",
  "/images/cafes/velvet-roast/espresso-machine.jpg",
  "/images/cafes/velvet-roast/croissant-flatlay.jpg",
];

function formatCount(value: number | null | undefined) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value ?? 0);
}

function formatLocation(cafe: CafePageRankingResponse) {
  return [cafe.regionArea, cafe.regionCity, cafe.regionProvince]
    .filter(Boolean)
    .join(", ") || cafe.address;
}

function buildTags(cafe: CafePageRankingResponse) {
  return [
    cafe.rankPosition ? `Rank #${cafe.rankPosition}` : "Regional pick",
    cafe.regionCity,
    `${formatCount(cafe.followerCount)} followers`,
  ].filter(Boolean) as string[];
}

export function mapTopCafePagesToCafeSummaries(
  cafes: CafePageRankingResponse[],
): CafeSummary[] {
  return cafes.map((cafe, index) => ({
    id: cafe.id,
    name: cafe.name,
    location: formatLocation(cafe),
    address: cafe.address,
    type: "Regional favorite",
    rating: cafe.rankPosition ? `#${cafe.rankPosition}` : "Top",
    reviewCount: `${formatCount(cafe.followerCount)} followers`,
    distance: "Regional pick",
    priceLevel: "",
    hours: cafe.pageActive ? "Active page" : "Cafe Story",
    status: cafe.status,
    photoCount: `${formatCount(cafe.likeCount)} likes`,
    image:
      cafe.coverUrl ??
      cafe.avatarUrl ??
      fallbackCafeImages[index % fallbackCafeImages.length],
    gallery: [
      cafe.coverUrl,
      cafe.avatarUrl,
      fallbackCafeImages[index % fallbackCafeImages.length],
    ].filter(Boolean) as string[],
    tags: buildTags(cafe),
    amenities: buildTags(cafe),
    popularDrinks: ["View page", "Follow", "Stories"],
    description:
      cafe.description?.trim() ||
      `${cafe.name} is trending in ${cafe.regionCity ?? "Cafe Story"}.`,
    featureSummary:
      cafe.description?.trim() ||
      `${cafe.name} is a regional cafe pick based on current community signals.`,
  }));
}
