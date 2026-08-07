"use client";

import { useI18n } from "@/components/providers/locale-provider";
import { MenuItemCard } from "@/components/cafe/menu-item-card";
import { MenuSectionTitle } from "@/components/cafe/menu-section-title";
import type { CafeMenuItem } from "@/types/cafe";

type MenuPastrySectionProps = {
  items: CafeMenuItem[];
};

export function MenuPastrySection({ items }: MenuPastrySectionProps) {
  const { t } = useI18n();

  return (
    <section className="space-y-8 px-6 py-12 sm:px-10">
      <MenuSectionTitle ornament title={t("cafeMenu.pastry.title")} />
      <div className="grid gap-4 md:grid-cols-2">
        {items.map((item) => (
          <MenuItemCard item={item} key={item.id} variant="image-row" />
        ))}
      </div>
    </section>
  );
}
