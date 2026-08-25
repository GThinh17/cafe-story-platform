import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";

/**
 * The only fields these helpers read. Widened from `UserResponse` so a
 * `ChatMemberResponse` — which the conversation list already embeds — can be
 * rendered without fetching the full user.
 */
export type MessageUserLike = {
  userId: string;
  userName: string | null;
  userFullName: string | null;
  userAvatar: string | null;
  avatar?: string | null;
  profileImage?: string | null;
  imageUrl?: string | null;
};

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

export function getMessageUserDisplayName(
  user: MessageUserLike,
  fallbackName: string,
) {
  return user.userFullName?.trim() || user.userName || fallbackName;
}

export function getMessageUserAvatarImage(user: MessageUserLike) {
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
