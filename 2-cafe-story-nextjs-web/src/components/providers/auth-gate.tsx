"use client";

import type { ReactNode } from "react";
import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/components/providers/auth-provider";
import { isAuthRoute } from "@/lib/routes";

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
  const { isAuthenticated, isLoading } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const isAuthPage = isAuthRoute(pathname);

  useEffect(() => {
    if (isLoading) {
      return;
    }

    if (!isAuthenticated && !isAuthPage) {
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
  }, [isAuthPage, isAuthenticated, isLoading, pathname, router]);

  if (isLoading) {
    return isAuthPage ? children : null;
  }

  if (!isAuthenticated && !isAuthPage) {
    return null;
  }

  if (isAuthenticated && isAuthPage) {
    return null;
  }

  return children;
}
