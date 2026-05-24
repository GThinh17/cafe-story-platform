"use client";

import type { ActivityNotification } from "@/types/user";

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
      <span className="grid h-14 w-14 shrink-0 place-items-center">
        <span className="grid h-12 w-12 place-items-center rounded-full bg-black text-xl font-black text-white">
          @
        </span>
      </span>
    );
  }

  return (
    <span className="relative h-14 w-14 shrink-0">
      {item.avatarImages.map((image, index) => (
        <img
          alt=""
          className={`absolute h-10 w-10 rounded-full border-2 border-white object-cover ${
            index === 0 ? "left-0 top-0" : "bottom-0 right-0"
          }`}
          decoding="async"
          key={image}
          loading="lazy"
          src={image}
        />
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
          className="h-14 w-14 shrink-0 rounded-md object-cover"
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

  return (
    <main className="h-screen w-full overflow-hidden border-x border-border bg-white shadow-xl">
      <section className="flex h-full flex-col">
        <header className="shrink-0 px-12 pb-4 pt-11">
          <div className="flex items-start justify-between gap-6">
            <h1 className="text-3xl font-black tracking-[-0.02em] text-black">
              {"Th\u00f4ng b\u00e1o"}
            </h1>
            <button
              aria-label="\u0110\u00f3ng th\u00f4ng b\u00e1o"
              className="grid h-9 w-9 place-items-center rounded-md text-3xl leading-none transition hover:bg-surface-muted"
              onClick={onClose}
              type="button"
            >
              {"\u00d7"}
            </button>
          </div>

          <div className="mt-7 flex gap-3 overflow-x-auto pb-1">
            {filterTabs.map((tab, index) => (
              <button
                className={`h-10 shrink-0 rounded-full border px-5 text-sm font-black transition ${
                  index === 0
                    ? "border-transparent bg-surface-muted"
                    : "border-border bg-white hover:bg-surface-muted"
                }`}
                key={tab}
                type="button"
              >
                {tab}
              </button>
            ))}
          </div>
        </header>

        <div className="min-h-0 flex-1 overflow-y-auto px-12">
          <section>
            <h2 className="text-xl font-black text-black">
              {"Th\u00e1ng n\u00e0y"}
            </h2>
            <div className="mt-5 divide-y divide-border">
              {thisMonthItems.map((item) => (
                <NotificationRow item={item} key={item.id} />
              ))}
            </div>
          </section>

          <section className="mt-7">
            <h2 className="text-xl font-black text-black">
              {"Tr\u01b0\u1edbc \u0111\u00f3"}
            </h2>
            <div className="mt-5 space-y-2">
              {earlierItems.map((item) => (
                <NotificationRow item={item} key={item.id} />
              ))}
            </div>
          </section>
        </div>
      </section>
    </main>
  );
}
