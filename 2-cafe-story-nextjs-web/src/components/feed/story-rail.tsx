"use client";

import Link from "next/link";
import { useCallback, useEffect, useRef, useState } from "react";
import { ChevronLeftIcon, ChevronRightIcon, PlusIcon } from "lucide-react";
import type { StoryItem } from "@/types/feed";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { Button } from "../ui/button";

type StoryRailProps = {
  stories: StoryItem[];
};

const SCROLL_STEP = 500;

export function StoryRail({ stories }: StoryRailProps) {
  const scrollerRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const updateScrollState = useCallback(() => {
    const el = scrollerRef.current;
    if (!el) return;
    setCanScrollLeft(el.scrollLeft > 0);
    setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 1);
  }, []);

  useEffect(() => {
    updateScrollState();
    const el = scrollerRef.current;
    if (!el) return;

    el.addEventListener("scroll", updateScrollState, { passive: true });
    window.addEventListener("resize", updateScrollState);
    return () => {
      el.removeEventListener("scroll", updateScrollState);
      window.removeEventListener("resize", updateScrollState);
    };
  }, [updateScrollState, stories.length]);

  const scrollBy = (delta: number) => {
    scrollerRef.current?.scrollBy({ left: delta, behavior: "smooth" });
  };

  return (
    <div className="flex w-full items-center gap-2">
      <Button
        aria-hidden={!canScrollLeft}
        aria-label="Scroll stories left"
        className="grid size-8 shrink-0 place-items-center rounded-full bg-surface shadow-md transition hover:bg-surface-muted disabled:invisible"
        disabled={!canScrollLeft}
        onClick={() => scrollBy(-SCROLL_STEP)}
        type="button"
      >
        <ChevronLeftIcon className="size-4 text-foreground" />
      </Button>

      <div
        className="min-w-0 flex-1 overflow-x-auto pb-1 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        ref={scrollerRef}
      >
        <div className="flex gap-8">
          {stories.map((story) => {
            const initial = (story.label || "?").slice(0, 1).toUpperCase();
            return (
              <Link
                aria-label={`View ${story.label} story`}
                className="grid w-[4.2rem] shrink-0 justify-items-center text-center"
                href={story.href}
                key={story.id}
              >
                <Avatar className="size-[5.2rem]">
                  <AvatarImage
                    alt={`${story.label} story avatar`}
                    src={story.avatarUrl}
                  />
                  <AvatarFallback>{initial}</AvatarFallback>
                </Avatar>
                <span className="mt-2 block w-[5.2rem] truncate text-xs font-semibold leading-4 text-foreground">
                  {story.label}
                </span>
              </Link>
            );
          })}

          <Link
            aria-label="Discover more on explore"
            className="grid w-[5.2rem] shrink-0 justify-items-center text-center"
            href="/explore"
          >
            <span className="grid size-[5.2rem] place-items-center rounded-full bg-surface-muted">
              <PlusIcon className="size-6 text-primary" />
            </span>
            <span className="mt-2 block w-[4.2rem] truncate text-xs font-semibold leading-4 text-foreground">
              Explore
            </span>
          </Link>
        </div>
      </div>

      <Button
        aria-hidden={!canScrollRight}
        aria-label="Scroll stories right"
        className="grid size-8 shrink-0 place-items-center rounded-full bg-surface shadow-md transition hover:bg-surface-muted disabled:invisible"
        disabled={!canScrollRight}
        onClick={() => scrollBy(SCROLL_STEP)}
        type="button"
      >
        <ChevronRightIcon className="size-4 text-foreground" />
      </Button>
    </div>
  );
}
