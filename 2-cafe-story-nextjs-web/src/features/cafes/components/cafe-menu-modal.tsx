"use client";

import * as DialogPrimitive from "@radix-ui/react-dialog";
import { CafeMenuModalHeader } from "@/features/cafes/components/cafe-menu-modal-header";
import { Dialog, DialogOverlay, DialogPortal } from "@/components/ui/dialog";
import { MenuClassicSection } from "@/features/cafes/components/menu-classic-section";
import { MenuPastrySection } from "@/features/cafes/components/menu-pastry-section";
import { MenuSeasonalSection } from "@/features/cafes/components/menu-seasonal-section";
import { MenuSignatureSection } from "@/features/cafes/components/menu-signature-section";
import { ScrollArea } from "@/components/ui/scroll-area";
import { cn } from "@/lib/utils";
import type { CafeMenu } from "@/types/cafe";

type CafeMenuModalProps = {
  cafeName: string;
  menu: CafeMenu;
  onOpenChange: (open: boolean) => void;
  open: boolean;
};

export function CafeMenuModal({
  cafeName,
  menu,
  onOpenChange,
  open,
}: CafeMenuModalProps) {
  return (
    <Dialog onOpenChange={onOpenChange} open={open}>
      <DialogPortal>
        <DialogOverlay className="z-[90] bg-black/70 backdrop-blur-md" />
        <DialogPrimitive.Content
          className={cn(
            "fixed left-1/2 top-1/2 z-[100] w-[calc(100vw-32px)] max-w-[900px] -translate-x-1/2 -translate-y-1/2 overflow-hidden rounded-[24px] border border-line-soft bg-surface text-espresso shadow-[0_36px_120px_rgba(0,0,0,0.45)] outline-none",
            "max-h-[90vh] data-[state=open]:animate-in data-[state=open]:fade-in-0 data-[state=open]:zoom-in-95 data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95",
          )}
        >
          <CafeMenuModalHeader cafeName={cafeName} />
          <ScrollArea className="max-h-[calc(90vh-89px)] bg-[radial-gradient(circle_at_top,#fffdf8_0,#f7f4ef_56%,#efe7da_100%)]">
            <MenuSignatureSection items={menu.signature} />
            <MenuSeasonalSection items={menu.seasonal} />
            <MenuClassicSection items={menu.classics} />
            <MenuPastrySection items={menu.pastries} />
          </ScrollArea>
        </DialogPrimitive.Content>
      </DialogPortal>
    </Dialog>
  );
}
