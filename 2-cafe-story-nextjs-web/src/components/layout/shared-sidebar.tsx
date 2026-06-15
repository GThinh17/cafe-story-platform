"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  BadgeDollarSignIcon,
  BellIcon,
  CompassIcon,
  HomeIcon,
  MessageCircleIcon,
  PlusIcon,
  UserIcon,
} from "lucide-react";
import { ActivityList } from "@/components/notification/activity-list";
import { PricingPlanModal } from "@/components/layout/pricing-plan-modal";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { CreatePostModal } from "@/components/review/create-post-modal";
import { BrandIcon } from "@/components/ui/brand-icon";
import { Button, buttonVariants } from "@/components/ui/button";
import { useCurrentUser } from "@/hooks/use-current-user";
import {
  Sheet,
  SheetContent,
  SheetTitle,
} from "@/components/ui/sheet";
import { cn } from "@/lib/utils";
import { getCafePagesByOwnerId } from "@/lib/api/cafes";
import { mockReviewComposer, mockReviewDraftHints } from "@/mocks/reviews";
import type { CafePageResponse } from "@/types/cafe";
import { usePathname } from "next/navigation";

type SidebarItem = {
  href: string;
  label: string;
  icon: "home" | "explore" | "bell" | "message" | "profile" | "plus";
};

const sidebarItems: SidebarItem[] = [
  { href: "/", label: "Home", icon: "home" },
  { href: "/explore", label: "Explore", icon: "explore" },
  { href: "/notifications", label: "Notifications", icon: "bell" },
  { href: "/messages", label: "Messages", icon: "message" },
  { href: "/login", label: "Profile", icon: "profile" },
  { href: "/reviews/new", label: "Create Post", icon: "plus" },
];

const sidebarIcons = {
  home: HomeIcon,
  explore: CompassIcon,
  bell: BellIcon,
  message: MessageCircleIcon,
  profile: UserIcon,
  plus: PlusIcon,
};

function isActivePath(pathname: string, href: string) {
  if (href === "/") {
    return pathname === "/";
  }

  return pathname === href || pathname.startsWith(`${href}/`);
}

function isActiveCafePage(cafe: CafePageResponse) {
  return cafe.pageActive === true || cafe.status === "ACTIVE";
}

