"use client";

import Link from "next/link";
import { useState } from "react";
import { PricingPlanModal } from "@/components/layout/pricing-plan-modal";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";

type CafePageSettingsModalProps = {
  onOpenChange: (open: boolean) => void;
  open: boolean;
};

export function CafePageSettingsModal({
  onOpenChange,
  open,
}: CafePageSettingsModalProps) {
  const [isPricingOpen, setIsPricingOpen] = useState(false);

  return (
    <>
      <Dialog onOpenChange={onOpenChange} open={open}>
        <DialogContent className="w-[min(360px,calc(100vw-32px))] p-0">
          <DialogTitle className="sr-only">Cafe page settings</DialogTitle>
          <DialogDescription className="sr-only">
            Choose a cafe page management action.
          </DialogDescription>
          <div className="flex flex-col text-center">
            <button
              className="min-h-12 px-6 text-sm font-semibold text-muted transition disabled:cursor-not-allowed disabled:opacity-60"
              disabled
              title="Cafe menu editing is not available yet."
              type="button"
            >
              Edit menu
            </button>
            <Separator />
            <DialogClose asChild>
              <Link
                className="flex min-h-12 items-center justify-center px-6 text-sm font-semibold text-foreground transition hover:bg-surface-muted"
                href="/cafes/edit"
              >
                Edit cafe page
              </Link>
            </DialogClose>
            <Separator />
            <button
              className="min-h-12 px-6 text-sm font-semibold text-foreground transition hover:bg-surface-muted"
              onClick={() => {
                onOpenChange(false);
                setIsPricingOpen(true);
              }}
              type="button"
            >
              Renew subscription
            </button>
            <Separator />
            <DialogClose asChild>
              <button
                className="min-h-12 px-6 text-sm text-foreground transition hover:bg-surface-muted"
                type="button"
              >
                Cancel
              </button>
            </DialogClose>
          </div>
        </DialogContent>
      </Dialog>

      <PricingPlanModal
        isOpen={isPricingOpen}
        onOpenChange={setIsPricingOpen}
      />
    </>
  );
}
