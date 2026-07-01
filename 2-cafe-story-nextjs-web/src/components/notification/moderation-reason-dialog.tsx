"use client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import type { ModerationStatus } from "@/types/notification";

type ModerationReasonDialogProps = {
  open: boolean;
  status: ModerationStatus | null;
  reason: string | null;
  onClose: () => void;
};

const TITLES: Record<ModerationStatus, string> = {
  APPROVED: "Bài đăng đã được duyệt",
  DENIED: "Bài đăng đã bị từ chối",
  SEND_ADMIN: "Bài đăng cần admin xem xét",
};

const FALLBACK_REASON: Record<ModerationStatus, string> = {
  APPROVED: "Bài của bạn đã được đăng công khai.",
  DENIED: "Bài của bạn không phù hợp với chính sách nội dung.",
  SEND_ADMIN: "Bài của bạn cần được admin duyệt thêm trước khi công khai.",
};

export function ModerationReasonDialog({
  open,
  status,
  reason,
  onClose,
}: ModerationReasonDialogProps) {
  const effectiveStatus: ModerationStatus = status ?? "SEND_ADMIN";
  const title = TITLES[effectiveStatus];
  const body = reason?.trim() ? reason : FALLBACK_REASON[effectiveStatus];

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
          <Button onClick={onClose}>Đã hiểu</Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
