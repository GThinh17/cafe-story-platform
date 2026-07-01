"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { ListOrderedIcon, TrophyIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import { UserCell } from "@/components/admin/user-cell";
import {
  AdminDataTable,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
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

const LIMIT = 20;

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
  const [periodType, setPeriodType] = useState<RankingPeriodType>("DAILY");
  const [period, setPeriod] = useState(() => defaultPeriod("DAILY"));
  const [page, setPage] = useState(1);
  const [rows, setRows] = useState<ReviewerRankingSnapshot[]>([]);
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

      return getReviewerRanking({ period, periodType, page, limit: LIMIT }, signal)
        .then((response) => setRows(response))
        .catch((requestError: unknown) => {
          if (signal?.aborted) {
            return;
          }

          setError(
            requestError instanceof Error
              ? requestError.message
              : "Unable to load ranking.",
          );
        })
        .finally(() => {
          if (!signal?.aborted) {
            setIsLoading(false);
          }
        });
    },
    [period, periodType, page],
  );

  useEffect(() => {
    setPage(1);
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
              <Badge variant={badgeVariant[row.badge]}>{row.badge}</Badge>
            ) : (
              <span className="text-muted-foreground">—</span>
            ),
        });
      }

      baseColumns.push(
        { header: "Score", cell: (row) => row.score },
        { header: "Likes", cell: (row) => row.likeCount },
        { header: "Shares", cell: (row) => row.shareCount },
        { header: "Comments", cell: (row) => row.commentCount },
      );

      return baseColumns;
    },
    [periodType],
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
      setGenerateError(
        requestError instanceof Error ? requestError.message : "Generate failed.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Reviewer Ranking"
        description="Browse reviewer ranking snapshots by period and generate new ones."
        actions={
          <div className="flex flex-wrap gap-2">
            <Button asChild type="button" variant="outline">
              <Link href="/formulas">
                <ListOrderedIcon data-icon="inline-start" />
                Scoring formulas
              </Link>
            </Button>
            <Button type="button" onClick={() => setIsGenerateOpen(true)}>
              <TrophyIcon data-icon="inline-start" />
              Generate snapshot
            </Button>
          </div>
        }
      />
      <Toolbar onRefresh={() => load()}>
        <div className="flex flex-col gap-1">
          <span className="text-xs font-medium text-muted">Period type</span>
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
                  <SelectItem value={type} key={type}>{type}</SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>
        <FilterInput label="Period" value={period} placeholder="2026-06" onChange={setPeriod} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={rows}
        getRowKey={(row) => row.id}
        isLoading={isLoading}
        error={error}
        emptyTitle="No ranking data"
        emptyDescription="No snapshot found for this period. Use Generate snapshot to create one."
      />
      <div className="flex items-center justify-between rounded-md border border-border bg-surface px-4 py-3 text-sm text-muted">
        <span>Page {page}</span>
        <div className="flex gap-2">
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={page <= 1}
            onClick={() => setPage((current) => Math.max(1, current - 1))}
          >
            Previous
          </Button>
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={rows.length < LIMIT}
            onClick={() => setPage((current) => current + 1)}
          >
            Next
          </Button>
        </div>
      </div>
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
          <span className="text-sm font-medium">Period type</span>
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
                {type.charAt(0) + type.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </div>
        {generatePeriodType === "DAILY" ? (
          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium" htmlFor="generate-daily-date">
              Ngày
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
              Tháng
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
