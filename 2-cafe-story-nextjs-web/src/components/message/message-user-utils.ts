import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import type { UserResponse } from "@/types/user";

function firstNonEmpty(values: Array<string | null | undefined>) {
  return values
    .map((value) => value?.trim())
    .find(
      (value) =>
        value &&
        value.toLowerCase() !== "null" &&
        value.toLowerCase() !== "undefined",
    );
}

export function getMessageUserDisplayName(user: UserResponse) {
  return user.userFullName?.trim() || user.userName || "Cafe Story user";
}

export function getMessageUserAvatarImage(user: UserResponse) {
  return firstNonEmpty([
    user.userAvatar,
    user.avatar,
    user.profileImage,
    user.imageUrl,
  ]) ?? DEFAULT_AVATAR_IMAGE;
}

export function getMessageUserInitials(source: string) {
  const words = source.split(/\s+/).filter(Boolean);

  return words.length > 0
    ? words
        .slice(0, 2)
        .map((word) => word[0])
        .join("")
        .toUpperCase()
    : "CS";
}
