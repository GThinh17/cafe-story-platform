"use client";

import { useI18n } from "@/components/providers/locale-provider";
import { MenuItemCard } from "@/components/cafe/menu-item-card";
import { MenuSectionTitle } from "@/components/cafe/menu-section-title";
import type { CafeMenuItem } from "@/types/cafe";

type MenuSignatureSectionProps = {
  items: CafeMenuItem[];
};

export function MenuSignatureSection({ items }: MenuSignatureSectionProps) {
  const { t } = useI18n();

  return (
    <section className="space-y-10 px-6 py-10 sm:px-10">
      <MenuSectionTitle ornament title={t("cafeMenu.signature.title")} />
      <div className="grid gap-8 md:grid-cols-2">
        {items.map((item) => (
          <MenuItemCard item={item} key={item.id} />
        ))}
      </div>
    </section>
  );
}
