"use client";

import { useEffect, useRef, useState } from "react";
import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react";
import type { ActivityNotification } from "@/types/user";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";

type ActivityListProps = {
  items: ActivityNotification[];
  onClose?: () => void;
};

const filterTabs = [
  "T\u1ea5t c\u1ea3",
  "Ng\u01b0\u1eddi m\u00e0 b\u1ea1n theo d\u00f5i",
  "B\u00ecnh lu\u1eadn",
  "L\u01b0\u1ee3t theo",
];

function NotificationAvatars({ item }: { item: ActivityNotification }) {
  if (item.iconLabel) {
    return (
      <Avatar className="size-14" size="lg">
        <AvatarFallback className="bg-foreground text-xl font-black text-background">
          @
        </AvatarFallback>
      </Avatar>
    );
  }

  return (
    <span className="relative size-14 shrink-0">
      {item.avatarImages.map((image, index) => (
        <Avatar
          className={`absolute size-10 border-2 border-surface ${
            index === 0 ? "left-0 top-0" : "bottom-0 right-0"
          }`}
          key={image}
        >
          <AvatarImage alt="" src={image} />
          <AvatarFallback>{item.actors[index]?.slice(0, 1) ?? "U"}</AvatarFallback>
        </Avatar>
      ))}
    </span>
  );
}

function NotificationRow({ item }: { item: ActivityNotification }) {
  return (
    <article className="flex min-w-0 items-center gap-4 py-4">
      <NotificationAvatars item={item} />

      <p className="min-w-0 flex-1 text-base leading-6 text-foreground">
        {item.actors.map((actor, index) => (
          <span key={actor}>
            {index > 0 ? " v\u00e0 " : ""}
            <span className="font-black">{actor}</span>
          </span>
        ))}
        {item.actors.length > 0 ? " " : ""}
        {item.message} <span className="text-muted">{item.date}</span>
      </p>

      {item.thumbnailImage ? (
        <img
          alt=""
          className="size-14 shrink-0 rounded-md object-cover"
          decoding="async"
          loading="lazy"
          src={item.thumbnailImage}
        />
      ) : null}
    </article>
  );
}

export function ActivityList({ items, onClose }: ActivityListProps) {
  const thisMonthItems = items.filter((item) => item.section === "thisMonth");
  const earlierItems = items.filter((item) => item.section === "earlier");
  const tabsViewportRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  function updateTabScrollState() {
    const viewport = tabsViewportRef.current;

    if (!viewport) {
      return;
    }

    setCanScrollLeft(viewport.scrollLeft > 0);
    setCanScrollRight(
      viewport.scrollLeft + viewport.clientWidth < viewport.scrollWidth - 1,
    );
  }

  function scrollTabs(direction: "left" | "right") {
    const viewport = tabsViewportRef.current;

    if (!viewport) {
      return;
    }

    viewport.scrollBy({
      left: direction === "right" ? 200 : -200,
      behavior: "smooth",
    });
  }

  useEffect(() => {
    updateTabScrollState();
    window.addEventListener("resize", updateTabScrollState);

    return () => window.removeEventListener("resize", updateTabScrollState);
  }, []);

  return (
    <main className="h-screen w-full overflow-hidden border-x border-border bg-background shadow-xl">
      <section className="flex h-full flex-col">
        <header className="shrink-0 px-4 pb-4 pt-10 sm:px-6">
          <div className="flex items-start justify-between gap-6">
            <h1 className="text-3xl font-black text-foreground">
              {"Th\u00f4ng b\u00e1o"}
            </h1>
            <Button
              aria-label="\u0110\u00f3ng th\u00f4ng b\u00e1o"
              onClick={onClose}
              size="icon-sm"
              type="button"
              variant="ghost"
            >
              {"\u00d7"}
            </Button>
          </div>

          <Tabs className="mt-7" defaultValue={filterTabs[0]}>
            <div className="relative">
              {canScrollLeft ? (
                <Button
                  aria-label="Scroll notification filters left"
                  className="absolute left-0 top-1/2 z-10 size-8 -translate-y-1/2 rounded-full bg-surface shadow- ring-5 ring-background/75"
                  onClick={() => scrollTabs("left")}
                  size="icon-sm"
                  type="button"
                  variant="outline"
                >
                  <ChevronLeftIcon />
                </Button>
              ) : null}

              <div
                className="overflow-hidden"
                onScroll={updateTabScrollState}
                ref={tabsViewportRef}
              >
                <TabsList className="flex h-auto w-max flex-nowrap justify-start gap-3 rounded-none bg-transparent p-0 pr-12">
                  {filterTabs.map((tab) => (
                    <TabsTrigger
                      className="shrink-0 rounded-full border border-border bg-surface px-5 py-2.5 text-sm font-black shadow-sm data-[state=active]:border-transparent data-[state=active]:bg-surface-muted"
                      key={tab}
                      value={tab}
                    >
                      {tab}
                    </TabsTrigger>
                  ))}
                </TabsList>
              </div>

              {canScrollRight ? (
                <Button
                  aria-label="Scroll notification filters right"
                  className="absolute right-0 top-1/2 z-10 size-8 -translate-y-1/2 rounded-full bg-surface shadow- ring-5 ring-background/75"
                  onClick={() => scrollTabs("right")}
                  size="icon-sm"
                  type="button"
                  variant="outline"
                >
                  <ChevronRightIcon />
                </Button>
              ) : null}
            </div>
          </Tabs>
        </header>

        <div className="min-h-0 flex-1 overflow-y-auto px-4 [scrollbar-width:none] sm:px-6 [&::-webkit-scrollbar]:hidden">
          {thisMonthItems.length === 0 && earlierItems.length === 0 ? (
            <div className="flex flex-col items-center gap-3 py-20 text-center">
              <p className="text-base font-medium text-foreground">Ch\u01b0a c\u00f3 th\u00f4ng b\u00e1o n\u00e0o.</p>
              <p className="max-w-[240px] text-sm leading-6 text-muted">
                Theo d\u00f5i ai \u0111\u00f3 \u0111\u1ec3 nh\u1eadn c\u1eadp nh\u1eadt v\u1ec1 b\u00e0i vi\u1ebft v\u00e0 ho\u1ea1t \u0111\u1ed9ng c\u1ee7a h\u1ecd.
              </p>
            </div>
          ) : (
            <>
              {thisMonthItems.length > 0 && (
                <section>
                  <h2 className="text-xl font-black text-foreground">
                    {"Th\u00e1ng n\u00e0y"}
                  </h2>
                  <div className="mt-5 flex flex-col">
                    {thisMonthItems.map((item) => (
                      <div key={item.id}>
                        <NotificationRow item={item} />
                        <Separator />
                      </div>
                    ))}
                  </div>
                </section>
              )}

              {earlierItems.length > 0 && (
                <section className="mt-7">
                  <h2 className="text-xl font-black text-foreground">
                    {"Tr\u01b0\u1edbc \u0111\u00f3"}
                  </h2>
                  <div className="mt-5 flex flex-col gap-2">
                    {earlierItems.map((item) => (
                      <NotificationRow item={item} key={item.id} />
                    ))}
                  </div>
                </section>
              )}
            </>
          )}
        </div>
      </section>
    </main>
  );
}
