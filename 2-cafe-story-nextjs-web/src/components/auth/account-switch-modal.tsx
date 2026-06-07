"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { X } from "lucide-react";
import { logout } from "@/lib/api/auth";
import {
  getUserAvatarImage,
  getUserDisplayName,
  getUserEmail,
  getUserHandle,
} from "@/lib/avatar";
import type { AuthUser } from "@/types/auth";
import { AvatarImage } from "@/components/ui/avatar-image";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { useAuth } from "@/components/providers/auth-provider";

type AccountSwitchModalProps = {
  isOpen: boolean;
  onClose: () => void;
  user: AuthUser | null;
};

export function AccountSwitchModal({
  isOpen,
  onClose,
  user,
}: AccountSwitchModalProps) {
  const router = useRouter();
  const { setUser } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const displayName = getUserDisplayName(user);
  const handle = getUserHandle(user);
  const email = getUserEmail(user);

  async function handleLogout() {
    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      setUser?.(null);
      router.replace("/login");
      router.refresh();
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="w-[min(360px,calc(100vw-32px))] p-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <DialogTitle className="text-lg font-black text-foreground">
              Account
            </DialogTitle>
            <DialogDescription className="mt-1">
              Cafe Story session
            </DialogDescription>
          </div>
          <DialogClose asChild>
            <Button
              aria-label="Close"
              className="text-muted hover:text-foreground"
              size="icon-sm"
              type="button"
              variant="ghost"
            >
              <X aria-hidden="true" />
            </Button>
          </DialogClose>
        </div>

        <div className="mt-5 flex items-center gap-3 rounded-md bg-surface-muted p-3">
          <AvatarImage
            alt={`${displayName} avatar`}
            className="h-12 w-12 rounded-full object-cover"
            src={getUserAvatarImage(user)}
          />
          <div className="min-w-0">
            <p className="truncate text-sm font-black text-foreground">
              {handle}
            </p>
            <p className="truncate text-sm text-muted">{displayName}</p>
            {email ? (
              <p className="truncate text-xs text-muted">{email}</p>
            ) : null}
          </div>
        </div>

        <Button
          className="mt-5 w-full font-black"
          disabled={isLoggingOut}
          onClick={handleLogout}
          size="lg"
          type="button"
        >
          {isLoggingOut ? "Signing out..." : "Logout"}
        </Button>
      </DialogContent>
    </Dialog>
  );
}
