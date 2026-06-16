"use client";

import type { ReactNode } from "react";
import { XIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

type AdminDetailDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  isLoading?: boolean;
  error?: string | null;
  children: ReactNode;
  footer?: ReactNode;
  className?: string;
};

export function AdminDetailDialog({
  open,
  onOpenChange,
  title,
  description,
  isLoading = false,
  error,
  children,
  footer,
  className,
}: AdminDetailDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className={cn(
          "max-h-[92vh] w-[94vw] max-w-5xl grid-rows-[auto_minmax(0,1fr)_auto] p-0",
          className,
        )}
      >
        <div className="flex items-start justify-between gap-4 border-b border-border px-5 py-4">
          <div className="min-w-0">
            <DialogTitle className="truncate">{title}</DialogTitle>
            {description ? (
              <DialogDescription className="mt-1">{description}</DialogDescription>
            ) : null}
          </div>
          <Button
            type="button"
            variant="ghost"
            size="icon-sm"
            aria-label="Close"
            onClick={() => onOpenChange(false)}
          >
            <XIcon />
          </Button>
        </div>
        <div className="overflow-y-auto px-5 py-4">
          {isLoading ? (
            <div className="flex flex-col gap-3">
              {Array.from({ length: 5 }).map((_, index) => (
                <Skeleton className="h-12 w-full" key={index} />
              ))}
            </div>
          ) : error ? (
            <div className="rounded-md border border-border bg-surface-muted p-4">
              <p className="text-sm font-bold text-accent">Request failed</p>
              <p className="mt-2 text-sm text-muted">{error}</p>
            </div>
          ) : (
            children
          )}
        </div>
        {footer ? (
          <div className="border-t border-border px-5 py-4">{footer}</div>
        ) : null}
      </DialogContent>
    </Dialog>
  );
}

export function AdminDetailGrid({ children }: { children: ReactNode }) {
  return <dl className="grid gap-3 sm:grid-cols-2">{children}</dl>;
}

export function AdminDetailField({
  label,
  children,
  className,
}: {
  label: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <div className={cn("min-w-0 rounded-md border border-border bg-background p-3", className)}>
      <dt className="text-xs font-black uppercase tracking-[0.08em] text-muted">
        {label}
      </dt>
      <dd className="mt-1 break-words text-sm text-foreground">{children}</dd>
    </div>
  );
}

