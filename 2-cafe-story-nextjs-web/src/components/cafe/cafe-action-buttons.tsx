"use client";

import { BookOpenIcon, HeartIcon } from "lucide-react";
import { useState } from "react";
import { useI18n } from "@/components/providers/locale-provider";
import { Button } from "@/components/ui/button";
import { FollowButton } from "@/components/ui/follow-button";
import { CafeMenuModal } from "@/components/cafe/cafe-menu-modal";
import { useCafeMenuModal } from "@/hooks/use-cafe-menu-modal";
import { likeCafePage, unlikeCafePage } from "@/lib/api/cafes";
import { cn } from "@/lib/utils";
import type { CafeMenu } from "@/types/cafe";

type CafeActionButtonsProps = {
  cafeId: string;
  cafeName: string;
  isLiked?: boolean;
  isFollowing?: boolean;
  likeCount?: number;
  menu: CafeMenu;
  onLikeStateChange?: (nextState: {
    isLiked: boolean;
    likeCount: number;
  }) => void;
};

function formatCount(value: number) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}

export function CafeActionButtons({
  cafeId,
  cafeName,
  isLiked = false,
  isFollowing = false,
  likeCount = 0,
  menu,
  onLikeStateChange,
}: CafeActionButtonsProps) {
  const { t } = useI18n();
  const [isPending, setIsPending] = useState(false);
  const { isOpen, openMenu, setIsOpen } = useCafeMenuModal();

  async function handleLikeClick() {
    if (isPending) {
      return;
    }

    const wasLiked = isLiked;
    const previousLikeCount = likeCount;
    const nextLikeCount = Math.max(
      0,
      previousLikeCount + (wasLiked ? -1 : 1),
    );

    onLikeStateChange?.({
      isLiked: !wasLiked,
      likeCount: nextLikeCount,
    });
    setIsPending(true);

    try {
      if (wasLiked) {
        await unlikeCafePage(cafeId);
      } else {
        await likeCafePage(cafeId);
      }
    } catch {
      onLikeStateChange?.({
        isLiked: wasLiked,
        likeCount: previousLikeCount,
      });
    } finally {
      setIsPending(false);
    }
  }

  return (
    <>
      <div className="flex flex-col gap-3 sm:flex-row">
        <Button
          className={cn(
            "flex h-12 cursor-pointer items-center justify-center gap-2 rounded-sm border border-primary/40 bg-primary text-sm font-black text-white transition hover:bg-primary/90 hover:text-white",
            isLiked && "bg-secondary hover:bg-secondary/90 hover:text-primary/90 text-primary border-secondary",
          )}
          onClick={() => void handleLikeClick()}
          type="button"
        >
          <HeartIcon
            className={cn("size-5", isLiked && "fill-primary text-primary")}
            data-icon="inline-start"
          />
           {formatCount(likeCount)}
        </Button>
        <FollowButton
          className="h-12 rounded-sm px-8"
          isFollowing={isFollowing}
          targetId={cafeId}
          targetType="cafe"
        />
        <Button
          className="flex h-12 cursor-pointer items-center justify-center gap-2 rounded-sm px-8 text-sm font-black transition hover:text-espresso/80"
          onClick={openMenu}
          type="button"
          variant="secondary"
        >
          <BookOpenIcon className="size-5" data-icon="inline-start" />
          {t("cafe.action.viewMenu")}
        </Button>
      </div>

      <CafeMenuModal
        cafeName={cafeName}
        menu={menu}
        onOpenChange={setIsOpen}
        open={isOpen}
      />
    </>
  );
}
