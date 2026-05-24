import { CafeCard } from "@/components/cafe/cafe-card";
import { CafePage } from "@/components/cafe/cafe-page";
import { CafeSuggestionList } from "@/components/cafe/cafe-suggestion-list";
import { PageShell } from "@/components/layout/page-shell";
import type { CafeSummary } from "@/types/cafe";

type ExploreCafesProps = {
  cafes: CafeSummary[];
  featuredCafe: CafeSummary;
};

const exploreTabs = ["Nearby", "Roasters", "Work", "Quiet", "New"];

export function ExploreCafes({ cafes, featuredCafe }: ExploreCafesProps) {
  return (
    <PageShell
      title="Explore"
      description="Find cafes by mood, drink, and the kind of seat you need today."
      aside={<CafeSuggestionList cafes={cafes} />}
    >
      <section className="space-y-5 rounded-md border border-border bg-surface p-4 shadow-sm">
        <label className="block">
          <span className="sr-only">Search cafes</span>
          <input
            className="h-12 w-full rounded-md border border-border bg-background px-4 text-sm outline-none transition placeholder:text-muted focus:border-primary"
            placeholder="Search cafes, roasters, people"
            type="search"
          />
        </label>

        <div className="flex flex-wrap gap-2">
          {exploreTabs.map((tab, index) => (
            <button
              className={`rounded-md border px-4 py-2 text-sm font-black transition ${
                index === 0
                  ? "border-primary bg-primary text-white"
                  : "border-border bg-surface hover:border-primary hover:text-primary"
              }`}
              key={tab}
              type="button"
            >
              {tab}
            </button>
          ))}
        </div>
      </section>

      <CafePage cafe={featuredCafe} />

      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-black">More cafes nearby</h2>
          <a className="text-sm font-black text-primary" href="/explore">
            View map
          </a>
        </div>

        <div className="grid gap-5">
          {cafes.slice(1).map((cafe) => (
            <CafeCard cafe={cafe} key={cafe.id} />
          ))}
        </div>
      </section>
    </PageShell>
  );
}
