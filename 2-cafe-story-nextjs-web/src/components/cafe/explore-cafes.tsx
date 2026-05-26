import type { CafeEditorialCollection, CafeSummary } from "@/types/cafe";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";

type ExploreCafesProps = {
  cafes: CafeSummary[];
  collections: CafeEditorialCollection[];
};

type ExploreIconName =
  | "cup"
  | "gem"
  | "leaf"
  | "laptop"
  | "pin"
  | "search"
  | "sparkle"
  | "star";

const exploreFilters: {
  icon: ExploreIconName;
  label: string;
}[] = [
  { icon: "sparkle", label: "Vintage vibes" },
  { icon: "laptop", label: "Workspace ready" },
  { icon: "cup", label: "Specialty brews" },
  { icon: "gem", label: "Hidden gems" },
  { icon: "leaf", label: "Outdoor patios" },
];

function ExploreIcon({ name }: { name: ExploreIconName }) {
  const paths: Record<ExploreIconName, string[]> = {
    cup: [
      "M5 8h11v5a5 5 0 0 1-5 5H10a5 5 0 0 1-5-5V8Z",
      "M16 10h2a3 3 0 0 1 0 6h-2",
      "M4 21h14",
    ],
    gem: ["M6 3h12l4 6-10 12L2 9l4-6Z", "M2 9h20", "M9 3 6 9l6 12 6-12-3-6"],
    leaf: ["M5 19C6 9 13 4 21 4c0 8-5 15-15 16", "M5 19c4-5 8-8 13-10"],
    laptop: ["M4 5h16v10H4V5Z", "M2 19h20", "M8 19h8"],
    pin: ["M12 21s7-4.5 7-11a7 7 0 1 0-14 0c0 6.5 7 11 7 11Z", "M12 10.5a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z"],
    search: ["m21 21-4.3-4.3", "M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14Z"],
    sparkle: ["M12 3l1.6 5.4L19 10l-5.4 1.6L12 17l-1.6-5.4L5 10l5.4-1.6L12 3Z", "M5 4v4", "M3 6h4", "M19 16v4", "M17 18h4"],
    star: ["M12 3l2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.3 6.4 20.2 7.5 14 3 9.6l6.2-.9L12 3Z"],
  };

  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.7"
      viewBox="0 0 24 24"
    >
      {paths[name].map((path) => (
        <path d={path} key={path} />
      ))}
    </svg>
  );
}

function TrendingCafeCard({ cafe }: { cafe: CafeSummary }) {
  return (
    <Card className="group min-w-0 border-0 bg-transparent shadow-none">
      <a className="block no-underline" href={`/cafes/${cafe.id}`}>
        <div className="relative mb-4 aspect-[4/5] overflow-hidden rounded-md bg-surface-muted">
          <img
            alt={`${cafe.name} cafe`}
            className="h-full w-full object-cover transition duration-700 group-hover:scale-105"
            decoding="async"
            loading="lazy"
            src={cafe.image}
          />
          <Badge className="absolute right-3 top-3 gap-1 rounded-sm bg-surface/95 px-2 py-1 text-xs font-black text-espresso shadow-sm" variant="outline">
            <ExploreIcon name="star" />
            {cafe.rating}
          </Badge>
        </div>

        <CardTitle className="font-serif text-2xl font-medium text-espresso transition group-hover:text-primary">
          {cafe.name}
        </CardTitle>
      </a>
      <CardContent className="p-0">
        <p className="mt-2 min-h-12 text-sm italic leading-6 text-coffee-muted">
        "{cafe.description}"
        </p>
        <p className="mt-3 flex items-center gap-1 text-xs font-black uppercase tracking-[0.12em] text-muted">
        <ExploreIcon name="pin" />
        {cafe.location}
        </p>
      </CardContent>
    </Card>
  );
}

