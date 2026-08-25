"use client";

import Link from "next/link";
import { useState } from "react";
import { AccountSwitchModal } from "@/components/auth/account-switch-modal";
import { AvatarImage } from "@/components/ui/avatar-image";
import { useI18n } from "@/components/providers/locale-provider";
import { useCurrentUser } from "@/hooks/use-current-user";
import {
  getUserAvatarImage,
  getUserDisplayName,
  getUserHandle,
  getUserInitials,
} from "@/lib/avatar";

export function HomeAccountPanel() {
  const { t } = useI18n();
  const { user, isLoading } = useCurrentUser();
  const [isSwitchOpen, setIsSwitchOpen] = useState(false);

  if (!isLoading && !user) {
    return (
      <div className="flex items-center justify-between gap-3">
        <span className="min-w-0 flex-1">
          <span className="block truncate text-sm font-bold">
            Cafe Story
          </span>
          <span className="block truncate text-sm text-muted">
            Sign in to personalize your feed
          </span>
        </span>
        <Link className="text-xs font-bold text-primary" href="/login">
          Login
        </Link>
      </div>
    );
  }

  const displayName = getUserDisplayName(user);
  const handle = getUserHandle(user);

  return (
    <>
      <div className="flex items-center gap-3">
        <span className="relative grid h-12 w-12 shrink-0 place-items-center overflow-hidden rounded-full bg-surface-muted text-sm font-bold text-primary-strong">
          {user ? (
            <AvatarImage
              alt={t("post.avatarAlt", { name: displayName })}
              className="h-full w-full rounded-full object-cover"
              src={getUserAvatarImage(user)}
            />
          ) : (
            getUserInitials(user)
          )}
        </span>
        <span className="min-w-0 flex-1">
          <span className="block truncate text-sm font-bold">
            {isLoading ? t("accountPanel.loading") : handle}
          </span>
          <span className="block truncate text-sm text-muted">
            {isLoading ? "Cafe Story" : displayName}
          </span>
        </span>
        <button
          className="text-xs font-bold text-primary transition hover:text-primary-strong"
          onClick={() => setIsSwitchOpen(true)}
          type="button"
        >
          {t("account.switch")}
        </button>
      </div>

      <AccountSwitchModal
        isOpen={isSwitchOpen}
        onClose={() => setIsSwitchOpen(false)}
        user={user}
      />
    </>
  );
}
