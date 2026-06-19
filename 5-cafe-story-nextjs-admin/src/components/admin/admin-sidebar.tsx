"use client";

import Link from "next/link";
import { useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import {
  BadgeDollarSignIcon,
  CoffeeIcon,
  CreditCardIcon,
  FileTextIcon,
  FlagIcon,
  LayoutDashboardIcon,
  LogOutIcon,
  MenuIcon,
  MessageSquareIcon,
  ShieldCheckIcon,
  TrophyIcon,
  UsersIcon,
} from "lucide-react";
import { BrandIcon } from "@/components/ui/brand-icon";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { useCurrentUser } from "@/hooks/use-current-user";
import { cn } from "@/lib/utils";
import type { AuthUser } from "@/types/auth";

const NAV_GROUPS: Array<{
  label?: string;
  items: Array<{ href: string; icon: React.ElementType; label: string }>;
}> = [
  {
    items: [{ href: "/", icon: LayoutDashboardIcon, label: "Overview" }],
  },
  {
    label: "Content",
    items: [
      { href: "/blogs", icon: FileTextIcon, label: "Blogs" },
      { href: "/cafes", icon: CoffeeIcon, label: "Cafes" },
      { href: "/comments", icon: MessageSquareIcon, label: "Comments" },
    ],
  },
  {
    label: "Operations",
    items: [
      { href: "/moderation", icon: ShieldCheckIcon, label: "Moderation" },
      { href: "/reports", icon: FlagIcon, label: "Reports" },
      { href: "/ranking", icon: TrophyIcon, label: "Ranking" },
      { href: "/users", icon: UsersIcon, label: "Users" },
    ],
  },
  {
    label: "Finance",
    items: [
      { href: "/payments", icon: CreditCardIcon, label: "Payments" },
      { href: "/extra-fees", icon: BadgeDollarSignIcon, label: "Extra Fees" },
    ],
  },
];

function isActivePath(pathname: string, href: string) {
  if (href === "/") {
    return pathname === "/" || pathname === "/admin";
  }
  return pathname === href || pathname.startsWith(`${href}/`);
}

function NavContent({
  pathname,
  onNavigate,
}: {
  pathname: string;
  onNavigate?: () => void;
}) {
  return (
    <nav className="flex flex-col gap-5">
      {NAV_GROUPS.map((group, index) => (
        <div key={index} className="flex flex-col gap-1">
          {group.label ? (
            <p className="px-3 pb-0.5 text-[11px] font-semibold uppercase tracking-widest text-muted/50">
              {group.label}
            </p>
          ) : null}
          {group.items.map(({ href, icon: Icon, label }) => {
            const isActive = isActivePath(pathname, href);
            return (
              <Link
                aria-current={isActive ? "page" : undefined}
                className={cn(
                  "flex min-h-10 items-center gap-3 rounded-md px-3 text-sm font-semibold no-underline transition",
                  isActive
                    ? "bg-primary/10 text-primary"
                    : "text-muted hover:bg-surface-muted hover:text-foreground",
                )}
                href={href}
                key={label}
                onClick={onNavigate}
              >
                <Icon className="size-4 shrink-0" />
                {label}
              </Link>
            );
          })}
        </div>
      ))}
    </nav>
  );
}

function BrandBlock({ user }: { user: AuthUser }) {
  return (
    <>
      <div className="flex items-center gap-3">
        <BrandIcon className="size-11 shadow-sm" />
        <div className="min-w-0">
          <p className="text-sm font-black text-espresso">CafeStory Admin</p>
          <p className="text-xs font-semibold text-muted">Operations workspace</p>
        </div>
      </div>
      <div className="rounded-md border border-line-soft bg-background p-3">
        <p className="truncate text-sm font-bold text-espresso">
          {user.userFullName || user.userName}
        </p>
        <p className="truncate text-xs font-semibold text-muted">{user.userEmail}</p>
      </div>
    </>
  );
}

function SignOutButton({ onSignOut }: { onSignOut: () => void }) {
  return (
    <button
      type="button"
      className="flex w-full min-h-10 items-center gap-3 rounded-md px-3 text-sm font-semibold text-muted transition hover:bg-surface-muted hover:text-foreground"
      onClick={onSignOut}
    >
      <LogOutIcon className="size-4 shrink-0" />
      Sign out
    </button>
  );
}

export function AdminSidebar({ user }: { user: AuthUser }) {
  const pathname = usePathname();
  const router = useRouter();
  const { setUser } = useCurrentUser();
  const [mobileOpen, setMobileOpen] = useState(false);

  function handleSignOut() {
    setUser?.(null);
    router.replace("/login");
  }

  return (
    <>
      {/* Mobile top bar */}
      <div className="sticky top-0 z-40 flex items-center gap-3 border-b border-border bg-surface px-4 py-3 lg:hidden">
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          aria-label="Open navigation"
          onClick={() => setMobileOpen(true)}
        >
          <MenuIcon />
        </Button>
        <BrandIcon className="size-7 shadow-sm" />
        <p className="text-sm font-black text-espresso">CafeStory Admin</p>
      </div>

      {/* Mobile drawer */}
      <Sheet open={mobileOpen} onOpenChange={setMobileOpen}>
        <SheetContent side="left" className="w-72 p-0" showCloseButton={false}>
          <div className="flex h-full flex-col gap-5 overflow-y-auto px-4 py-5">
            <SheetHeader className="p-0 gap-4">
              <SheetTitle className="sr-only">Navigation</SheetTitle>
              <BrandBlock user={user} />
            </SheetHeader>
            <NavContent pathname={pathname} onNavigate={() => setMobileOpen(false)} />
            <div className="mt-auto border-t border-border pt-3">
              <SignOutButton onSignOut={handleSignOut} />
            </div>
          </div>
        </SheetContent>
      </Sheet>

      {/* Desktop sidebar */}
      <aside className="hidden border-r border-border bg-surface px-4 py-5 lg:fixed lg:inset-y-0 lg:left-0 lg:flex lg:w-72 lg:flex-col">
        <div className="flex h-full flex-col gap-5 overflow-y-auto">
          <BrandBlock user={user} />
          <NavContent pathname={pathname} />
          <div className="mt-auto border-t border-border pt-3">
            <SignOutButton onSignOut={handleSignOut} />
          </div>
        </div>
      </aside>
    </>
  );
}
