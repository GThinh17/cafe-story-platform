"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { XIcon } from "lucide-react";
import { Input } from "@/components/ui/input";
import { ExploreIcon } from "@/components/cafe/explore-icon";

export function ExploreSearchHeader() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [inputValue, setInputValue] = useState(searchParams.get("query") ?? "");
  const isFirstRender = useRef(true);

  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }

    const timer = setTimeout(() => {
      const query = inputValue.trim();
      const params = new URLSearchParams(searchParams.toString());
      if (query) {
        params.set("query", query);
      } else {
        params.delete("query");
      }
      router.replace(`/explore?${params.toString()}`);
    }, 400);

    return () => clearTimeout(timer);
  }, [inputValue, router, searchParams]);

  function handleClear() {
    setInputValue("");
  }

  const hasValue = inputValue.length > 0;

  return (
    <div className="mx-auto w-full max-w-[690px] max-h-[42px]">
      <form
        className="relative w-full"
        role="search"
        onSubmit={(e) => e.preventDefault()}
      >
        <label className="sr-only" htmlFor="explore-cafe-search">
          Search cafes and stories
        </label>
        <span className="absolute left-4 top-1/2 z-10 -translate-y-1/2 text-muted">
          <ExploreIcon name="search" />
        </span>
        <Input
          autoComplete="off"
          className="h-[42px] rounded-3xl border-transparent bg-surface-muted pl-11 pr-10 text-sm font-medium border-none focus:border-transparent focus:outline-none"
          id="explore-cafe-search"
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Find your next story..."
          type="text"
          value={inputValue}
        />
        {hasValue && (
          <button
            aria-label="Clear search"
            className="absolute right-3 top-1/2 -translate-y-1/2 grid size-7 place-items-center rounded-full text-muted transition hover:bg-surface-muted hover:text-foreground"
            onClick={handleClear}
            type="button"
          >
            <XIcon className="size-3.5" />
          </button>
        )}
      </form>
    </div>
  );
}