function EditorialCollectionCard({
  collection,
}: {
  collection: CafeEditorialCollection;
}) {
  return (
    <Card className="group relative min-h-[270px] overflow-hidden border-0 bg-espresso text-white">
      <img
        alt={collection.alt}
        className="absolute inset-0 h-full w-full object-cover transition duration-700 group-hover:scale-105"
        decoding="async"
        loading="lazy"
        src={collection.image}
      />
      <div className="absolute inset-0 bg-gradient-to-t from-espresso/90 via-espresso/45 to-espresso/10" />
      <CardContent className="relative flex h-full min-h-[270px] flex-col justify-end p-8">
        <p className="text-xs font-black uppercase tracking-[0.16em] text-white/85">
          {collection.eyebrow}
        </p>
        <h3 className="mt-2 max-w-sm font-serif text-4xl font-semibold leading-tight">
          {collection.title}
        </h3>
        <Button
          asChild
          className="mt-8 inline-flex h-11 w-fit items-center justify-center border border-white px-6 text-xs font-black uppercase tracking-[0.14em] text-white no-underline transition hover:bg-white hover:text-espresso"
          variant="outline"
        >
          <a href="/explore">{collection.ctaLabel}</a>
        </Button>
      </CardContent>
    </Card>
  );
}

export function ExploreCafes({ cafes, collections }: ExploreCafesProps) {
  const trendingCafes = cafes.slice(0, 3);

  return (
    <div className="w-full overflow-x-clip px-4 py-12 sm:px-8 xl:px-12">
      <div className="mx-auto max-w-[1140px] space-y-16">
        <header className="flex justify-center">
          <label className="relative block w-full max-w-3xl">
            <span className="sr-only">Search cafes</span>
            <span className="absolute left-5 top-1/2 -translate-y-1/2 text-muted">
              <ExploreIcon name="search" />
            </span>
            <Input
              className="h-16 w-full rounded-md border border-transparent bg-surface-muted pl-14 pr-5 text-base font-medium outline-none transition placeholder:italic placeholder:text-muted focus:border-primary focus:bg-surface"
              placeholder="Find your next story..."
              type="search"
            />
          </label>
        </header>

        <section className="space-y-14">
          <div className="flex gap-2 overflow-x-auto pb-1 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
            {exploreFilters.map((filter, index) => (
              <Button
                className={`inline-flex h-10 shrink-0 items-center gap-2 rounded-full border px-5 text-xs font-black uppercase tracking-[0.14em] transition ${
                  index === 0
                    ? "border-espresso bg-espresso text-white"
                    : "border-line-soft bg-surface text-coffee-muted hover:border-primary hover:text-primary"
                }`}
                key={filter.label}
                type="button"
                variant={index === 0 ? "default" : "outline"}
              >
                <ExploreIcon name={filter.icon} />
                {filter.label}
              </Button>
            ))}
          </div>

          <div className="space-y-7">
            <div className="flex items-end justify-between gap-4">
              <h1 className="font-serif text-4xl font-medium italic text-espresso">
                Trending Near You
              </h1>
              <a
                className="border-b border-line-soft pb-1 text-xs font-black uppercase tracking-[0.12em] text-coffee-muted no-underline transition hover:text-primary"
                href="/explore"
              >
                View all
              </a>
            </div>

            <div className="grid gap-8 md:grid-cols-3">
              {trendingCafes.map((cafe) => (
                <TrendingCafeCard cafe={cafe} key={cafe.id} />
              ))}
            </div>
          </div>
        </section>

        <section className="space-y-7">
          <h2 className="font-serif text-4xl font-medium italic text-espresso">
            Editorial Collections
          </h2>
          <div className="grid gap-8 md:grid-cols-2">
            {collections.map((collection) => (
              <EditorialCollectionCard
                collection={collection}
                key={collection.id}
              />
            ))}
          </div>
        </section>

        <footer className="py-14 text-center">
          <Separator className="mb-14" />
          <img
            alt=""
            className="mx-auto h-10 w-10 opacity-50"
            decoding="async"
            src="/icons/cafestory-brand-icon.svg"
          />
          <p className="mt-5 font-serif text-2xl italic text-espresso">
            Cafe Story
          </p>
          <p className="mt-3 text-xs font-black uppercase tracking-[0.16em] text-muted">
            Curating the fine art of coffee
          </p>
        </footer>
      </div>
    </div>
  );
}
