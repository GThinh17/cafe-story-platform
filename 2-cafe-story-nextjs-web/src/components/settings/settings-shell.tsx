"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Store, User } from "lucide-react";
import type { ReactNode } from "react";
import { useCurrentUser } from "@/hooks/use-current-user";
import { cn } from "@/lib/utils";

type SettingsTab = {
  key: "profile" | "cafe";
  label: string;
  description: string;
  href: string | null;
  icon: typeof User;
  disabled?: boolean;
  disabledReason?: string;
};

function useSettingsTabs(): SettingsTab[] {
  const { user, isLoading } = useCurrentUser();
  const username = user?.userName;

  return [
    {
      key: "profile",
      label: "Profile",
      description: "Chỉnh sửa hồ sơ cá nhân",
      href: username ? `/${encodeURIComponent(username)}/edit` : null,
      icon: User,
      disabled: !username && !isLoading,
      disabledReason: "Bạn cần đăng nhập",
    },
    {
      key: "cafe",
      label: "Trang quán",
      description: "Chỉnh sửa cafe page & cộng sự",
      href: "/cafes/edit",
      icon: Store,
    },
  ];
}

function isTabActive(tab: SettingsTab, pathname: string | null) {
  if (!pathname) return false;
  if (tab.key === "cafe") return pathname.startsWith("/cafes/edit");
  if (tab.key === "profile") return pathname.endsWith("/edit") && !pathname.startsWith("/cafes/");
  return false;
}

type SettingsShellProps = {
  children: ReactNode;
};

export function SettingsShell({ children }: SettingsShellProps) {
  const pathname = usePathname();
  const tabs = useSettingsTabs();

  return (
    <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-6 overflow-x-clip px-4 py-6 sm:px-8 md:grid-cols-[220px_minmax(0,1fr)] md:gap-10 md:py-8 xl:ml-12 xl:px-0 2xl:ml-20">
      <aside className="md:sticky md:top-6 md:h-fit">
        <p className="hidden text-xs font-semibold uppercase tracking-wider text-muted md:block">
          Cài đặt
        </p>
        <nav
          aria-label="Settings sections"
          className="mt-3 flex gap-2 overflow-x-auto pb-1 md:flex-col md:overflow-visible md:pb-0"
        >
          {tabs.map((tab) => {
            const active = isTabActive(tab, pathname);
            const Icon = tab.icon;
            const commonClass = cn(
              "group inline-flex shrink-0 items-center gap-3 rounded-md border border-transparent px-3 py-2 text-left text-sm transition-colors md:w-full",
              active
                ? "border-border bg-surface text-primary-strong"
                : "text-foreground hover:bg-surface-muted",
              tab.disabled && "cursor-not-allowed opacity-50",
            );

            if (tab.disabled || !tab.href) {
              return (
                <button
                  aria-disabled
                  className={commonClass}
                  disabled
                  key={tab.key}
                  title={tab.disabledReason}
                  type="button"
                >
                  <Icon className="size-4 shrink-0" />
                  <span className="min-w-0">
                    <span className={cn("block font-semibold", active && "text-primary-strong")}>
                      {tab.label}
                    </span>
                    <span className="hidden text-xs text-muted md:block">{tab.description}</span>
                  </span>
                </button>
              );
            }

            return (
              <Link
                aria-current={active ? "page" : undefined}
                className={commonClass}
                href={tab.href}
                key={tab.key}
              >
                <Icon
                  className={cn(
                    "size-4 shrink-0",
                    active ? "text-primary-strong" : "text-muted group-hover:text-foreground",
                  )}
                />
                <span className="min-w-0">
                  <span className={cn("block font-semibold", active && "text-primary-strong")}>
                    {tab.label}
                  </span>
                  <span className="hidden text-xs text-muted md:block">{tab.description}</span>
                </span>
              </Link>
            );
          })}
        </nav>
      </aside>

      <section className="min-w-0 space-y-6">{children}</section>
    </main>
  );
}
