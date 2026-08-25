import type { AuthUser } from "@/types/auth";

export function hasAdminRole(user: AuthUser | null | undefined) {
  return Boolean(
    user?.roles?.some((role) => role.toUpperCase() === "ADMIN"),
  );
}
