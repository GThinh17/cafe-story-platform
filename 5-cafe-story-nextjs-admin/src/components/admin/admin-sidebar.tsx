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
  FlaskConicalIcon,
  LayoutDashboardIcon,
  LogOutIcon,
  MapIcon,
  MenuIcon,
  MessageSquareIcon,
  ShieldCheckIcon,
  TrophyIcon,
  UsersIcon,
  WalletIcon,
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
import {
  LanguageSelector,
  useTranslate,
  type TranslationKey,
} from "@/features/i18n";
import { cn } from "@/lib/utils";
import type { AuthUser } from "@/types/auth";

const NAV_GROUPS: Array<{
  labelKey?: TranslationKey;
  items: Array<{
    href: string;
    icon: React.ElementType;
    labelKey: TranslationKey;
  }>;
}> = [
  {
    items: [
      { href: "/", icon: LayoutDashboardIcon, labelKey: "navigation.overview" },
    ],
  },
  {
    labelKey: "navigation.content",
    items: [
      { href: "/blogs", icon: FileTextIcon, labelKey: "navigation.blogs" },
      { href: "/cafes", icon: CoffeeIcon, labelKey: "navigation.cafes" },
      { href: "/comments", icon: MessageSquareIcon, labelKey: "navigation.comments" },
    ],
  },
  {
    labelKey: "navigation.operations",
    items: [
      { href: "/moderation", icon: ShieldCheckIcon, labelKey: "navigation.moderation" },
      { href: "/reports", icon: FlagIcon, labelKey: "navigation.reports" },
      { href: "/ranking", icon: TrophyIcon, labelKey: "navigation.ranking" },
      { href: "/users", icon: UsersIcon, labelKey: "navigation.users" },
      { href: "/regions", icon: MapIcon, labelKey: "navigation.regions" },
    ],
  },
  {
    labelKey: "navigation.finance",
    items: [
      { href: "/payments", icon: CreditCardIcon, labelKey: "navigation.payments" },
      { href: "/extra-fees", icon: BadgeDollarSignIcon, labelKey: "navigation.extraFees" },
      { href: "/payout", icon: WalletIcon, labelKey: "navigation.payouts" },
      { href: "/formulas", icon: FlaskConicalIcon, labelKey: "navigation.formulas" },
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
  const t = useTranslate();
  return (
    <nav className="flex flex-col gap-4">
      {NAV_GROUPS.map((group, index) => (
        <div key={index} className="flex flex-col gap-0.5">
          {group.labelKey ? (
            <p className="px-2.5 pb-1 text-[10px] font-semibold uppercase tracking-widest text-muted/60">
              {t(group.labelKey)}
            </p>
          ) : null}
          {group.items.map(({ href, icon: Icon, labelKey }) => {
            const isActive = isActivePath(pathname, href);
            return (
              <Link
                aria-current={isActive ? "page" : undefined}
                className={cn(
                  "relative flex h-9 items-center gap-2.5 rounded-md px-2.5 text-[13px] font-medium no-underline transition",
                  isActive
                    ? "bg-sidebar-accent text-sidebar-accent-foreground before:absolute before:inset-y-1.5 before:left-0 before:w-0.5 before:rounded-full before:bg-primary"
                    : "text-sidebar-foreground hover:bg-surface-muted hover:text-foreground",
                )}
                href={href}
                key={labelKey}
                onClick={onNavigate}
              >
                <Icon className="size-4 shrink-0" />
                {t(labelKey)}
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
      <div className="flex items-center gap-2.5">
        <BrandIcon className="size-8" />
        <div className="min-w-0">
          <p className="text-sm font-semibold text-espresso">CafeStory Admin</p>
          <p className="truncate text-[11px] text-muted">
            {user.userFullName || user.userName}
          </p>
        </div>
      </div>
    </>
  );
}

function SignOutButton({ onSignOut }: { onSignOut: () => void }) {
  const t = useTranslate();
  return (
    <button
      type="button"
      className="flex h-9 w-full items-center gap-2.5 rounded-md px-2.5 text-[13px] font-medium text-sidebar-foreground transition hover:bg-surface-muted hover:text-foreground"
      onClick={onSignOut}
    >
      <LogOutIcon className="size-4 shrink-0" />
      {t("navigation.signOut")}
    </button>
  );
}

export function AdminSidebar({ user }: { user: AuthUser }) {
  const pathname = usePathname();
  const router = useRouter();
  const { setUser } = useCurrentUser();
  const [mobileOpen, setMobileOpen] = useState(false);
  const t = useTranslate();

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
          aria-label={t("navigation.open")}
          onClick={() => setMobileOpen(true)}
        >
          <MenuIcon />
        </Button>
        <BrandIcon className="size-7" />
        <p className="text-sm font-semibold text-espresso">CafeStory Admin</p>
      </div>

      {/* Mobile drawer */}
      <Sheet open={mobileOpen} onOpenChange={setMobileOpen}>
        <SheetContent side="left" className="w-72 p-0" showCloseButton={false}>
          <div className="flex h-full flex-col gap-5 overflow-y-auto px-4 py-5">
            <SheetHeader className="p-0 gap-4">
              <SheetTitle className="sr-only">{t("navigation.label")}</SheetTitle>
              <BrandBlock user={user} />
            </SheetHeader>
            <NavContent pathname={pathname} onNavigate={() => setMobileOpen(false)} />
            <div className="mt-auto border-t border-border pt-3">
              <LanguageSelector className="mb-3" />
              <SignOutButton onSignOut={handleSignOut} />
            </div>
          </div>
        </SheetContent>
      </Sheet>

      {/* Desktop sidebar */}
      <aside className="hidden border-r border-sidebar-border bg-sidebar px-3 py-4 lg:fixed lg:inset-y-0 lg:left-0 lg:flex lg:w-60 lg:flex-col">
        <div className="flex h-full flex-col gap-4 overflow-y-auto">
          <BrandBlock user={user} />
          <NavContent pathname={pathname} />
          <div className="mt-auto border-t border-border pt-3">
            <LanguageSelector className="mb-3" />
            <SignOutButton onSignOut={handleSignOut} />
          </div>
        </div>
      </aside>
    </>
  );
}
