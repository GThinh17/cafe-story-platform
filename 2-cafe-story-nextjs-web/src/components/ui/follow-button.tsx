"use client";

import { useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { followUser, unfollowUser } from "@/lib/api/users";
import { followCafePage, unfollowCafePage } from "@/lib/api/cafes";
import { ApiError } from "@/lib/api/client";
import { followRegistry } from "@/lib/follow-registry";
import { cn } from "@/lib/utils";

type FollowButtonProps = {
  targetId: string;
  targetType: "user" | "cafe";
  isFollowing?: boolean;
  className?: string;
  onToggle?: (nextIsFollowing: boolean) => void;
};

export function FollowButton({
  targetId,
  targetType,
  isFollowing: initialFollowing = false,
  className,
  onToggle,
}: FollowButtonProps) {
  // Registry là nguồn ưu tiên — nếu chưa có thì dùng prop
  const registryState = targetId ? followRegistry.get(targetType, targetId) : null;
  const resolved = registryState !== null ? registryState : Boolean(initialFollowing);

  const [following, setFollowing] = useState(resolved);
  const [loading, setLoading] = useState(false);

  const followingRef = useRef(resolved);
  followingRef.current = following;

  // Subscribe để đồng bộ khi instance khác follow/unfollow cùng target
  useEffect(() => {
    if (!targetId) return;
    return followRegistry.subscribe(targetType, targetId, (val) => {
      setFollowing(val);
      followingRef.current = val;
    });
  }, [targetType, targetId]);

  // Sync khi targetId hoặc initialFollowing thay đổi (async data load, profile page)
  // Registry là nguồn ưu tiên nếu đã có (user đã tương tác từ trang khác)
  // Nếu registry null → seed từ prop và cập nhật local state
  useEffect(() => {
    if (!targetId) return;
    const registryVal = followRegistry.get(targetType, targetId);
    if (registryVal !== null) {
      setFollowing(registryVal);
      followingRef.current = registryVal;
    } else {
      const val = Boolean(initialFollowing);
      followRegistry.set(targetType, targetId, val);
      setFollowing(val);
      followingRef.current = val;
    }
  }, [initialFollowing, targetType, targetId]);

  async function handleClick(e: React.MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    if (loading || !targetId) return;

    const prev = followingRef.current;
    const next = !prev;

    followingRef.current = next;
    setFollowing(next);
    setLoading(true);
    followRegistry.set(targetType, targetId, next);
    onToggle?.(next);

    try {
      if (targetType === "user") {
        if (prev) await unfollowUser(targetId);
        else await followUser(targetId);
      } else {
        if (prev) await unfollowCafePage(targetId);
        else await followCafePage(targetId);
      }
    } catch (err) {
      // 409 khi follow = server xác nhận user đang follow (state FE bị sai do BE không trả isFollowing)
      // → tự sửa về true thay vì rollback về false
      const isConflictOnFollow = !prev && err instanceof ApiError && err.statusCode === 409;
      const corrected = isConflictOnFollow ? true : prev;
      followingRef.current = corrected;
      setFollowing(corrected);
      followRegistry.set(targetType, targetId, corrected);
      onToggle?.(corrected);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Button
      className={cn("h-8 px-5 text-sm font-black", className)}
      disabled={loading}
      onClick={handleClick}
      type="button"
      variant={following ? "secondary" : "default"}
    >
      {following ? "Following" : "Follow"}
    </Button>
  );
}
