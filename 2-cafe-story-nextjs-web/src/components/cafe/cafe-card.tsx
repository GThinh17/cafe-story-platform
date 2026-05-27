import type { CafeSummary } from "@/types/cafe";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

type CafeCardProps = {
  cafe: CafeSummary;
};

export function CafeCard({ cafe }: CafeCardProps) {
  return (
    <Card className="overflow-hidden transition hover:border-primary/50">
      <a className="block no-underline" href={`/cafes/${cafe.id}`}>
        <img
          alt={`${cafe.name} cafe`}
          className="aspect-[16/10] w-full object-cover"
          decoding="async"
          loading="lazy"
          src={cafe.image}
        />
      </a>
      <CardHeader className="flex flex-row items-start justify-between gap-4 p-4">
        <div className="min-w-0">
          <CardTitle className="truncate text-lg font-black">
            <a
              className="text-foreground no-underline hover:text-primary"
              href={`/cafes/${cafe.id}`}
            >
              {cafe.name}
            </a>
          </CardTitle>
          <CardDescription className="mt-1 text-sm">
            {cafe.location} - {cafe.distance}
          </CardDescription>
        </div>
        <Badge className="text-sm font-black" variant="rating">
          {cafe.rating}
        </Badge>
      </CardHeader>

      <CardContent className="flex flex-col gap-4 p-4 pt-0">
        <p className="text-sm leading-6 text-foreground">{cafe.description}</p>

        <div className="flex items-center gap-3 text-xs font-bold text-muted">
          <span>{cafe.reviewCount} reviews</span>
          <span>{cafe.priceLevel}</span>
          <span>{cafe.hours}</span>
        </div>

        <div className="flex flex-wrap gap-2">
          {cafe.tags.map((tag) => (
            <Badge className="font-bold" key={tag} variant="secondary">
              {tag}
            </Badge>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
