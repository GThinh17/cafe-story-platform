"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  ArrowLeftIcon,
  BarChart3Icon,
  BadgeIcon,
  CoffeeIcon,
  LayoutDashboardIcon,
  TrophyIcon,
  WalletIcon,
} from "lucide-react";
import { BrandIcon } from "@/components/ui/brand-icon";
import type { ReviewerProfile } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerDashboardSidebarProps = {
  profile: ReviewerProfile;
};

const sidebarLinks = [
  { href: "/reviewer-dashboard", icon: LayoutDashboardIcon, label: "Overview" },
  {
    href: "/reviewer-dashboard/performance",
    icon: BarChart3Icon,
    label: "Performance",
  },
  { href: "/reviewer-dashboard/ranking", icon: TrophyIcon, label: "Ranking" },
  { href: "/reviewer-dashboard/badges", icon: BadgeIcon, label: "Badges" },
  { href: "/reviewer-dashboard/earnings", icon: WalletIcon, label: "Earnings" },
];

export function ReviewerDashboardSidebar({
  profile,
}: ReviewerDashboardSidebarProps) {
  const pathname = usePathname();

  return (
    <aside className="border-border bg-surface px-4 py-5 lg:fixed lg:inset-y-0 lg:left-0 lg:w-72 lg:border-r">
      <div className="flex h-full flex-col gap-6">
        <div className="flex items-center gap-3">
          <BrandIcon className="size-11 shadow-sm" />
          <div className="min-w-0">
            <p className="text-sm font-black text-espresso">CafeStory</p>
            <p className="truncate text-xs font-semibold text-muted">
              Reviewer workspace
            </p>
          </div>
        </div>

        <Link
          className="inline-flex min-h-11 items-center justify-center gap-2 rounded-md border border-border bg-background px-4 text-sm font-black text-foreground no-underline transition hover:border-primary hover:text-primary"
          href="/"
        >
          <ArrowLeftIcon className="size-4" />
          Home
        </Link>

        <div className="rounded-md border border-line-soft bg-background p-4">
          <div className="flex items-center gap-3">
            <img
              alt={`${profile.name} avatar`}
              className="size-12 shrink-0 rounded-full border border-border object-cover"
              decoding="async"
              src={profile.avatar}
            />
            <div className="min-w-0">
              <p className="truncate text-sm font-black text-espresso">
                {profile.name}
              </p>
              <p className="truncate text-xs font-semibold text-muted">
                {profile.badge} Reviewer
              </p>
            </div>
          </div>
        </div>

        <nav className="flex flex-col gap-1.5">
          {sidebarLinks.map(({ href, icon: Icon, label }) => {
            const isActive =
              href === "/reviewer-dashboard"
                ? pathname === href
                : pathname === href || pathname.startsWith(`${href}/`);

            return (
            <Link
              aria-current={isActive ? "page" : undefined}
              className={`flex min-h-11 items-center gap-3 rounded-md px-3 text-left text-sm font-semibold no-underline transition ${
                isActive
                  ? "bg-primary/10 text-primary"
                  : "text-muted hover:bg-surface-muted hover:text-primary"
              }`}
              href={href}
              key={label}
            >
              <Icon className="size-4" />
              {label}
            </Link>
            );
          })}
        </nav>

        <div className="mt-auto rounded-md bg-espresso px-4 py-4 text-white">
          <CoffeeIcon className="size-5" />
          <p className="mt-3 text-sm font-black">Keep reviewing</p>
          <p className="mt-1 text-xs leading-5 text-white/72">
            Your next cafe story can move you closer to DIAMOND.
          </p>
        </div>
      </div>
    </aside>
  );
}
