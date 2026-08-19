"use client";

import { type ReactNode, useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { AdminSidebar } from "@/components/admin/admin-sidebar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useCurrentUser } from "@/hooks/use-current-user";
import { hasAdminRole } from "@/lib/auth";
import { useTranslate } from "@/features/i18n";

function AdminLoadingState() {
  return (
    <div className="min-h-screen bg-background p-6 text-foreground">
      <div className="mx-auto flex max-w-5xl flex-col gap-4">
        <Skeleton className="h-12 w-64" />
        <Skeleton className="h-72 w-full" />
      </div>
    </div>
  );
}

function AdminAuthRedirect() {
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    const loginHref = `/login?next=${encodeURIComponent(pathname || "/")}`;
    router.replace(loginHref);
  }, [pathname, router]);

  return <AdminLoadingState />;
}

function AdminAccessDenied({ error }: { error: string | null }) {
  const t = useTranslate();
  return (
    <div className="grid min-h-screen place-items-center bg-background p-6 text-foreground">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>{t("auth.accessDenied.title")}</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3 text-sm leading-6 text-muted">
          <p>{t("auth.accessDenied.description")}</p>
          {error ? <p>{error}</p> : null}
        </CardContent>
      </Card>
    </div>
  );
}

function AdminAuthGuard({ children }: { children: ReactNode }) {
  const { user, isInitialLoading, isLoading, hasResolvedInitialAuth, error } =
    useCurrentUser();

  if (isInitialLoading || (isLoading && !hasResolvedInitialAuth)) {
    return <AdminLoadingState />;
  }

  if (!hasResolvedInitialAuth) {
    return <AdminLoadingState />;
  }

  if (!user) {
    return <AdminAuthRedirect />;
  }

  if (!hasAdminRole(user)) {
    return <AdminAccessDenied error={error} />;
  }

  return children;
}

function AdminDashboardContent({ children }: { children: ReactNode }) {
  const { user } = useCurrentUser();

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AdminSidebar user={user} />
      <div className="min-h-screen lg:pl-60">
        <main className="px-4 py-5 sm:px-5 lg:px-6">{children}</main>
      </div>
    </div>
  );
}

export function AdminDashboardShell({ children }: { children: ReactNode }) {
  return (
    <AdminAuthGuard>
      <AdminDashboardContent>{children}</AdminDashboardContent>
    </AdminAuthGuard>
  );
}
