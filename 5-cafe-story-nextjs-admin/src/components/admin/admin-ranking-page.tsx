"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { ListOrderedIcon, TrophyIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import { UserCell } from "@/components/admin/user-cell";
import {
  AdminDataTable,
  AdminPagination,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import type { PageResponse } from "@/types/api";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import {
  FilterInput,
  Toolbar,
} from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { generateReviewerRanking, getReviewerRanking } from "@/lib/api/admin";
import {
  formatNumber,
  localizeApiError,
  useEnumLabel,
  useI18n,
  useUiText,
} from "@/features/i18n";
import { cn } from "@/lib/utils";
import type {
  RankingPeriodType,
  ReviewerBadge,
  ReviewerRankingSnapshot,
} from "@/types/admin";

const periodTypes: RankingPeriodType[] = ["DAILY", "WEEKLY", "MONTHLY"];

const badgeVariant: Record<ReviewerBadge, "default" | "secondary" | "outline"> = {
  IRON: "outline",
  BRONZE: "secondary",
  SILVER: "secondary",
  GOLD: "default",
  DIAMOND: "default",
};

const PAGE_SIZE = 20;

function pad(value: number) {
  return String(value).padStart(2, "0");
}

function isoWeek(date: Date) {
  const target = new Date(date.getTime());
  target.setHours(0, 0, 0, 0);
  target.setDate(target.getDate() + 3 - ((target.getDay() + 6) % 7));
  const firstThursday = new Date(target.getFullYear(), 0, 4);
  const week =
    1 +
    Math.round(
      ((target.getTime() - firstThursday.getTime()) / 86400000 -
        3 +
        ((firstThursday.getDay() + 6) % 7)) /
        7,
    );

  return `${target.getFullYear()}-W${pad(week)}`;
}

function defaultPeriod(periodType: RankingPeriodType) {
  const now = new Date();

  if (periodType === "DAILY") {
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
  }

  if (periodType === "MONTHLY") {
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}`;
  }

  return isoWeek(now);
}

export function AdminRankingPage() {
  const { locale, localeTag, t } = useI18n();
  const ui = useUiText();
  const enumLabel = useEnumLabel();
  const [periodType, setPeriodType] = useState<RankingPeriodType>("DAILY");
  const [period, setPeriod] = useState(() => defaultPeriod("DAILY"));
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<PageResponse<ReviewerRankingSnapshot> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isGenerateOpen, setIsGenerateOpen] = useState(false);
  const [generatePeriodType, setGeneratePeriodType] = useState<RankingPeriodType>("DAILY");
  const [generateDate, setGenerateDate] = useState(() => defaultPeriod("DAILY"));
  const [generateMonth, setGenerateMonth] = useState(() => defaultPeriod("MONTHLY"));
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [generateError, setGenerateError] = useState<string | null>(null);

  const todayISO = defaultPeriod("DAILY");
  const thisMonthISO = defaultPeriod("MONTHLY");

  const load = useCallback(
    (signal?: AbortSignal) => {
      setIsLoading(true);
      setError(null);

      return getReviewerRanking({ period, periodType, page, size: PAGE_SIZE }, signal)
        .then((response) => setPageData(response))
        .catch((requestError: unknown) => {
          if (signal?.aborted) {
            return;
          }

          setError(localizeApiError(requestError, locale, t));
        })
        .finally(() => {
          if (!signal?.aborted) {
            setIsLoading(false);
          }
        });
    },
    [locale, page, period, periodType, t],
  );

  useEffect(() => {
    setPage(0);
  }, [period, periodType]);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);

    return () => controller.abort();
  }, [load]);

  const columns = useMemo<AdminTableColumn<ReviewerRankingSnapshot>[]>(
    () => {
      const baseColumns: AdminTableColumn<ReviewerRankingSnapshot>[] = [
        { header: "Rank", className: "w-16", cell: (row) => `#${row.rankPosition}` },
        {
          header: "Reviewer",
          cell: (row) => (
            <UserCell name={row.reviewerUserName} avatar={row.reviewerUserAvatar} />
          ),
        },
      ];

      if (periodType === "MONTHLY") {
        baseColumns.push({
          header: "Badge",
          cell: (row) =>
            row.badge ? (
              <Badge variant={badgeVariant[row.badge]}>{enumLabel(row.badge)}</Badge>
            ) : (
              <span className="text-muted-foreground">—</span>
            ),
        });
      }

      baseColumns.push(
        { header: "Score", cell: (row) => formatNumber(row.score, localeTag) },
        { header: "Likes", cell: (row) => formatNumber(row.likeCount, localeTag) },
        { header: "Shares", cell: (row) => formatNumber(row.shareCount, localeTag) },
        { header: "Comments", cell: (row) => formatNumber(row.commentCount, localeTag) },
      );

      return baseColumns;
    },
    [enumLabel, localeTag, periodType],
  );

  async function handleGenerate() {
    setIsSubmitting(true);
    setGenerateError(null);

    let referenceDate: string | undefined;
    if (generatePeriodType === "DAILY") {
      referenceDate = generateDate || undefined;
    } else if (generatePeriodType === "MONTHLY") {
      referenceDate = generateMonth ? `${generateMonth}-01` : undefined;
    }

    try {
      await generateReviewerRanking(generatePeriodType, referenceDate);
      setIsGenerateOpen(false);
      void load();
    } catch (requestError) {
      setGenerateError(localizeApiError(requestError, locale, t, "common.error.action"));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title="Reviewer Ranking"
        description="Browse reviewer ranking snapshots by period and generate new ones."
        actions={
          <div className="flex flex-wrap gap-2">
            <Button asChild type="button" variant="outline">
              <Link href="/formulas">
                <ListOrderedIcon data-icon="inline-start" />
                {ui("Scoring formulas")}
              </Link>
            </Button>
            <Button type="button" onClick={() => setIsGenerateOpen(true)}>
              <TrophyIcon data-icon="inline-start" />
              {ui("Generate snapshot")}
            </Button>
          </div>
        }
      />
      <Toolbar onRefresh={() => load()}>
        <div className="flex flex-col gap-1">
          <span className="text-xs font-medium text-muted">{ui("Period type")}</span>
          <Select
            value={periodType}
            onValueChange={(value) => {
              const nextType = value as RankingPeriodType;
              setPeriodType(nextType);
              setPeriod(defaultPeriod(nextType));
            }}
          >
            <SelectTrigger size="lg" className="rounded-md w-full bg-surface sm:w-44">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                {periodTypes.map((type) => (
                  <SelectItem value={type} key={type}>{enumLabel(type)}</SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>
        <FilterInput label="Period" value={period} placeholder="2026-06" onChange={setPeriod} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={pageData?.content ?? []}
        getRowKey={(row) => row.id}
        isLoading={isLoading}
        error={error}
        emptyTitle="No ranking data"
        emptyDescription="No snapshot found for this period. Use Generate snapshot to create one."
      />
      <AdminPagination page={pageData} onPageChange={setPage} />
      <AdminConfirmDialog
        open={isGenerateOpen}
        onOpenChange={(open) => {
          setIsGenerateOpen(open);
          if (!open) {
            setGenerateError(null);
            setGeneratePeriodType("DAILY");
            setGenerateDate(todayISO);
            setGenerateMonth(thisMonthISO);
          }
        }}
        title="Generate ranking snapshot"
        description="Chọn loại kỳ và ngày/tháng cụ thể để tạo snapshot xếp hạng."
        confirmLabel="Generate"
        isSubmitting={isSubmitting}
        onConfirm={handleGenerate}
      >
        <div className="flex flex-col gap-2">
          <span className="text-sm font-medium">{ui("Period type")}</span>
          <div className="flex rounded-md border border-border overflow-hidden">
            {periodTypes.map((type) => (
              <button
                key={type}
                type="button"
                className={cn(
                  "flex-1 px-4 py-2 text-sm font-semibold transition border-r border-border last:border-r-0",
                  generatePeriodType === type
                    ? "bg-primary text-white"
                    : "bg-surface text-muted hover:bg-surface-muted hover:text-foreground",
                )}
                onClick={() => setGeneratePeriodType(type)}
              >
                {enumLabel(type)}
              </button>
            ))}
          </div>
        </div>
        {generatePeriodType === "DAILY" ? (
          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium" htmlFor="generate-daily-date">
              {ui("Date")}
            </label>
            <input
              id="generate-daily-date"
              type="date"
              value={generateDate}
              max={todayISO}
              onChange={(event) => setGenerateDate(event.target.value)}
              className="rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
        ) : null}
        {generatePeriodType === "MONTHLY" ? (
          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium" htmlFor="generate-monthly-month">
              {ui("Month")}
            </label>
            <input
              id="generate-monthly-month"
              type="month"
              value={generateMonth}
              max={thisMonthISO}
              onChange={(event) => setGenerateMonth(event.target.value)}
              className="rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
        ) : null}
        {generateError ? <p className="text-sm text-accent">{generateError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
