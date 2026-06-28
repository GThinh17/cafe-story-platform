"use client";

import { useRef, useState, useEffect } from "react";
import { ChevronLeftIcon, ChevronRightIcon, UserIcon } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import type { NotificationResponse, NotificationType } from "@/types/notification";
import type { UserResponse } from "@/types/user";

const NOTIFICATION_VERBS: Record<NotificationType, string> = {
  LIKE: "liked your post.",
  SHARE: "shared your post.",
  COMMENT: "commented on your post.",
  MESSAGE: "sent you a message.",
  FOLLOW: "started following you.",
  TAG: "tagged you in a post.",
};

const TYPE_LABELS: Record<NotificationType, string> = {
  LIKE: "Likes",
  SHARE: "Shares",
  COMMENT: "Comments",
  MESSAGE: "Messages",
  FOLLOW: "Follows",
  TAG: "Tags",
};

const TYPE_ORDER: NotificationType[] = [
  "FOLLOW",
  "COMMENT",
  "LIKE",
  "SHARE",
  "MESSAGE",
  "TAG",
];

function getActorDisplayName(actor: UserResponse | undefined): string {
  return (
    actor?.userName?.trim() ||
    actor?.userFullName?.trim() ||
    "Someone"
  );
}

function getActorAvatar(actor: UserResponse | undefined): string | undefined {
  return (
    actor?.userAvatar ||
    actor?.avatar ||
    actor?.profileImage ||
    actor?.imageUrl ||
    undefined
  );
}

