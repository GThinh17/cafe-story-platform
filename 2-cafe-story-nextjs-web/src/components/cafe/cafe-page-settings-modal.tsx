"use client";

import Link from "next/link";
import { useState } from "react";
import { useI18n } from "@/components/providers/locale-provider";
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
  const { t } = useI18n();
  const [isPricingOpen, setIsPricingOpen] = useState(false);

  return (
    <>
      <Dialog onOpenChange={onOpenChange} open={open}>
        <DialogContent className="w-[min(360px,calc(100vw-32px))] p-0">
          <DialogTitle className="sr-only">{t("cafe.settings.title")}</DialogTitle>
          <DialogDescription className="sr-only">
            {t("cafe.settings.srDescription")}
          </DialogDescription>
          <div className="flex flex-col text-center">
            <button
              className="min-h-12 px-6 text-sm font-semibold text-muted transition disabled:cursor-not-allowed disabled:opacity-60"
              disabled
              title={t("cafe.settings.editMenuDisabled")}
              type="button"
            >
              {t("cafe.settings.editMenu")}
            </button>
            <Separator />
            <DialogClose asChild>
              <Link
                className="flex min-h-12 items-center justify-center px-6 text-sm font-semibold text-foreground transition hover:bg-surface-muted"
                href="/cafes/edit"
              >
                {t("cafe.settings.editPage")}
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
              {t("cafe.settings.renew")}
            </button>
            <Separator />
            <DialogClose asChild>
              <button
                className="min-h-12 px-6 text-sm text-foreground transition hover:bg-surface-muted"
                type="button"
              >
                {t("common.cancel")}
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
