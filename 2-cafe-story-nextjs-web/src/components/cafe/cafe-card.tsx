import type { CafeSummary } from "@/types/cafe";

type CafeCardProps = {
  cafe: CafeSummary;
};

export function CafeCard({ cafe }: CafeCardProps) {
  return (
    <article className="overflow-hidden rounded-md border border-border bg-surface shadow-sm transition hover:border-primary/50">
      <a className="block no-underline" href={`/cafes/${cafe.id}`}>
        <img
          alt={`${cafe.name} cafe`}
          className="aspect-[16/10] w-full object-cover"
          decoding="async"
          loading="lazy"
          src={cafe.image}
        />
      </a>
      <div className="space-y-4 p-4">
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <a
              className="truncate text-lg font-black text-foreground no-underline hover:text-primary"
              href={`/cafes/${cafe.id}`}
            >
              {cafe.name}
            </a>
            <p className="mt-1 text-sm text-muted">
              {cafe.location} - {cafe.distance}
            </p>
          </div>
          <span className="rounded-md bg-[#fff4d6] px-2.5 py-1 text-sm font-black text-rating">
            {cafe.rating}
          </span>
        </div>

        <p className="text-sm leading-6 text-foreground">{cafe.description}</p>

        <div className="flex items-center gap-3 text-xs font-bold text-muted">
          <span>{cafe.reviewCount} reviews</span>
          <span>{cafe.priceLevel}</span>
          <span>{cafe.hours}</span>
        </div>

        <div className="flex flex-wrap gap-2">
          {cafe.tags.map((tag) => (
            <span
              className="rounded-md bg-surface-muted px-2.5 py-1 text-xs font-bold text-primary-strong"
              key={tag}
            >
              {tag}
            </span>
          ))}
        </div>
      </div>
    </article>
  );
}
