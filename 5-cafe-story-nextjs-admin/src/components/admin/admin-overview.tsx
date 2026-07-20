"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import {
  CoffeeIcon,
  CreditCardIcon,
  FileTextIcon,
  MessageSquareIcon,
  ShieldCheckIcon,
  TrophyIcon,
  UsersIcon,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { AdminStatCard } from "@/components/admin/admin-stat-card";
import {
  AdminRevenueChart,
  RevenueLegend,
  formatVnd,
} from "@/components/admin/admin-revenue-chart";
import { AdminStatusDonut } from "@/components/admin/admin-status-donut";
import { VietnamMap } from "@/components/admin/vietnam-map";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
  getDashboardSummary,
  getRegionAnalytics,
  getRevenueAnalytics,
} from "@/lib/api/admin";
import type {
  AdminDashboardSummary,
  AdminRegionAnalytics,
  AdminRevenueAnalytics,
} from "@/types/admin";

function SectionCard({
  title,
  href,
  meta,
  children,
  className,
}: {
  title: string;
  href: string;
  meta?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <Card
      asChild
      className={`group/section transition hover:border-primary/40 hover:shadow-sm ${className ?? ""}`}
    >
      <Link href={href} className="flex flex-col no-underline">
        <CardHeader className="flex flex-row items-center justify-between gap-3 p-4 pb-2">
          <CardTitle className="text-sm font-semibold text-espresso">
            {title}
          </CardTitle>
          {meta}
        </CardHeader>
        <div className="min-h-0 flex-1 p-4 pt-0">{children}</div>
      </Link>
    </Card>
  );
}