export function SharedSidebar() {
  const pathname = usePathname();
  const { user, isLoading } = useCurrentUser();
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const [isCreatePostOpen, setIsCreatePostOpen] = useState(false);
  const [isPricingPlanOpen, setIsPricingPlanOpen] = useState(false);
  const [ownedCafePage, setOwnedCafePage] = useState<CafePageResponse | null>(null);
  const profileHref = user?.userName ? `/${user.userName}` : "/login";
  const userId = user?.userId;

  useEffect(() => {
    let isCurrentRequest = true;

    if (!userId) {
      setOwnedCafePage(null);
      return () => {
        isCurrentRequest = false;
      };
    }

    const ownerUserId = userId;

    async function loadOwnedCafePage() {
      try {
        const cafes = await getCafePagesByOwnerId(ownerUserId);

        if (!isCurrentRequest) {
          return;
        }

        setOwnedCafePage(cafes.find(isActiveCafePage) ?? null);
      } catch {
        if (isCurrentRequest) {
          setOwnedCafePage(null);
        }
      }
    }

    void loadOwnedCafePage();

    return () => {
      isCurrentRequest = false;
    };
  }, [userId]);

  return (
    <>
      <aside
        className="group fixed inset-y-0 left-0 z-50 flex w-16 flex-col overflow-hidden border-r border-border bg-surface shadow-lg transition-[width,box-shadow] duration-200 ease-out hover:w-60 hover:shadow-2xl focus-within:w-60 focus-within:shadow-2xl sm:w-[72px]"
        aria-label="Primary navigation"
      >
        <Link
          className="flex h-[72px] min-w-0 items-center gap-3 px-3 text-muted no-underline sm:px-4"
          href="/"
          aria-label="Cafe Story home"
          onClick={() => setIsNotificationsOpen(false)}
        >
          <BrandIcon className="size-10 shadow-sm" />
          <span className="translate-x-[-4px] whitespace-nowrap text-lg font-medium text-primary-strong opacity-0 transition duration-150 group-hover:translate-x-0 group-hover:opacity-100 group-focus-within:translate-x-0 group-focus-within:opacity-100">
            Cafe Story
          </span>
        </Link>

        <nav className="grid gap-1.5 px-2 py-2 sm:px-3">
          {sidebarItems.map((item) => {
            const Icon = sidebarIcons[item.icon];
            const isNotificationItem = item.icon === "bell";
            const isCreatePostItem = item.icon === "plus";
            const isProfileItem = item.icon === "profile";
            const itemHref =
              isProfileItem && !isLoading ? profileHref : item.href;
            const isActive = isNotificationItem
              ? isNotificationsOpen || isActivePath(pathname, itemHref)
              : isCreatePostItem
                ? isCreatePostOpen || isActivePath(pathname, itemHref)
              : isActivePath(pathname, itemHref);
            const itemClassName = cn(
              buttonVariants({ variant: "ghost" }),
              "h-12 w-full min-w-0 justify-start gap-3 px-3 text-sm no-underline",
              isActive
                ? `${isNotificationItem ? "font-medium" : "font-bold"} text-primary-strong hover:text-primary-strong`
                : "font-medium text-muted hover:text-muted",
            );

            const itemContent = (
              <>
                <span className="grid size-6 shrink-0 place-items-center">
                  <Icon aria-hidden="true" />
                </span>
                <span className="translate-x-[-4px] whitespace-nowrap opacity-0 transition duration-150 group-hover:translate-x-0 group-hover:opacity-100 group-focus-within:translate-x-0 group-focus-within:opacity-100">
                  {item.label}
                </span>
              </>
            );

            if (isProfileItem && isLoading) {
              return (
                <Button
                  aria-disabled="true"
                  aria-label={item.label}
                  className={itemClassName}
                  disabled
                  key={item.href}
                  type="button"
                  variant="ghost"
                >
                  {itemContent}
                </Button>
              );
            }

            if (isNotificationItem) {
              return (
                <Button
                  aria-expanded={isNotificationsOpen}
                  aria-label={item.label}
                  className={itemClassName}
                  key={item.href}
                  onClick={() => setIsNotificationsOpen((isOpen) => !isOpen)}
                  type="button"
                  variant="ghost"
                >
                  {itemContent}
                </Button>
              );
            }

            if (isCreatePostItem) {
              return (
                <Button
                  aria-expanded={isCreatePostOpen}
                  aria-label={item.label}
                  className={itemClassName}
                  key={item.href}
                  onClick={() => {
                    setIsNotificationsOpen(false);
                    setIsCreatePostOpen(true);
                  }}
                  type="button"
                  variant="ghost"
                >
                  {itemContent}
                </Button>
              );
            }

            return (
              <Link
                aria-current={isActive ? "page" : undefined}
                aria-label={item.label}
                className={itemClassName}
                href={itemHref}
                key={item.href}
                onClick={() => setIsNotificationsOpen(false)}
              >
                {itemContent}
              </Link>
            );
          })}

          <Button
            aria-expanded={isPricingPlanOpen}
            aria-label="Pricing plan"
            className={cn(
              buttonVariants({ variant: "ghost" }),
              "h-12 w-full min-w-0 justify-start gap-3 px-3 text-sm no-underline",
              isPricingPlanOpen
                ? "font-bold text-primary-strong hover:text-primary-strong"
                : "font-medium text-muted hover:text-muted",
            )}
            onClick={() => {
              setIsNotificationsOpen(false);
              setIsCreatePostOpen(false);
              setIsPricingPlanOpen(true);
            }}
            type="button"
            variant="ghost"
          >
            <span className="grid size-6 shrink-0 place-items-center">
              <BadgeDollarSignIcon aria-hidden="true" />
            </span>
            <span className="translate-x-[-4px] whitespace-nowrap opacity-0 transition duration-150 group-hover:translate-x-0 group-hover:opacity-100 group-focus-within:translate-x-0 group-focus-within:opacity-100">
              Pricing plan
            </span>
          </Button>
        </nav>

        <div className="mt-auto px-2 pb-4 sm:px-3">
          <div className="flex h-12 min-w-0 items-center gap-3 px-3">
            <ThemeToggle />
            <span className="translate-x-[-4px] whitespace-nowrap text-sm font-medium text-muted opacity-0 transition duration-150 group-hover:translate-x-0 group-hover:opacity-100 group-focus-within:translate-x-0 group-focus-within:opacity-100">
              Theme
            </span>
          </div>
        </div>
      </aside>

      <Sheet open={isNotificationsOpen} onOpenChange={setIsNotificationsOpen}>
        <SheetContent
          className="!w-[min(500px,100vw)] !max-w-[500px] border-border bg-background p-0"
          side="left"
          showCloseButton={false}
        >
          <SheetTitle className="sr-only">Notifications</SheetTitle>
          <ActivityList
            items={[]}
            onClose={() => setIsNotificationsOpen(false)}
          />
        </SheetContent>
      </Sheet>

      <CreatePostModal
        composer={mockReviewComposer}
        hints={mockReviewDraftHints}
        isOpen={isCreatePostOpen}
        ownedCafePage={ownedCafePage}
        onClose={() => setIsCreatePostOpen(false)}
      />

      <PricingPlanModal
        isOpen={isPricingPlanOpen}
        onOpenChange={setIsPricingPlanOpen}
      />
    </>
  );
}
