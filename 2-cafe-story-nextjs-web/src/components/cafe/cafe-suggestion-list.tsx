import type { CafeSummary } from "@/types/cafe";

type CafeSuggestionListProps = {
  cafes: CafeSummary[];
};

export function CafeSuggestionList({ cafes }: CafeSuggestionListProps) {
  return (
    <section className="space-y-5">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-black">Top matches</h2>
        <a className="text-xs font-black text-primary" href="/explore">
          See all
        </a>
      </div>
      <div className="space-y-4">
        {cafes.slice(0, 3).map((cafe) => (
          <a
            className="flex items-center gap-3 rounded-md no-underline transition hover:bg-surface-muted"
            href="#"
            key={cafe.id}
          >
            <img
              alt=""
              className="h-12 w-12 rounded-md object-cover"
              decoding="async"
              loading="lazy"
              src={cafe.image}
            />
            <span className="min-w-0">
              <span className="block truncate text-sm font-black text-foreground">
                {cafe.name}
              </span>
              <span className="block truncate text-xs text-muted">
                {cafe.rating} - {cafe.type}
              </span>
            </span>
          </a>
        ))}
      </div>
    </section>
  );
}
