import { MenuSectionTitle } from "@/features/cafes/components/menu-section-title";
import type { CafeMenuItem } from "@/types/cafe";

type MenuSeasonalSectionProps = {
  items: CafeMenuItem[];
};

function formatMenuPrice(price: number) {
  return `$${price.toFixed(1)}`;
}

export function MenuSeasonalSection({ items }: MenuSeasonalSectionProps) {
  return (
    <section className="border-y border-line-soft bg-surface-muted/70 px-6 py-14 sm:px-10">
      <MenuSectionTitle
        className="mb-8"
        eyebrow="Limited Time"
        title="Seasonal Offerings"
      />
      <div className="mx-auto max-w-[620px] divide-y divide-line-soft">
        {items.map((item) => (
          <article
            className="flex items-start justify-between gap-8 py-5 transition hover:border-espresso"
            key={item.id}
          >
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
          </article>
        ))}
      </div>
    </section>
  );
}
