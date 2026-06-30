"use client";

import { LoaderCircleIcon } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { cn } from "@/lib/utils";
import type { MentionItem } from "@/hooks/use-followings";

type MentionPickerProps = {
  isOpen: boolean;
  query: string;
  items: MentionItem[];
  isLoading: boolean;
  activeIndex: number;
  onSelect: (item: MentionItem) => void;
  onHoverIndex?: (index: number) => void;
};

function getInitials(name: string) {
  const words = name.trim().split(/\s+/).filter(Boolean);
  if (words.length === 0) return "?";
  return words
    .slice(0, 2)
    .map((w) => w[0])
    .join("")
    .toUpperCase();
}

export function MentionPicker({
  isOpen,
  query,
  items,
  isLoading,
  activeIndex,
  onSelect,
  onHoverIndex,
}: MentionPickerProps) {
  if (!isOpen) return null;

  const safeActive = Math.min(activeIndex, Math.max(items.length - 1, 0));

  return (
    <div
      aria-label="Tag a user or cafe page"
      className="absolute left-0 top-full z-[80] mt-1 w-80 max-w-[calc(100%-8px)] overflow-hidden rounded-md border border-line-soft bg-surface shadow-[0_18px_40px_rgba(39,19,16,0.18)]"
      role="listbox"
    >
      <div className="flex items-center gap-2 border-b border-line-soft px-3 py-2 text-xs text-muted">
        <span className="font-medium text-espresso">Tag</span>
        <span className="truncate">
          {query.length > 0 ? `@${query}` : "Type to search…"}
        </span>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center gap-2 px-3 py-6 text-xs text-muted">
          <LoaderCircleIcon className="size-3.5 animate-spin" aria-hidden="true" />
          Loading followings…
        </div>
      ) : items.length === 0 ? (
        <div className="px-3 py-6 text-center text-xs text-muted">
          No matches in your following list.
        </div>
      ) : (
        <ul className="max-h-64 overflow-y-auto py-1">
          {items.map((item, idx) => {
            const isActive = idx === safeActive;
            return (
              <li key={`${item.kind}-${item.id}`}>
                <button
                  aria-selected={isActive}
                  className={cn(
                    "flex w-full items-center gap-3 px-3 py-2 text-left transition-colors hover:bg-surface-muted",
                    isActive && "bg-surface-muted",
                  )}
                  onClick={() => onSelect(item)}
                  onMouseEnter={() => onHoverIndex?.(idx)}
                  role="option"
                  type="button"
                >
                  <Avatar size="sm">
                    {item.avatarUrl ? (
                      <AvatarImage
                        alt={item.displayName}
                        src={item.avatarUrl}
                      />
                    ) : null}
                    <AvatarFallback>
                      {getInitials(item.displayName)}
                    </AvatarFallback>
                  </Avatar>

                  <span className="flex flex-1 flex-col overflow-hidden">
                    <span className="truncate text-sm font-medium text-espresso">
                      {item.displayName}
                    </span>
                    <span className="truncate text-xs text-muted">
                      @{item.slug}
                    </span>
                  </span>

                  <span
                    className={cn(
                      "shrink-0 rounded-md px-2 py-0.5 text-[10px] font-medium",
                      item.kind === "user"
                        ? "bg-surface-muted text-primary-strong"
                        : "bg-primary/10 text-primary",
                    )}
                  >
                    {item.kind === "user" ? "User" : "Page"}
                  </span>
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
