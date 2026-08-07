"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { PricingPlanModal } from "@/components/layout/pricing-plan-modal";
import { useI18n } from "@/components/providers/locale-provider";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";

type CafePageExpiredModalProps = {
  isOwner: boolean;
};

export function CafePageExpiredModal({ isOwner }: CafePageExpiredModalProps) {
  const { t } = useI18n();
  const router = useRouter();
  const [isPricingOpen, setIsPricingOpen] = useState(false);

  return (
    <>
      <Dialog
        onOpenChange={(open) => {
          if (!open) {
            router.replace("/");
          }
        }}
        open
      >
        <DialogContent className="w-[min(420px,calc(100vw-32px))] p-6">
          <div className="flex flex-col gap-5">
            <div className="flex flex-col gap-2">
              <DialogTitle>{t("cafe.expired.title")}</DialogTitle>
              <DialogDescription>
                {t("cafe.expired.description")}
              </DialogDescription>
            </div>

            <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <Button
                onClick={() => router.back()}
                type="button"
                variant="outline"
              >
                {t("cafe.expired.goBack")}
              </Button>
              {isOwner ? (
                <Button
                  onClick={() => setIsPricingOpen(true)}
                  type="button"
                >
                  {t("cafe.expired.renew")}
                </Button>
              ) : null}
            </div>
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
