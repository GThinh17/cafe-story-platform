import { MenuItemCard } from "@/features/cafes/components/menu-item-card";
import { MenuSectionTitle } from "@/features/cafes/components/menu-section-title";
import type { CafeMenuItem } from "@/types/cafe";

type MenuSignatureSectionProps = {
  items: CafeMenuItem[];
};

export function MenuSignatureSection({ items }: MenuSignatureSectionProps) {
  return (
    <section className="space-y-10 px-6 py-10 sm:px-10">
      <MenuSectionTitle ornament title="Signature Pours" />
      <div className="grid gap-8 md:grid-cols-2">
        {items.map((item) => (
          <MenuItemCard item={item} key={item.id} />
        ))}
      </div>
    </section>
  );
}
