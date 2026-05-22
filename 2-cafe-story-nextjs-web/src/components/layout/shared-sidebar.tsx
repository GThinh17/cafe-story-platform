"use client";

import { BrandIcon } from "@/components/ui/brand-icon";
import { usePathname } from "next/navigation";

type SidebarItem = {
  href: string;
  label: string;
  icon: "home" | "explore" | "bell" | "message" | "profile" | "plus";
  variant?: "primary";
};

const sidebarItems: SidebarItem[] = [
  { href: "/", label: "Home", icon: "home" },
  { href: "/explore", label: "Explore", icon: "explore" },
  { href: "/notifications", label: "Notifications", icon: "bell" },
  { href: "/messages", label: "Messages", icon: "message" },
  { href: "/profile", label: "Profile", icon: "profile" },
  { href: "/reviews/new", label: "Create Post", icon: "plus", variant: "primary" },
];

const iconPaths: Record<SidebarItem["icon"], string[]> = {
  home: [
    "M3 10.5 12 3l9 7.5",
    "M5 9.5V21h5v-6h4v6h5V9.5",
  ],
  explore: [
    "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z",
    "m15.5 8.5-2 5-5 2 2-5 5-2Z",
  ],
  bell: [
    "M18 16v-5a6 6 0 0 0-12 0v5l-2 2h16l-2-2Z",
    "M10 20a2 2 0 0 0 4 0",
  ],
  message: [
    "M4 5.5h16v11H8l-4 4v-15Z",
    "M8 9h8",
    "M8 13h5",
  ],
  profile: [
    "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z",
    "M4.5 21a7.5 7.5 0 0 1 15 0",
  ],
  plus: ["M12 5v14", "M5 12h14"],
};

function SidebarIcon({ name }: { name: SidebarItem["icon"] }) {
  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="2"
      viewBox="0 0 24 24"
    >
      {iconPaths[name].map((path) => (
        <path d={path} key={path} />
      ))}
    </svg>
  );
}

function isActivePath(pathname: string, href: string) {
  if (href === "/") {
    return pathname === "/";
  }

  return pathname === href || pathname.startsWith(`${href}/`);
}

export function SharedSidebar() {
  const pathname = usePathname();

  return (
    <aside
      className="group fixed inset-y-0 left-0 z-50 flex w-16 flex-col overflow-hidden border-r border-border bg-surface shadow-lg transition-[width,box-shadow] duration-200 ease-out hover:w-60 hover:shadow-2xl focus-within:w-60 focus-within:shadow-2xl sm:w-[72px]"
      aria-label="Primary navigation"
    >
      <a
        className="flex h-[72px] min-w-0 items-center gap-3 px-3 text-muted no-underline sm:px-4"
        href="/"
        aria-label="Cafe Story home"
      >
        <BrandIcon className="h-10 w-10 shadow-sm" />
        <span className="translate-x-[-4px] whitespace-nowrap text-lg font-black text-primary-strong opacity-0 transition duration-150 group-hover:translate-x-0 group-hover:opacity-100 group-focus-within:translate-x-0 group-focus-within:opacity-100">
          Cafe Story
        </span>
      </a>

      <nav className="grid gap-1.5 px-2 py-2 sm:px-3">
        {sidebarItems.map((item) => {
          const isActive = isActivePath(pathname, item.href);

          return (
            <a
              aria-current={isActive ? "page" : undefined}
              aria-label={item.label}
              className={`flex h-12 min-w-0 items-center gap-3 rounded-md px-3 text-sm font-bold no-underline transition ${
                item.variant === "primary"
                  ? "mt-1 bg-primary text-white hover:bg-primary-strong"
                  : isActive
                    ? "bg-surface-muted text-primary-strong"
                    : "text-muted hover:bg-surface-muted hover:text-primary-strong"
              }`}
              href={item.href}
              key={item.href}
              title={item.label}
            >
              <span className="grid h-6 w-6 shrink-0 place-items-center">
                <SidebarIcon name={item.icon} />
              </span>
              <span className="translate-x-[-4px] whitespace-nowrap opacity-0 transition duration-150 group-hover:translate-x-0 group-hover:opacity-100 group-focus-within:translate-x-0 group-focus-within:opacity-100">
                {item.label}
              </span>
            </a>
          );
        })}
      </nav>
    </aside>
  );
}
