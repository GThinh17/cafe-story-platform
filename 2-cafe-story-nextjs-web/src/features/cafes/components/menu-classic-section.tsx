import { MenuItemCard } from "@/features/cafes/components/menu-item-card";
import { MenuSectionTitle } from "@/features/cafes/components/menu-section-title";
import type { CafeMenuItem } from "@/types/cafe";

type MenuClassicSectionProps = {
  items: CafeMenuItem[];
};

export function MenuClassicSection({ items }: MenuClassicSectionProps) {
  return (
    <section className="space-y-8 px-6 py-12 sm:px-10">
      <MenuSectionTitle eyebrow="The Classics" title="Espresso Bar" />
      <div className="grid gap-3 md:grid-cols-2">
        {items.map((item) => (
          <MenuItemCard item={item} key={item.id} variant="compact" />
        ))}
      </div>
    </section>
  );
}
