import type { TopCafe } from "@/types/feed";

type TopCafesNearbyProps = {
  cafes: TopCafe[];
};

export function TopCafesNearby({ cafes }: TopCafesNearbyProps) {
  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-base font-bold">Top Cafes Nearby</h2>
        <a className="text-xs font-bold text-foreground" href="#">
          See all
        </a>
      </div>
      <div className="space-y-4">
        {cafes.map((cafe) => (
          <a className="flex items-center gap-3" href="#" key={cafe.name}>
            <span className="h-11 w-11 rounded-full bg-surface-muted" />
            <span className="min-w-0 flex-1">
              <span className="block truncate text-sm font-bold">
                {cafe.name}
              </span>
              <span className="block text-xs font-medium text-muted">
                {cafe.rating} - {cafe.type}
              </span>
            </span>
            <span className="text-xs font-bold text-primary">View</span>
          </a>
        ))}
      </div>
    </div>
  );
}
