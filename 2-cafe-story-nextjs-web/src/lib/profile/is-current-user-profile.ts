import type { AuthUser } from "@/types/auth";

export function normalizeUsername(username: string | null | undefined) {
  return username?.trim().toLowerCase() ?? "";
}

export function isCurrentUserProfile(routeUsername: string, user: AuthUser | null) {
  const normalizedRouteUsername = normalizeUsername(routeUsername);

  return Boolean(
    normalizedRouteUsername &&
      [user?.userName, user?.userEmail].some(
        (value) => normalizeUsername(value) === normalizedRouteUsername,
      ),
  );
}
