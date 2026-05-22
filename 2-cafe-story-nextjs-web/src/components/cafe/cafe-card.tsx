import type { CafeSummary } from "@/types/cafe";

type CafeCardProps = {
  cafe: CafeSummary;
};

export function CafeCard({ cafe }: CafeCardProps) {
  return (
    <article className="overflow-hidden rounded-md border border-border bg-surface shadow-sm">
      <img
        alt={`${cafe.name} cafe`}
        className="aspect-[16/10] w-full object-cover"
        decoding="async"
        loading="lazy"
        src={cafe.image}
      />
      <div className="space-y-4 p-4">
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <h2 className="truncate text-lg font-black">{cafe.name}</h2>
            <p className="mt-1 text-sm text-muted">
              {cafe.location} - {cafe.distance}
            </p>
          </div>
          <span className="rounded-md bg-[#fff4d6] px-2.5 py-1 text-sm font-black text-rating">
            {cafe.rating}
          </span>
        </div>

        <p className="text-sm leading-6 text-foreground">{cafe.description}</p>

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
