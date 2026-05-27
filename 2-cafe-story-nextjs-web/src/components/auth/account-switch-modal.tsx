"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { logout } from "@/lib/api/auth";
import {
  getUserAvatarImage,
  getUserDisplayName,
  getUserEmail,
  getUserHandle,
} from "@/lib/avatar";
import type { AuthUser } from "@/types/auth";
import { AvatarImage } from "@/components/ui/avatar-image";

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
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const displayName = getUserDisplayName(user);
  const handle = getUserHandle(user);
  const email = getUserEmail(user);

  if (!isOpen) {
    return null;
  }

  async function handleLogout() {
    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      router.replace("/login");
      router.refresh();
    }
  }

  return (
    <div
      aria-labelledby="account-switch-title"
      aria-modal="true"
      className="fixed inset-0 z-50 grid place-items-center bg-black/35 px-4"
      role="dialog"
    >
      <button
        aria-label="Close account switcher"
        className="absolute inset-0 cursor-default"
        onClick={onClose}
        type="button"
      />
      <section className="relative w-full max-w-[360px] rounded-lg border border-border bg-surface p-5 shadow-2xl">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p
              className="text-lg font-black text-foreground"
              id="account-switch-title"
            >
              Account
            </p>
            <p className="mt-1 text-sm text-muted">Cafe Story session</p>
          </div>
          <button
            aria-label="Close"
            className="grid h-8 w-8 place-items-center rounded-full text-xl leading-none text-muted transition hover:bg-surface-muted hover:text-foreground"
            onClick={onClose}
            type="button"
          >
            x
          </button>
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

        <button
          className="mt-5 h-11 w-full rounded-md bg-espresso px-4 text-sm font-black text-white transition hover:bg-primary-strong disabled:cursor-not-allowed disabled:opacity-70"
          disabled={isLoggingOut}
          onClick={handleLogout}
          type="button"
        >
          {isLoggingOut ? "Signing out..." : "Logout"}
        </button>
      </section>
    </div>
  );
}
