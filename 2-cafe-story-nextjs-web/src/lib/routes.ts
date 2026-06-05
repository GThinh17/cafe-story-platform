export const ACCESS_TOKEN_COOKIE = "access_token";

export const authRoutes = ["/login", "/register"] as const;

export function isAuthRoute(pathname: string) {
  return authRoutes.some((route) => pathname === route);
}

export function isProtectedRoute(pathname: string) {
  return !isAuthRoute(pathname);
}
