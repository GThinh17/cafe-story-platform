import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardTitle } from "@/components/ui/card";
import { ExploreIcon } from "@/components/cafe/explore-icon";
import type { CafeSummary } from "@/types/cafe";

type TrendingCafeCardProps = {
  cafe: CafeSummary;
};

export function TrendingCafeCard({ cafe }: TrendingCafeCardProps) {
  return (
    <Card className="group min-w-0 border-0 bg-transparent shadow-none">
      <Link className="block no-underline" href={`/cafes/${cafe.id}`}>
        <div className="relative mb-4 aspect-[4/5] overflow-hidden rounded-md bg-surface-muted">
          <img
            alt={`${cafe.name} cafe`}
            className="h-full w-full object-cover transition duration-700 group-hover:scale-105"
            decoding="async"
            loading="lazy"
            src={cafe.image}
          />
          <Badge
            className="absolute right-3 top-3 gap-1 rounded-sm bg-surface/95 px-2 py-1 text-xs font-black text-espresso shadow-sm"
            variant="outline"
          >
            <ExploreIcon className="size-3.5" name="star" />
            {cafe.rating}
          </Badge>
        </div>

        <CardTitle className="font-sans text-xl font-bold text-espresso transition group-hover:text-primary">
          {cafe.name}
        </CardTitle>
      </Link>
      <CardContent className="p-0">
        <p className="mt-2 min-h-12 text-sm leading-6 text-coffee-muted">
          &quot;{cafe.description}&quot;
        </p>
        <p className="mt-3 flex items-center gap-1 text-xs font-medium text-muted">
          <ExploreIcon className="size-3.5" name="pin" />
          {cafe.location}
        </p>
      </CardContent>
    </Card>
  );
}
