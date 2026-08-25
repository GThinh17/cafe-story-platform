import { imageWidths, optimizeImageUrl } from "@/lib/image-optimizer";
import type { AuthUser } from "@/types/auth";

export const DEFAULT_AVATAR_IMAGE = "/images/default-avatar.svg";

export function getUserDisplayName(user: AuthUser | null) {
  return user?.userFullName?.trim() || user?.userName || "Cafe Story user";
}

export function getUserHandle(user: AuthUser | null) {
  return user?.userName || user?.userEmail || "cafestory_user";
}

export function getUserEmail(user: AuthUser | null) {
  return user?.userEmail || "";
}

export function getUserAvatarImage(user: AuthUser | null) {
  return (
    optimizeImageUrl(user?.userAvatar, { width: imageWidths.avatar }) ||
    DEFAULT_AVATAR_IMAGE
  );
}

export function getUserInitials(user: AuthUser | null) {
  const source = getUserDisplayName(user);
  const words = source.split(/\s+/).filter(Boolean);

  if (words.length === 0) {
    return "CS";
  }

  return words
    .slice(0, 2)
    .map((word) => word[0])
    .join("")
    .toUpperCase();
}
