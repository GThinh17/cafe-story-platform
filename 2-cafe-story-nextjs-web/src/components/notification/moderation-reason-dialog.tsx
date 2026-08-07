"use client";

import { useI18n } from "@/components/providers/locale-provider";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import type { TranslationKey } from "@/lib/i18n";
import type { ModerationStatus } from "@/types/notification";

type ModerationReasonDialogProps = {
  open: boolean;
  status: ModerationStatus | null;
  reason: string | null;
  onClose: () => void;
};

const TITLE_KEYS: Record<ModerationStatus, TranslationKey> = {
  APPROVED: "moderation.title.APPROVED",
  DENIED: "moderation.title.DENIED",
  SEND_ADMIN: "moderation.title.SEND_ADMIN",
};

const FALLBACK_REASON_KEYS: Record<ModerationStatus, TranslationKey> = {
  APPROVED: "moderation.reason.APPROVED",
  DENIED: "moderation.reason.DENIED",
  SEND_ADMIN: "moderation.reason.SEND_ADMIN",
};

export function ModerationReasonDialog({
  open,
  status,
  reason,
  onClose,
}: ModerationReasonDialogProps) {
  const { t } = useI18n();
  const effectiveStatus: ModerationStatus = status ?? "SEND_ADMIN";
  const title = t(TITLE_KEYS[effectiveStatus]);
  const body = reason?.trim()
    ? reason
    : t(FALLBACK_REASON_KEYS[effectiveStatus]);

  return (
    <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
      <DialogContent className="flex max-w-md flex-col gap-4 p-6">
        <div className="space-y-2">
          <DialogTitle className="text-lg font-semibold">{title}</DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground">
            {body}
          </DialogDescription>
        </div>
        <div className="flex justify-end">
          <Button onClick={onClose}>{t("moderation.acknowledge")}</Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
