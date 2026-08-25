import { NextResponse, type NextRequest } from "next/server";
import { ACCESS_TOKEN_COOKIE, isProtectedRoute } from "@/lib/routes";

export function proxy(request: NextRequest) {
  const { nextUrl } = request;
  const pathname = nextUrl.pathname;
  const hasAccessToken = Boolean(request.cookies.get(ACCESS_TOKEN_COOKIE)?.value);

  if (isProtectedRoute(pathname) && !hasAccessToken) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("reason", "auth_required");
    loginUrl.searchParams.set("next", `${pathname}${nextUrl.search}`);

    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/((?!api|_next/static|_next/image|favicon.ico|icon.svg|.*\\..*).*)",
  ],
};
