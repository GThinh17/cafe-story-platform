"use client";

import { BookOpenIcon, MapIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CafeMenuModal } from "@/features/cafes/components/cafe-menu-modal";
import { useCafeMenuModal } from "@/features/cafes/hooks/use-cafe-menu-modal";
import type { CafeMenu } from "@/types/cafe";

type CafeActionButtonsProps = {
  cafeName: string;
  menu: CafeMenu;
};

export function CafeActionButtons({ cafeName, menu }: CafeActionButtonsProps) {
  const { isOpen, openMenu, setIsOpen } = useCafeMenuModal();

  return (
    <>
      <div className="flex flex-col gap-3 sm:flex-row">
        <Button
          className="flex h-12 items-center justify-center gap-2 rounded-sm bg-espresso px-8 text-sm font-black text-white transition hover:bg-primary-strong"
          onClick={openMenu}
          type="button"
        >
          <BookOpenIcon data-icon="inline-start" />
          View Menu
        </Button>
        <Button
          className="flex h-12 items-center justify-center gap-2 rounded-sm border border-espresso bg-transparent px-8 text-sm font-black text-espresso transition hover:bg-surface-muted"
          type="button"
          variant="outline"
        >
          <MapIcon data-icon="inline-start" />
          Get Directions
        </Button>
      </div>

      <CafeMenuModal
        cafeName={cafeName}
        menu={menu}
        onOpenChange={setIsOpen}
        open={isOpen}
      />
    </>
  );
}
