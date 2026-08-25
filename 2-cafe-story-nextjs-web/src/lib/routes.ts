export const ACCESS_TOKEN_COOKIE = "access_token";

export const authRoutes = ["/login", "/register"] as const;
export const paymentReturnRoutes = [
  "/payment/success",
  "/payments/cancel",
  "/payments/stripe/success",
  "/payments/vnpay/return",
] as const;

export function isAuthRoute(pathname: string) {
  return authRoutes.some((route) => pathname === route);
}

export function isPaymentReturnRoute(pathname: string) {
  return paymentReturnRoutes.some((route) => pathname === route);
}

export function isProtectedRoute(pathname: string) {
  return !isAuthRoute(pathname) && !isPaymentReturnRoute(pathname);
}
