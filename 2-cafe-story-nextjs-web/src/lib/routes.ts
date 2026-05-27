export const ACCESS_TOKEN_COOKIE = "access_token";

export const authRoutes = ["/login", "/register"] as const;

export const protectedRoutePrefixes = [
  "/blogs",
  "/cafes",
  "/explore",
  "/messages",
  "/notifications",
  "/profile",
  "/reviewers",
  "/reviews",
] as const;

export function isAuthRoute(pathname: string) {
  return authRoutes.some((route) => pathname === route);
}

export function isProtectedRoute(pathname: string) {
  return protectedRoutePrefixes.some(
    (route) => pathname === route || pathname.startsWith(`${route}/`),
  );
}
