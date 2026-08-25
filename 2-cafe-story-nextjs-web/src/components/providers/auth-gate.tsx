"use client";

import type { ReactNode } from "react";
import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/components/providers/auth-provider";
import { isAuthRoute, isProtectedRoute } from "@/lib/routes";

type AuthGateProps = {
  children: ReactNode;
};

function getSafeNextPath(pathname: string) {
  const query = window.location.search;
  const nextPath = query ? `${pathname}${query}` : pathname;

  if (!nextPath.startsWith("/") || nextPath.startsWith("//")) {
    return "/";
  }

  return nextPath;
}

export function AuthGate({ children }: AuthGateProps) {
  const {
    hasResolvedInitialAuth,
    isAuthenticated,
    isInitialLoading,
  } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const isAuthPage = isAuthRoute(pathname);
  const isProtectedPage = isProtectedRoute(pathname);
  const shouldWaitForInitialAuth =
    isProtectedPage && !hasResolvedInitialAuth && isInitialLoading && !isAuthenticated;

  useEffect(() => {
    if (shouldWaitForInitialAuth) {
      return;
    }

    if (!isAuthenticated && isProtectedPage) {
      const params = new URLSearchParams();
      const nextPath = getSafeNextPath(pathname);

      params.set("reason", "auth_required");
      params.set("next", nextPath);

      const loginHref = `/login?${params.toString()}`;

      router.replace(loginHref);
      return;
    }

    if (isAuthenticated && isAuthPage) {
      router.replace("/");
    }
  }, [
    isAuthPage,
    isAuthenticated,
    isProtectedPage,
    pathname,
    router,
    shouldWaitForInitialAuth,
  ]);

  if (shouldWaitForInitialAuth) {
    return isAuthPage ? children : null;
  }

  if (!isAuthenticated && isProtectedPage) {
    return null;
  }

  if (isAuthenticated && isAuthPage) {
    return null;
  }

  return children;
}
