"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  BellIcon,
  CompassIcon,
  HomeIcon,
  PlusIcon,
  UserIcon,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { useCurrentUser } from "@/hooks/use-current-user";
import { useCreatePost } from "@/context/create-post-context";
import { cn } from "@/lib/utils";

type NavItem = {
  href: string;
  label: string;
  icon: LucideIcon;
};

function isActivePath(pathname: string, href: string) {
  if (href === "/") return pathname === "/";
  return pathname === href || pathname.startsWith(`${href}/`);
}

export function BottomNav() {
  const pathname = usePathname();
  const { user, isLoading } = useCurrentUser();
  const { open: openCreatePost } = useCreatePost();
  const profileHref = !isLoading && user?.userName ? `/${user.userName}` : "/login";

  const items: NavItem[] = [
    { href: "/", label: "Home", icon: HomeIcon },
    { href: "/explore", label: "Explore", icon: CompassIcon },
    { href: "/notifications", label: "Activity", icon: BellIcon },
    { href: profileHref, label: "Profile", icon: UserIcon },
  ];

  return (
    <nav
      aria-label="Mobile navigation"
      className="fixed inset-x-0 bottom-0 z-50 border-t border-border bg-surface sm:hidden"
    >
      <div className="flex h-[60px] items-center">
        <button
          aria-label="Create Post"
          className="flex flex-1 flex-col items-center justify-center py-2"
          onClick={openCreatePost}
          title="Create Post"
          type="button"
        >
          <span className="grid size-9 place-items-center rounded-full bg-primary text-primary-foreground shadow-sm">
            <PlusIcon className="size-[18px]" strokeWidth={2.5} />
          </span>
        </button>

        {items.map(({ href, label, icon: Icon }) => {
          const isActive = isActivePath(pathname, href);

          return (
            <Link
              key={href}
              aria-current={isActive ? "page" : undefined}
              aria-label={label}
              className={cn(
                "flex flex-1 flex-col items-center justify-center gap-1 py-2 transition-colors",
                isActive ? "text-primary-strong" : "text-muted",
              )}
              href={href}
              title={label}
            >
              <Icon className="size-5" strokeWidth={isActive ? 2.5 : 2} />
              <span className="text-[10px] font-medium leading-none">{label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
