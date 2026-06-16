"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  BadgeDollarSignIcon,
  CoffeeIcon,
  CreditCardIcon,
  FileTextIcon,
  FlagIcon,
  LayoutDashboardIcon,
  MessageSquareIcon,
  ShieldCheckIcon,
  UsersIcon,
} from "lucide-react";
import { BrandIcon } from "@/components/ui/brand-icon";
import { cn } from "@/lib/utils";
import type { AuthUser } from "@/types/auth";

const sidebarLinks = [
  { href: "/", icon: LayoutDashboardIcon, label: "Overview" },
  { href: "/users", icon: UsersIcon, label: "Users" },
  { href: "/blogs", icon: FileTextIcon, label: "Blogs" },
  { href: "/cafes", icon: CoffeeIcon, label: "Cafes" },
  { href: "/comments", icon: MessageSquareIcon, label: "Comments" },
  { href: "/moderation", icon: ShieldCheckIcon, label: "Moderation" },
  { href: "/reports", icon: FlagIcon, label: "Reports" },
  { href: "/payments", icon: CreditCardIcon, label: "Payments" },
  { href: "/extra-fees", icon: BadgeDollarSignIcon, label: "Extra Fees" },
];

function isActivePath(pathname: string, href: string) {
  if (href === "/") {
    return pathname === "/" || pathname === "/admin";
  }

  return pathname === href || pathname.startsWith(`${href}/`);
}

export function AdminSidebar({ user }: { user: AuthUser }) {
  const pathname = usePathname();

  return (
    <aside className="border-border bg-surface px-4 py-5 lg:fixed lg:inset-y-0 lg:left-0 lg:w-72 lg:border-r">
      <div className="flex h-full flex-col gap-6">
        <div className="flex items-center gap-3">
          <BrandIcon className="size-11 shadow-sm" />
          <div className="min-w-0">
            <p className="text-sm font-black text-espresso">CafeStory Admin</p>
            <p className="truncate text-xs font-semibold text-muted">
              Operations workspace
            </p>
          </div>
        </div>

        <div className="rounded-md border border-line-soft bg-background p-4">
          <div className="min-w-0">
            <p className="truncate text-sm font-black text-espresso">
              {user.userFullName || user.userName}
            </p>
            <p className="truncate text-xs font-semibold text-muted">
              {user.userEmail}
            </p>
          </div>
        </div>

        <nav className="flex flex-col gap-1.5">
          {sidebarLinks.map(({ href, icon: Icon, label }) => {
            const isActive = isActivePath(pathname, href);

            return (
              <Link
                aria-current={isActive ? "page" : undefined}
                className={cn(
                  "flex min-h-11 items-center gap-3 rounded-md px-3 text-left text-sm font-semibold no-underline transition",
                  isActive
                    ? "bg-primary/10 text-primary"
                    : "text-muted hover:bg-surface-muted hover:text-primary",
                )}
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
          <ShieldCheckIcon className="size-5" />
          <p className="mt-3 text-sm font-black">Admin controls</p>
          <p className="mt-1 text-xs leading-5 text-white/72">
            Review operational queues and act on live platform data.
          </p>
        </div>
      </div>
    </aside>
  );
}