function formatRelativeTime(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const minutes = Math.floor(diff / 60_000);
  if (minutes < 1) return "just now";
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days}d`;
  return `${Math.floor(days / 30)}mo`;
}

function isThisMonth(dateStr: string): boolean {
  const now = new Date();
  const d = new Date(dateStr);
  return (
    d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth()
  );
}

function NotificationRow({
  item,
  actor,
  onClick,
}: {
  item: NotificationResponse;
  actor: UserResponse | undefined;
  onClick: (notification: NotificationResponse) => void;
}) {
  const displayName = getActorDisplayName(actor);
  const avatarUrl = getActorAvatar(actor);
  const initial = displayName.slice(0, 1).toUpperCase();

  return (
    <article
      className={`flex min-w-0 cursor-pointer items-center gap-4 rounded-lg px-2 py-4 transition-colors ${!item.isRead ? "bg-surface-muted/60" : ""}`}
      onClick={() => onClick(item)}
    >
      <Avatar className="size-12 shrink-0 border-2 border-surface">
        {avatarUrl ? <AvatarImage alt={displayName} src={avatarUrl} /> : null}
        <AvatarFallback>
          {actor ? initial : <UserIcon className="size-5 text-muted" />}
        </AvatarFallback>
      </Avatar>

      <p className="min-w-0 flex-1 text-base leading-6 text-foreground">
        <span className="font-black">{displayName}</span>{" "}
        {NOTIFICATION_VERBS[item.type]}{" "}
        <span className="text-muted">{formatRelativeTime(item.createdAt)}</span>
      </p>

      {!item.isRead && (
        <span className="size-2.5 shrink-0 rounded-full bg-primary" />
      )}
    </article>
  );
}

function NotificationSkeleton() {
  return (
    <div className="flex items-center gap-4 py-4">
      <Skeleton className="size-12 shrink-0 rounded-full" />
      <div className="flex-1 space-y-2">
        <Skeleton className="h-4 w-3/4 rounded" />
        <Skeleton className="h-3 w-1/3 rounded" />
      </div>
    </div>
  );
}

type ActivityListProps = {
  notifications: NotificationResponse[];
  unreadCount: number;
  isLoading: boolean;
  error: string | null;
  activeFilter: string;
  actors: Record<string, UserResponse>;
  onFilterChange: (filter: string) => void;
  onMarkAllRead: () => void;
  onItemClick: (notification: NotificationResponse) => void;
  onClose?: () => void;
};

export function ActivityList({
  notifications,
  unreadCount,
  isLoading,
  error,
  activeFilter,
  actors,
  onFilterChange,
  onMarkAllRead,
  onItemClick,
  onClose,
}: ActivityListProps) {
  const tabsViewportRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  function updateTabScrollState() {
    const viewport = tabsViewportRef.current;
    if (!viewport) return;
    setCanScrollLeft(viewport.scrollLeft > 0);
    setCanScrollRight(
      viewport.scrollLeft + viewport.clientWidth < viewport.scrollWidth - 1,
    );
  }

  function scrollTabs(direction: "left" | "right") {
    tabsViewportRef.current?.scrollBy({
      left: direction === "right" ? 200 : -200,
      behavior: "smooth",
    });
  }

  useEffect(() => {
    updateTabScrollState();
    window.addEventListener("resize", updateTabScrollState);
    return () => window.removeEventListener("resize", updateTabScrollState);
  }, []);

  const filtered =
    activeFilter === "ALL"
      ? notifications
      : notifications.filter((n) => n.type === activeFilter);

  const thisMonthItems = filtered.filter((n) => isThisMonth(n.createdAt));
  const earlierItems = filtered.filter((n) => !isThisMonth(n.createdAt));

  return (
    <main className="h-screen w-full overflow-hidden border-x border-border bg-background shadow-xl">
      <section className="flex h-full flex-col">
        <header className="shrink-0 px-4 pb-4 pt-10 sm:px-6">
          <div className="flex items-start justify-between gap-6">
            <h1 className="text-3xl font-black text-foreground">Notifications</h1>
            <div className="flex items-center gap-2">
              {unreadCount > 0 && (
                <Button
                  className="text-xs text-muted"
                  onClick={onMarkAllRead}
                  size="sm"
                  type="button"
                  variant="ghost"
                >
                  Mark all as read
                </Button>
              )}
              {onClose && (
                <Button
                  aria-label="Close notifications"
                  onClick={onClose}
                  size="icon-sm"
                  type="button"
                  variant="ghost"
                >
                  ×
                </Button>
              )}
            </div>
          </div>

          <Tabs className="mt-7" value={activeFilter} onValueChange={onFilterChange}>
            <div className="relative">
              {canScrollLeft ? (
                <Button
                  aria-label="Scroll notification filters left"
                  className="absolute left-0 top-1/2 z-10 size-8 -translate-y-1/2 rounded-full bg-surface shadow- ring-5 ring-background/75"
                  onClick={() => scrollTabs("left")}
                  size="icon-sm"
                  type="button"
                  variant="outline"
                >
                  <ChevronLeftIcon />
                </Button>
              ) : null}

              <div
                className="overflow-hidden"
                onScroll={updateTabScrollState}
                ref={tabsViewportRef}
              >
                <TabsList className="flex h-auto w-max flex-nowrap justify-start gap-3 rounded-none bg-transparent p-0 pr-12">
                  <TabsTrigger
                    className="shrink-0 rounded-full border border-border bg-surface px-5 py-2.5 text-sm font-black shadow-sm data-[state=active]:border-transparent data-[state=active]:bg-surface-muted"
                    value="ALL"
                  >
                    All
                  </TabsTrigger>
                  {TYPE_ORDER.map((type) => (
                    <TabsTrigger
                      className="shrink-0 rounded-full border border-border bg-surface px-5 py-2.5 text-sm font-black shadow-sm data-[state=active]:border-transparent data-[state=active]:bg-surface-muted"
                      key={type}
                      value={type}
                    >
                      {TYPE_LABELS[type]}
                    </TabsTrigger>
                  ))}
                </TabsList>
              </div>

              {canScrollRight ? (
                <Button
                  aria-label="Scroll notification filters right"
                  className="absolute right-0 top-1/2 z-10 size-8 -translate-y-1/2 rounded-full bg-surface shadow- ring-5 ring-background/75"
                  onClick={() => scrollTabs("right")}
                  size="icon-sm"
                  type="button"
                  variant="outline"
                >
                  <ChevronRightIcon />
                </Button>
              ) : null}
            </div>
          </Tabs>
        </header>

        <div className="min-h-0 flex-1 overflow-y-auto px-4 [scrollbar-width:none] sm:px-6 [&::-webkit-scrollbar]:hidden">
          {isLoading ? (
            <div className="flex flex-col">
              {Array.from({ length: 4 }).map((_, i) => (
                <NotificationSkeleton key={i} />
              ))}
            </div>
          ) : error ? (
            <div className="flex flex-col items-center gap-3 py-20 text-center">
              <p className="text-base font-medium text-foreground">{error}</p>
            </div>
          ) : thisMonthItems.length === 0 && earlierItems.length === 0 ? (
            <div className="flex flex-col items-center gap-3 py-20 text-center">
              <p className="text-base font-medium text-foreground">
                No notifications yet.
              </p>
              <p className="max-w-[240px] text-sm leading-6 text-muted">
                Follow someone to get updates about their posts and activity.
              </p>
            </div>
          ) : (
            <>
              {thisMonthItems.length > 0 && (
                <section>
                  <h2 className="text-xl font-black text-foreground">This month</h2>
                  <div className="mt-3 flex flex-col">
                    {thisMonthItems.map((item) => (
                      <div key={item.id}>
                        <NotificationRow
                          item={item}
                          actor={actors[item.actorId]}
                          onClick={onItemClick}
                        />
                        <Separator />
                      </div>
                    ))}
                  </div>
                </section>
              )}

              {earlierItems.length > 0 && (
                <section className="mt-7">
                  <h2 className="text-xl font-black text-foreground">Earlier</h2>
                  <div className="mt-3 flex flex-col">
                    {earlierItems.map((item) => (
                      <div key={item.id}>
                        <NotificationRow
                          item={item}
                          actor={actors[item.actorId]}
                          onClick={onItemClick}
                        />
                        <Separator />
                      </div>
                    ))}
                  </div>
                </section>
              )}
            </>
          )}
        </div>
      </section>
    </main>
  );
}
