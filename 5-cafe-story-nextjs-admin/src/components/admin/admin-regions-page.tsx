"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "next/navigation";
import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from "recharts";
import { AdminDataTable, type AdminTableColumn } from "@/components/admin/admin-data-table";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { FilterInput, Toolbar } from "@/components/admin/admin-page-utils";
import { VietnamMap } from "@/components/admin/vietnam-map";
import { Badge } from "@/components/ui/badge";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { getRegionAnalytics } from "@/lib/api/admin";
import { formatNumber, localizeApiError, useI18n, useUiText } from "@/features/i18n";
import { cn } from "@/lib/utils";
import type { AdminRegionAnalytics } from "@/types/admin";

type RegionMetric = "users" | "cafes" | "reviewers";

const METRIC_CONFIG: Record<
  RegionMetric,
  { field: keyof AdminRegionAnalytics; color: string }
> = {
  users: { field: "userCount", color: "var(--chart-1)" },
  cafes: { field: "cafePageCount", color: "var(--chart-3)" },
  reviewers: {
    field: "reviewerCount",
    color: "var(--chart-2)",
  },
};

const METRIC_LABELS: Record<RegionMetric, string> = {
  users: "Users",
  cafes: "Cafes",
  reviewers: "Reviewers",
};

function metricValue(region: AdminRegionAnalytics, metric: RegionMetric) {
  return region[METRIC_CONFIG[metric].field] as number;
}

