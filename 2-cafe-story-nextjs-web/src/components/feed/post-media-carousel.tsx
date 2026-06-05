"use client";

import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react";
import { useEffect, useMemo, useState, type CSSProperties } from "react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import type { FeedPost, FeedPostMedia } from "@/types/feed";

type PostMediaCarouselProps = {
  media: FeedPostMedia[];
  className?: string;
  frame?: "adaptive" | "fixed";
  imageClassName?: string;
};

const DEFAULT_ADAPTIVE_RATIO = 1;
const MAX_ADAPTIVE_RATIO = 16 / 9;
const MIN_ADAPTIVE_RATIO = 9 / 16;

function getMediaKey(media: FeedPostMedia, index: number) {
  return media.id ?? `${media.src}-${index}`;
}

function clampImageRatio(width: number, height: number) {
  if (width <= 0 || height <= 0) {
    return DEFAULT_ADAPTIVE_RATIO;
  }

  return Math.min(MAX_ADAPTIVE_RATIO, Math.max(MIN_ADAPTIVE_RATIO, width / height));
}

export function getFeedPostMediaList(post: FeedPost): FeedPostMedia[] {
  const media = post.media?.length
    ? post.media
    : post.image?.trim()
    ? [
        {
          id: post.id ? `${post.id}-image` : undefined,
          src: post.image,
          alt: `${post.cafe} post media`,
          type: "image" as const,
        },
      ]
    : [];

  return media.filter(
    (item) => item.type !== "video" && item.src.trim().length > 0,
  );
}

export function PostMediaCarousel({
  className,
  frame = "adaptive",
  imageClassName,
  media,
}: PostMediaCarouselProps) {
  const items = useMemo(
    () =>
      media.filter(
        (item) => item.type !== "video" && item.src.trim().length > 0,
      ),
    [media],
  );
  const mediaSignature = useMemo(
    () => items.map((item, index) => getMediaKey(item, index)).join("\u0000"),
    [items],
  );
  const [activeIndex, setActiveIndex] = useState(0);
  const [ratios, setRatios] = useState<Record<string, number>>({});
  const clampedActiveIndex = Math.min(activeIndex, Math.max(items.length - 1, 0));
  const activeItem = items[clampedActiveIndex];
  const hasMultipleItems = items.length > 1;

  useEffect(() => {
    setActiveIndex(0);
  }, [mediaSignature]);

  if (!activeItem) {
    return null;
  }

  const activeKey = getMediaKey(activeItem, clampedActiveIndex);
  const adaptiveStyle: CSSProperties | undefined =
    frame === "adaptive"
      ? {
          aspectRatio: ratios[activeKey] ?? DEFAULT_ADAPTIVE_RATIO,
        }
      : undefined;

  function goToPrevious() {
    setActiveIndex((currentIndex) => {
      const normalizedIndex = Math.min(
        currentIndex,
        Math.max(items.length - 1, 0),
      );

      return normalizedIndex === 0 ? items.length - 1 : normalizedIndex - 1;
    });
  }

  function goToNext() {
    setActiveIndex((currentIndex) => {
      const normalizedIndex = Math.min(
        currentIndex,
        Math.max(items.length - 1, 0),
      );

      return normalizedIndex === items.length - 1 ? 0 : normalizedIndex + 1;
    });
  }

  return (
    <div
      className={cn(
        "relative w-full overflow-hidden bg-espresso",
        frame === "adaptive" ? "max-h-[760px]" : "h-full",
        className,
      )}
      style={adaptiveStyle}
    >
      <div
        className="flex h-full w-full transition-transform duration-300 ease-out"
        style={{
          transform: `translateX(-${clampedActiveIndex * 100}%)`,
        }}
      >
        {items.map((item, index) => {
          const itemKey = getMediaKey(item, index);

          return (
            <div className="relative h-full w-full shrink-0" key={itemKey}>
              <img
                alt={item.alt ?? "Post media"}
                className={cn(
                  "absolute inset-0 h-full w-full object-contain",
                  imageClassName,
                )}
                decoding="async"
                loading="lazy"
                onLoad={(event) => {
                  if (frame !== "adaptive") {
                    return;
                  }

                  const image = event.currentTarget;
                  const nextRatio = clampImageRatio(
                    image.naturalWidth,
                    image.naturalHeight,
                  );

                  setRatios((currentRatios) =>
                    currentRatios[itemKey] === nextRatio
                      ? currentRatios
                      : {
                          ...currentRatios,
                          [itemKey]: nextRatio,
                        },
                  );
                }}
                src={item.src}
              />
            </div>
          );
        })}
      </div>

      {hasMultipleItems ? (
        <>
          <Button
            aria-label="Show previous post photo"
            className="absolute left-2 top-1/2 z-10 size-8 -translate-y-1/2 rounded-full border border-white/20 bg-black/45 p-0 text-white shadow-sm hover:bg-black/65 hover:text-white"
            onClick={goToPrevious}
            size="icon-sm"
            type="button"
            variant="ghost"
          >
            <ChevronLeftIcon className="size-4" />
          </Button>
          <Button
            aria-label="Show next post photo"
            className="absolute right-2 top-1/2 z-10 size-8 -translate-y-1/2 rounded-full border border-white/20 bg-black/45 p-0 text-white shadow-sm hover:bg-black/65 hover:text-white"
            onClick={goToNext}
            size="icon-sm"
            type="button"
            variant="ghost"
          >
            <ChevronRightIcon className="size-4" />
          </Button>
          <div
            aria-label="Post photo carousel position"
            className="absolute bottom-3 left-1/2 z-10 flex -translate-x-1/2 items-center gap-1.5"
          >
            {items.map((item, index) => (
              <button
                aria-label={`Show post photo ${index + 1}`}
                className={cn(
                  "size-2 rounded-full bg-white/45 shadow-[0_1px_3px_rgba(0,0,0,0.45)] transition-colors",
                  index === clampedActiveIndex && "bg-primary",
                )}
                key={getMediaKey(item, index)}
                onClick={() => setActiveIndex(index)}
                type="button"
              />
            ))}
          </div>
        </>
      ) : null}
    </div>
  );
}
