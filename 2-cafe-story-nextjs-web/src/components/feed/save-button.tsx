"use client";

import { BookmarkIcon } from "lucide-react";
import { useI18n } from "@/components/providers/locale-provider";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type SaveButtonProps = {
  isSaved?: boolean;
  isBusy?: boolean;
  onToggle: () => void;
  size?: "default" | "lg";
  className?: string;
};

export function SaveButton({
  isSaved,
  isBusy,
  onToggle,
  size = "default",
  className,
}: SaveButtonProps) {
  const { t } = useI18n();

  return (
    <Button
      aria-label={t(isSaved ? "post.action.unsave" : "post.action.save")}
      aria-pressed={isSaved}
      className={cn(
        "h-auto cursor-pointer px-0 py-0 hover:bg-transparent hover:text-primary",
        isSaved && "text-primary",
        className,
      )}
      disabled={isBusy}
      onClick={onToggle}
      type="button"
      variant="ghost"
    >
      <BookmarkIcon
        className={cn(
          size === "lg" ? "size-7" : "size-6",
          isSaved && "fill-current",
        )}
        strokeWidth={size === "lg" ? 2.4 : 2.2}
      />
    </Button>
  );
}
