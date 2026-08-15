"use client";

import type { ReactNode } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { useUiText } from "@/features/i18n";

type AdminConfirmDialogProps = {
  open: boolean;
  title: string;
  description: string;
  confirmLabel?: string;
  isSubmitting?: boolean;
  children?: ReactNode;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void | Promise<void>;
};

export function AdminConfirmDialog({
  open,
  title,
  description,
  confirmLabel = "Confirm",
  isSubmitting = false,
  children,
  onOpenChange,
  onConfirm,
}: AdminConfirmDialogProps) {
  const ui = useUiText();
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md p-5">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <DialogTitle>{ui(title)}</DialogTitle>
            <DialogDescription>{ui(description)}</DialogDescription>
          </div>
          {children}
          <div className="flex justify-end gap-2">
            <DialogClose asChild>
              <Button type="button" variant="outline" disabled={isSubmitting}>
                {ui("Cancel")}
              </Button>
            </DialogClose>
            <Button type="button" disabled={isSubmitting} onClick={onConfirm}>
              {isSubmitting ? ui("Working...") : ui(confirmLabel)}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
