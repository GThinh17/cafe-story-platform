import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ExploreIcon } from "@/components/cafe/explore-icon";

export function ExploreSearchHeader() {
  return (
    <header className="mx-auto flex w-full max-w-3xl flex-col items-center gap-5 text-center">
      <div className="space-y-3">
        <p className="text-xs font-black uppercase tracking-[0.18em] text-muted">
          Explore cafes
        </p>
        <h1 className="font-serif text-4xl font-medium italic leading-tight text-espresso sm:text-5xl">
          Find your next coffee story.
        </h1>
      </div>

      <form className="relative w-full" role="search">
        <label className="sr-only" htmlFor="explore-cafe-search">
          Search cafes and stories
        </label>
        <span className="absolute left-5 top-1/2 z-10 -translate-y-1/2 text-muted">
          <ExploreIcon name="search" />
        </span>
        <Input
          className="h-16 rounded-md border-transparent bg-surface-muted pl-14 pr-36 text-base font-medium shadow-sm placeholder:italic focus:border-primary focus:bg-surface"
          id="explore-cafe-search"
          placeholder="Find your next story..."
          type="search"
        />
        <Button
          className="absolute right-2 top-1/2 hidden h-12 -translate-y-1/2 rounded-md px-5 text-xs font-black uppercase tracking-[0.12em] sm:inline-flex"
          type="submit"
        >
          Search
        </Button>
      </form>
    </header>
  );
}