export function AdminRegionsPage() {
  const { locale, localeTag, t } = useI18n();
  const ui = useUiText();
  const searchParams = useSearchParams();
  const [regions, setRegions] = useState<AdminRegionAnalytics[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [metric, setMetric] = useState<RegionMetric>("users");
  const [search, setSearch] = useState("");
  const [selectedProvince, setSelectedProvince] = useState<string | null>(
    searchParams.get("province"),
  );
  const requestIdRef = useRef(0);

  const load = useMemo(
    () => () => {
      const controller = new AbortController();
      const requestId = requestIdRef.current + 1;
      requestIdRef.current = requestId;
      setIsLoading(true);
      setError(null);

      getRegionAnalytics(controller.signal)
        .then((response) => {
          if (requestIdRef.current === requestId) {
            setRegions(response);
          }
        })
        .catch((requestError) => {
          if (controller.signal.aborted || requestIdRef.current !== requestId) {
            return;
          }
          setError(localizeApiError(requestError, locale, t));
        })
        .finally(() => {
          if (!controller.signal.aborted && requestIdRef.current === requestId) {
            setIsLoading(false);
          }
        });

      return controller;
    },
    [locale, t],
  );

  useEffect(() => {
    const controller = load();
    return () => controller.abort();
  }, [load]);

  const provinces = useMemo(
    () => regions.filter((region) => region.provinceCode !== "unknown"),
    [regions],
  );
  const unknownRow = useMemo(
    () => regions.find((region) => region.provinceCode === "unknown") ?? null,
    [regions],
  );

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();
    const sorted = [...provinces].sort(
      (a, b) => metricValue(b, metric) - metricValue(a, metric),
    );
    const rows = query
      ? sorted.filter((region) =>
          region.provinceName.toLowerCase().includes(query),
        )
      : sorted;
    return unknownRow && !query ? [...rows, unknownRow] : rows;
  }, [provinces, unknownRow, search, metric]);

  const topProvinces = useMemo(
    () =>
      [...provinces]
        .sort((a, b) => metricValue(b, metric) - metricValue(a, metric))
        .slice(0, 10)
        .map((region) => ({
          name: region.provinceName
            .replace(/^Tỉnh /, "")
            .replace(/^Thành phố /, ""),
          value: metricValue(region, metric),
        })),
    [provinces, metric],
  );

  const chartConfig: ChartConfig = {
    value: { label: ui(METRIC_LABELS[metric]), color: METRIC_CONFIG[metric].color },
  };

  const columns: AdminTableColumn<AdminRegionAnalytics>[] = [
    {
      header: "Province",
      className: "min-w-56",
      cell: (row) => (
        <div className="flex items-center gap-2">
          <Badge variant="outline" className="font-mono">
            {row.provinceCode}
          </Badge>
          <span
            className={cn(
              "text-sm font-medium",
              row.provinceCode === selectedProvince && "text-primary",
            )}
          >
            {row.provinceName}
          </span>
        </div>
      ),
    },
    {
      header: "Users",
      className: "w-28 text-right",
      cell: (row) => (
        <span className="tabular-nums">{formatNumber(row.userCount, localeTag)}</span>
      ),
    },
    {
      header: "Cafes",
      className: "w-28 text-right",
      cell: (row) => (
        <span className="tabular-nums">
          {formatNumber(row.cafePageCount, localeTag)}
        </span>
      ),
    },
    {
      header: "Reviewers",
      className: "w-28 text-right",
      cell: (row) => (
        <span className="tabular-nums">
          {formatNumber(row.reviewerCount, localeTag)}
        </span>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title="Regions"
        description="Users, cafe pages, and reviewers grouped by province."
      />

      <Toolbar onRefresh={() => load()}>
        <FilterInput
          value={search}
          placeholder="Search province"
          onChange={setSearch}
        />
        <Tabs
          value={metric}
          onValueChange={(value) => setMetric(value as RegionMetric)}
        >
          <TabsList>
            {(Object.keys(METRIC_CONFIG) as RegionMetric[]).map((key) => (
              <TabsTrigger key={key} value={key}>
                {ui(METRIC_LABELS[key])}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
      </Toolbar>

      <div className="grid gap-3 lg:grid-cols-2">
        <Card>
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-semibold text-espresso">
              {ui("{metric} by province", { metric: ui(METRIC_LABELS[metric]) })}
            </CardTitle>
          </CardHeader>
          <div className="p-4 pt-0">
            <VietnamMap
              className="mx-auto h-96 max-w-72"
              data={provinces.map((region) => ({
                provinceCode: region.provinceCode,
                provinceName: region.provinceName,
                value: metricValue(region, metric),
              }))}
              valueLabel={ui(METRIC_LABELS[metric]).toLocaleLowerCase(localeTag)}
              selectedProvince={selectedProvince}
              onProvinceClick={(code) =>
                setSelectedProvince((current) =>
                  current === code ? null : code,
                )
              }
            />
          </div>
        </Card>
        <Card>
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-semibold text-espresso">
              Top 10 provinces
            </CardTitle>
          </CardHeader>
          <div className="p-4 pt-0">
            <ChartContainer config={chartConfig} className="h-96 w-full">
              <BarChart
                data={topProvinces}
                layout="vertical"
                margin={{ top: 0, right: 12, bottom: 0, left: 8 }}
              >
                <CartesianGrid horizontal={false} stroke="var(--line-soft)" />
                <XAxis
                  type="number"
                  tickLine={false}
                  axisLine={false}
                  tick={{ fontSize: 11 }}
                  allowDecimals={false}
                />
                <YAxis
                  type="category"
                  dataKey="name"
                  tickLine={false}
                  axisLine={false}
                  width={92}
                  tick={{ fontSize: 11 }}
                />
                <ChartTooltip content={<ChartTooltipContent />} />
                <Bar
                  dataKey="value"
                  fill={METRIC_CONFIG[metric].color}
                  radius={[0, 3, 3, 0]}
                  maxBarSize={18}
                />
              </BarChart>
            </ChartContainer>
          </div>
        </Card>
      </div>

      <AdminDataTable
        columns={columns}
        rows={filtered}
        getRowKey={(row) => row.provinceCode}
        isLoading={isLoading}
        error={error}
        emptyTitle="No provinces"
        emptyDescription="Region analytics is empty."
        onRowClick={(row) =>
          setSelectedProvince((current) =>
            current === row.provinceCode ? null : row.provinceCode,
          )
        }
      />
    </div>
  );
}
