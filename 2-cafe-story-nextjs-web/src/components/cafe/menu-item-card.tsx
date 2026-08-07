"use client";

import { useI18n } from "@/components/providers/locale-provider";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import type { CafeMenuItem } from "@/types/cafe";

type MenuItemCardProps = {
  item: CafeMenuItem;
  variant?: "featured" | "compact" | "image-row";
};

function formatMenuPrice(price: number) {
  return `$${price.toFixed(1)}`;
}

function MenuItemImageGrid({ item }: { item: CafeMenuItem }) {
  const { t } = useI18n();
  const images =
    item.images ??
    (item.image
      ? [
          {
            src: item.image,
            alt: t("cafe.menuItemAlt", { name: item.name }),
            label: item.name,
          },
        ]
      : []);

  if (images.length === 0) {
    return null;
  }

  return (
    <div className="relative grid aspect-[4/3] grid-cols-2 overflow-hidden bg-surface-muted">
      {images.slice(0, 4).map((image) => (
        <div className="relative min-h-0 overflow-hidden" key={image.src}>
          <img
            alt={image.alt}
            className="h-full w-full object-cover transition duration-700 group-hover:scale-105"
            decoding="async"
            loading="lazy"
            src={image.src}
          />
          {image.label ? (
            <span className="absolute inset-x-2 bottom-2 line-clamp-1 rounded-full bg-surface/70 px-2 py-1 text-center font-serif text-sm text-espresso backdrop-blur">
              {image.label}
            </span>
          ) : null}
        </div>
      ))}
      {item.badge ? (
        <Badge className="absolute left-4 top-4 rounded-full bg-espresso px-3 py-1 text-[10px] font-black uppercase tracking-[0.1em] text-white shadow-md">
          {item.badge}
        </Badge>
      ) : null}
    </div>
  );
}

export function MenuItemCard({
  item,
  variant = "featured",
}: MenuItemCardProps) {
  if (variant === "compact") {
    return (
      <article className="rounded-md p-4 transition hover:bg-surface-muted">
        <div className="flex items-start justify-between gap-5">
          <div>
            <h4 className="font-serif text-xl font-medium text-espresso">
              {item.name}
            </h4>
            <p className="mt-1 text-sm leading-6 text-coffee-muted">
              {item.description}
            </p>
          </div>
          <p className="shrink-0 font-serif text-base italic text-coffee-muted">
            {formatMenuPrice(item.price)}
          </p>
        </div>
      </article>
    );
  }

  if (variant === "image-row") {
    return (
      <article className="group flex gap-4 rounded-md border border-line-soft bg-surface p-3 transition hover:-translate-y-0.5 hover:shadow-md">
        {item.image ? (
          <div className="size-24 shrink-0 overflow-hidden rounded-full border border-line-soft bg-surface-muted">
            <img
              alt={`${item.name} menu item`}
              className="h-full w-full object-cover transition duration-500 group-hover:scale-105"
              decoding="async"
              loading="lazy"
              src={item.image}
            />
          </div>
        ) : null}
        <div className="min-w-0 flex-1 py-1">
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0">
              <div className="flex flex-wrap items-center gap-2">
                <h4 className="font-serif text-xl font-medium text-espresso">
                  {item.name}
                </h4>
                {item.badge ? (
                  <Badge className="rounded-full px-2 py-0.5 text-[10px] font-black uppercase tracking-[0.1em]">
                    {item.badge}
                  </Badge>
                ) : null}
              </div>
              <p className="mt-1 text-sm leading-6 text-coffee-muted">
                {item.description}
              </p>
            </div>
            <p className="shrink-0 font-serif text-base italic text-coffee-muted">
              {formatMenuPrice(item.price)}
            </p>
          </div>
        </div>
      </article>
    );
  }

  return (
    <Card className="group overflow-hidden border-line-soft bg-surface shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-xl">
      <MenuItemImageGrid item={item} />
      <CardContent className="p-6">
        <div className="flex items-start justify-between gap-5">
          <h4 className="font-serif text-2xl font-medium text-espresso">
            {item.name}
          </h4>
          <p className="shrink-0 font-serif text-lg italic text-coffee-muted">
            {formatMenuPrice(item.price)}
          </p>
        </div>
        <p
          className={cn(
            "mt-3 text-sm leading-6 text-coffee-muted",
            item.description.length < 72 ? "max-w-[28ch]" : undefined,
          )}
        >
          {item.description}
        </p>
      </CardContent>
    </Card>
  );
}
