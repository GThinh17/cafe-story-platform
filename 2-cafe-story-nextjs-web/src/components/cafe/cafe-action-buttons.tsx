"use client";

import { HeartIcon } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { FollowButton } from "@/components/ui/follow-button";
import { CafeRatingControl } from "@/components/cafe/cafe-rating-control";
import { likeCafePage, unlikeCafePage } from "@/lib/api/cafes";
import { cn } from "@/lib/utils";

type CafeActionButtonsProps = {
  cafeId: string;
  isLiked?: boolean;
  isFollowing?: boolean;
  likeCount?: number;
  myRating?: number | null;
  ratingCount?: number | null;
  ratingScore?: number | null;
  onLikeStateChange?: (nextState: {
    isLiked: boolean;
    likeCount: number;
  }) => void;
  onRatingChange?: (next: {
    isRating: boolean;
    myRating: number;
    ratingCount: number | null;
    ratingScore: number | null;
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
  isLiked = false,
  isFollowing = false,
  likeCount = 0,
  myRating = null,
  ratingCount = null,
  ratingScore = null,
  onLikeStateChange,
  onRatingChange,
}: CafeActionButtonsProps) {
  const [isPending, setIsPending] = useState(false);

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
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
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
      <CafeRatingControl
        cafeId={cafeId}
        myRating={myRating}
        onRatingChange={onRatingChange}
        ratingCount={ratingCount}
        ratingScore={ratingScore}
      />
    </div>
  );
}
