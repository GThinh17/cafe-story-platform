"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogTitle,
} from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";
import { logout } from "@/lib/api/auth";

type ProfileSettingsModalProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
};

export function ProfileSettingsModal({
  open,
  onOpenChange,
}: ProfileSettingsModalProps) {
  const router = useRouter();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  async function handleLogout() {
    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      onOpenChange(false);
      router.replace("/login");
      router.refresh();
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="w-[min(360px,calc(100vw-32px))] p-0">
        <DialogTitle className="sr-only">Profile settings</DialogTitle>
        <div className="flex flex-col text-center">
          <DialogClose asChild>
            <Link
              className="flex min-h-12 items-center justify-center px-6 text-sm font-semibold text-foreground transition hover:bg-surface-muted"
              href="/profile/edit"
            >
              Edit profile
            </Link>
          </DialogClose>
          <Separator />
          <DialogClose asChild>
            <Link
              className="flex min-h-12 items-center justify-center px-6 text-sm font-semibold text-foreground transition hover:bg-surface-muted"
              href="/reviewer-dashboard"
            >
              Reviewer dashboard
            </Link>
          </DialogClose>
          <Separator />
          <button
            className="min-h-12 px-6 text-sm font-semibold text-accent transition hover:bg-surface-muted"
            disabled={isLoggingOut}
            onClick={handleLogout}
            type="button"
          >
            {isLoggingOut ? "Logging out..." : "Log out"}
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
  );
}
