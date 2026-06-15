"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import {
  CreditCardIcon,
  FlagIcon,
  ShieldCheckIcon,
  UsersIcon,
} from "lucide-react";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { AdminStatCard } from "@/components/admin/admin-stat-card";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { getDashboardSummary } from "@/lib/api/admin";
import type { AdminDashboardSummary } from "@/types/admin";

const shortcuts = [
  {
    href: "/moderation",
    icon: ShieldCheckIcon,
    label: "Moderation queue",
    description: "Resolve AI review results.",
  },
  {
    href: "/reports",
    icon: FlagIcon,
    label: "Reports",
    description: "Review user-submitted content reports.",
  },
  {
    href: "/payments",
    icon: CreditCardIcon,
    label: "Payments",
    description: "Audit transfers and refunds.",
  },
  {
    href: "/users",
    icon: UsersIcon,
    label: "Users",
    description: "Manage account status and roles.",
  },
];

export function AdminOverview() {
  const [summary, setSummary] = useState<AdminDashboardSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestIdRef = useRef(0);

  useEffect(() => {
    const controller = new AbortController();
    const requestId = requestIdRef.current + 1;
    requestIdRef.current = requestId;
    setIsLoading(true);
    setError(null);

    getDashboardSummary(controller.signal)
      .then((response) => {
        if (requestIdRef.current === requestId) {
          setSummary(response);
        }
      })
      .catch((requestError) => {
        if (controller.signal.aborted || requestIdRef.current !== requestId) {
          return;
        }

        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to load dashboard summary.",
        );
      })
      .finally(() => {
        if (!controller.signal.aborted && requestIdRef.current === requestId) {
          setIsLoading(false);
        }
      });

    return () => controller.abort();
  }, []);

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Overview"
        description="Operational snapshot for users, content, payments, and moderation."
      />

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {Array.from({ length: 8 }).map((_, index) => (
            <Skeleton className="h-28 w-full" key={index} />
          ))}
        </div>
      ) : error ? (
        <Card className="p-5">
          <p className="text-sm font-bold text-accent">Request failed</p>
          <p className="mt-2 text-sm text-muted">{error}</p>
        </Card>
      ) : summary ? (
        <>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <AdminStatCard
              label="Users"
              value={summary.totalUsers}
              meta={`${summary.activeUsers} active`}
            />
            <AdminStatCard
              label="Reviewers"
              value={summary.totalReviewers}
              meta="Accounts with reviewer role"
            />
            <AdminStatCard
              label="Cafe pages"
              value={summary.totalCafePages}
              meta={`${summary.activeCafePages} active`}
            />
            <AdminStatCard
              label="Blogs"
              value={summary.totalBlogs}
              meta={`${summary.publishedBlogs} published · ${summary.hiddenBlogs} hidden`}
            />
            <AdminStatCard label="Comments" value={summary.totalComments} />
            <AdminStatCard
              label="Payments"
              value={summary.totalPayments}
              meta={`${summary.paidPayments} paid · ${summary.pendingPayments} pending`}
            />
            <AdminStatCard
              label="Failed payments"
              value={summary.failedPayments}
            />
            <AdminStatCard
              label="Pending moderation"
              value={summary.pendingModerationItems}
            />
          </div>

          <div className="grid gap-4 lg:grid-cols-4">
            {shortcuts.map(({ href, icon: Icon, label, description }) => (
              <Card key={href}>
                <CardHeader className="p-4 pb-2">
                  <Icon className="size-5 text-primary" />
                  <CardTitle className="text-base">{label}</CardTitle>
                </CardHeader>
                <CardContent className="flex flex-col gap-4 p-4 pt-0">
                  <p className="text-sm leading-6 text-muted">{description}</p>
                  <Button asChild variant="outline" size="sm">
                    <Link href={href}>Open</Link>
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </>
      ) : null}
    </div>
  );
}