export function AdminOverview() {
  const router = useRouter();
  const [summary, setSummary] = useState<AdminDashboardSummary | null>(null);
  const [revenue, setRevenue] = useState<AdminRevenueAnalytics | null>(null);
  const [regions, setRegions] = useState<AdminRegionAnalytics[] | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestIdRef = useRef(0);

  useEffect(() => {
    const controller = new AbortController();
    const requestId = requestIdRef.current + 1;
    requestIdRef.current = requestId;
    setIsLoading(true);
    setError(null);

    Promise.all([
      getDashboardSummary(controller.signal),
      getRevenueAnalytics(30, controller.signal),
      getRegionAnalytics(controller.signal),
    ])
      .then(([summaryResponse, revenueResponse, regionsResponse]) => {
        if (requestIdRef.current === requestId) {
          setSummary(summaryResponse);
          setRevenue(revenueResponse);
          setRegions(regionsResponse);
        }
      })
      .catch((requestError) => {
        if (controller.signal.aborted || requestIdRef.current !== requestId) {
          return;
        }

        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to load dashboard data.",
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
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title="Overview"
        description="Operational snapshot for users, content, payments, and moderation."
      />

      {isLoading ? (
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {Array.from({ length: 8 }).map((_, index) => (
            <Skeleton className="h-24 w-full" key={index} />
          ))}
        </div>
      ) : error ? (
        <Card className="p-4">
          <p className="text-sm font-semibold text-accent">Request failed</p>
          <p className="mt-1 text-sm text-muted">{error}</p>
        </Card>
      ) : summary ? (
        <>
          {/* Row 1 — 4 stat highlights */}
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <AdminStatCard
              label="Users"
              value={summary.totalUsers.toLocaleString("vi-VN")}
              meta={`${summary.activeUsers.toLocaleString("vi-VN")} active`}
              href="/users"
              icon={UsersIcon}
            />
            <AdminStatCard
              label="Revenue 30d"
              value={revenue ? formatVnd(revenue.totalAmount) : "—"}
              meta={
                revenue
                  ? `${summary.paidPayments.toLocaleString("vi-VN")} paid payments`
                  : undefined
              }
              href="/payments"
              icon={CreditCardIcon}
              iconClassName="bg-success/10 text-success"
            />
            <AdminStatCard
              label="Cafe pages"
              value={summary.totalCafePages.toLocaleString("vi-VN")}
              meta={`${summary.activeCafePages.toLocaleString("vi-VN")} active`}
              href="/cafes"
              icon={CoffeeIcon}
              iconClassName="bg-warning/10 text-warning"
            />
            <AdminStatCard
              label="Pending moderation"
              value={summary.pendingModerationItems.toLocaleString("vi-VN")}
              meta="Items waiting for review"
              href="/moderation"
              icon={ShieldCheckIcon}
              iconClassName="bg-destructive/10 text-destructive"
            />
          </div>

          {/* Row 2 — revenue chart + Vietnam map */}
          <div className="grid gap-3 lg:grid-cols-3">
            <SectionCard
              title="Revenue last 30 days"
              href="/payments"
              className="lg:col-span-2"
              meta={<RevenueLegend />}
            >
              {revenue ? (
                <AdminRevenueChart
                  analytics={revenue}
                  className="h-64 w-full"
                />
              ) : (
                <p className="text-sm text-muted">No revenue data.</p>
              )}
            </SectionCard>
            <Card
              className="group/section cursor-pointer transition hover:border-primary/40 hover:shadow-sm"
              onClick={() => router.push("/regions")}
            >
              <CardHeader className="flex flex-row items-center justify-between gap-3 p-4 pb-2">
                <CardTitle className="text-sm font-semibold text-espresso">
                  Users by province
                </CardTitle>
                <Link
                  href="/regions"
                  className="text-xs font-medium text-primary no-underline hover:underline"
                >
                  Open regions
                </Link>
              </CardHeader>
              <div className="p-4 pt-0">
                <VietnamMap
                  className="mx-auto h-72 max-w-56 cursor-pointer"
                  data={(regions ?? [])
                    .filter((region) => region.provinceCode !== "unknown")
                    .map((region) => ({
                      provinceCode: region.provinceCode,
                      provinceName: region.provinceName,
                      value: region.userCount,
                    }))}
                  valueLabel="users"
                  onProvinceClick={(code) =>
                    router.push(`/regions?province=${code}`)
                  }
                />
              </div>
            </Card>
          </div>

          {/* Row 3 — donut + blogs + reviewers + comments */}
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <SectionCard title="Payments by status" href="/payments">
              <AdminStatusDonut
                centerLabel="payments"
                slices={[
                  {
                    key: "paid",
                    label: "Paid",
                    value: summary.paidPayments,
                    color: "var(--chart-2)",
                  },
                  {
                    key: "pending",
                    label: "Pending",
                    value: summary.pendingPayments,
                    color: "var(--chart-3)",
                  },
                  {
                    key: "failed",
                    label: "Failed",
                    value: summary.failedPayments,
                    color: "var(--chart-5)",
                  },
                ]}
              />
            </SectionCard>
            <SectionCard title="Blogs by status" href="/blogs">
              <AdminStatusDonut
                centerLabel="blogs"
                slices={[
                  {
                    key: "published",
                    label: "Published",
                    value: summary.publishedBlogs,
                    color: "var(--chart-1)",
                  },
                  {
                    key: "hidden",
                    label: "Hidden",
                    value: summary.hiddenBlogs,
                    color: "var(--chart-3)",
                  },
                  {
                    key: "other",
                    label: "Other",
                    value: Math.max(
                      summary.totalBlogs -
                        summary.publishedBlogs -
                        summary.hiddenBlogs,
                      0,
                    ),
                    color: "var(--chart-4)",
                  },
                ]}
              />
            </SectionCard>
            <AdminStatCard
              label="Reviewers"
              value={summary.totalReviewers.toLocaleString("vi-VN")}
              meta="Active reviewer accounts"
              href="/ranking"
              icon={TrophyIcon}
              iconClassName="bg-chart-4/10 text-chart-4"
            />
            <div className="flex flex-col gap-3">
              <AdminStatCard
                label="Blogs"
                value={summary.totalBlogs.toLocaleString("vi-VN")}
                meta={`${summary.publishedBlogs.toLocaleString("vi-VN")} published`}
                href="/blogs"
                icon={FileTextIcon}
              />
              <AdminStatCard
                label="Comments"
                value={summary.totalComments.toLocaleString("vi-VN")}
                meta="Across all blogs"
                href="/comments"
                icon={MessageSquareIcon}
                iconClassName="bg-secondary text-secondary-foreground"
              />
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
}
