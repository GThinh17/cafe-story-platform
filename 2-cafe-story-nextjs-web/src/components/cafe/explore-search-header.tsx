"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { XIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ExploreIcon } from "@/components/cafe/explore-icon";

export function ExploreSearchHeader() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [inputValue, setInputValue] = useState(searchParams.get("query") ?? "");

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const query = inputValue.trim();
    const params = new URLSearchParams(searchParams.toString());
    if (query) {
      params.set("query", query);
    } else {
      params.delete("query");
    }
    router.push(`/explore?${params.toString()}`);
  }

  function handleClear() {
    setInputValue("");
    const params = new URLSearchParams(searchParams.toString());
    params.delete("query");
    router.push(`/explore?${params.toString()}`);
  }

  const hasValue = inputValue.length > 0;

  return (
    <header className="mx-auto flex w-full max-w-3xl flex-col items-center gap-5 text-center">
      <div className="space-y-3">
        <h1 className="font-sans text-3xl font-black leading-tight tracking-tight text-espresso sm:text-4xl">
          Find your next coffee story.
        </h1>
      </div>

      <form className="relative w-full" role="search" onSubmit={handleSubmit}>
        <label className="sr-only" htmlFor="explore-cafe-search">
          Search cafes and stories
        </label>
        <span className="absolute left-5 top-1/2 z-10 -translate-y-1/2 text-muted">
          <ExploreIcon name="search" />
        </span>
        <Input
          className="h-16 rounded-md border-transparent bg-surface-muted pl-14 pr-36 text-base font-medium shadow-sm focus:border-primary focus:bg-surface"
          id="explore-cafe-search"
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Find your next story..."
          type="search"
          value={inputValue}
        />
        {hasValue && (
          <button
            aria-label="Clear search"
            className="absolute right-[7.5rem] top-1/2 -translate-y-1/2 grid size-8 place-items-center rounded-full text-muted transition hover:bg-surface-muted hover:text-foreground"
            onClick={handleClear}
            type="button"
          >
            <XIcon className="size-4" />
          </button>
        )}
        <Button
          className="absolute right-2 top-1/2 hidden h-12 -translate-y-1/2 rounded-md px-5 text-xs font-semibold sm:inline-flex"
          type="submit"
        >
          Search
        </Button>
      </form>
    </header>
  );
}
