"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";
import { logout } from "@/lib/api/auth";
import type { AuthUser } from "@/types/auth";
import { useAuth } from "@/components/providers/auth-provider";

type ProfileSettingsModalProps = {
  cafePageHref?: string;
  currentUser?: AuthUser | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
};

export function ProfileSettingsModal({
  cafePageHref,
  currentUser,
  open,
  onOpenChange,
}: ProfileSettingsModalProps) {
  const router = useRouter();
  const { setUser } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const shouldShowReviewerDashboard = isReviewer(currentUser);
  const shouldShowCafePage = isCafePageRole(currentUser) || Boolean(cafePageHref);
  const currentUsername = currentUser?.userName?.trim();
  const resolvedCafePageHref = cafePageHref ?? "/cafes/edit";

  async function handleLogout() {
    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      setUser?.(null);
      onOpenChange(false);
      router.replace("/login");
      router.refresh();
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="w-[min(360px,calc(100vw-32px))] p-0">
        <DialogTitle className="sr-only">Profile settings</DialogTitle>
        <DialogDescription className="sr-only">
          Choose a profile settings action.
        </DialogDescription>
        <div className="flex flex-col text-center">
          {currentUsername ? (
            <DialogClose asChild>
              <Link
                className="flex min-h-12 items-center justify-center px-6 text-sm font-semibold text-foreground transition hover:bg-surface-muted"
                href={`/${currentUsername}/edit`}
              >
                Edit profile
              </Link>
            </DialogClose>
          ) : null}
          {shouldShowReviewerDashboard ? (
            <>
              <Separator />
              <DialogClose asChild>
                <Link
                  className="flex min-h-12 items-center justify-center px-6 text-sm font-semibold text-foreground transition hover:bg-surface-muted"
                  href="/reviewer-dashboard"
                >
                  Reviewer dashboard
                </Link>
              </DialogClose>
            </>
          ) : null}
          {shouldShowCafePage ? (
            <>
              <Separator />
              <DialogClose asChild>
                <Link
                  className="flex min-h-12 items-center justify-center px-6 text-sm font-semibold text-foreground transition hover:bg-surface-muted"
                  href={resolvedCafePageHref}
                >
                  Cafe page
                </Link>
              </DialogClose>
            </>
          ) : null}
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

function isReviewer(user: AuthUser | null | undefined) {
  return Boolean(
    user?.roles?.some((role) => role.toLowerCase().includes("reviewer")),
  );
}

export function isCafePageRole(user: AuthUser | null | undefined) {
  return Boolean(
    user?.roles?.some((role) => {
      const normalizedRole = role.toLowerCase();

      return (
        normalizedRole.includes("cafe") ||
        normalizedRole.includes("page_owner") ||
        normalizedRole === "cafe_page"
      );
    }),
  );
}
