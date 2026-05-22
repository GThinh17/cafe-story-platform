import { CafeCard } from "@/components/cafe/cafe-card";
import { CafeSuggestionList } from "@/components/cafe/cafe-suggestion-list";
import { PageShell } from "@/components/layout/page-shell";
import { mockCafeSummaries } from "@/mocks/cafes";

const exploreTabs = ["Nearby", "Roasters", "Work", "Quiet", "New"];

export default function ExplorePage() {
  return (
    <PageShell
      title="Explore"
      description="Find cafes by mood, drink, and the kind of seat you need today."
      aside={<CafeSuggestionList cafes={mockCafeSummaries} />}
    >
      <section className="space-y-5">
        <label className="block">
          <span className="sr-only">Search cafes</span>
          <input
            className="h-12 w-full rounded-md border border-border bg-surface px-4 text-sm outline-none transition placeholder:text-muted focus:border-primary"
            placeholder="Search cafes, roasters, people"
            type="search"
          />
        </label>

        <div className="flex flex-wrap gap-2">
          {exploreTabs.map((tab) => (
            <button
              className="rounded-md border border-border bg-surface px-4 py-2 text-sm font-black text-foreground transition hover:border-primary hover:text-primary"
              key={tab}
              type="button"
            >
              {tab}
            </button>
          ))}
        </div>
      </section>

      <div className="space-y-6">
        {mockCafeSummaries.map((cafe) => (
          <CafeCard cafe={cafe} key={cafe.id} />
        ))}
      </div>
    </PageShell>
  );
}
