"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { ListOrderedIcon, TrophyIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import {
  AdminDataTable,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import {
  FilterInput,
  FilterSelect,
  shortId,
  Toolbar,
} from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { generateReviewerRanking, getReviewerRanking } from "@/lib/api/admin";
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
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [generateError, setGenerateError] = useState<string | null>(null);

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
    () => [
      { header: "Rank", className: "w-16", cell: (row) => `#${row.rankPosition}` },
      { header: "Reviewer", cell: (row) => shortId(row.reviewerId) },
      {
        header: "Badge",
        cell: (row) => <Badge variant={badgeVariant[row.badge]}>{row.badge}</Badge>,
      },
      { header: "Score", cell: (row) => row.score },
      { header: "Likes", cell: (row) => row.likeCount },
      { header: "Shares", cell: (row) => row.shareCount },
      { header: "Comments", cell: (row) => row.commentCount },
    ],
    [],
  );

  async function handleGenerate() {
    setIsSubmitting(true);
    setGenerateError(null);

    try {
      await generateReviewerRanking(periodType);
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
        description="Xem ranking snapshot của reviewer theo period và tạo snapshot mới."
        actions={
          <div className="flex flex-wrap gap-2">
            <Button asChild type="button" variant="outline">
              <Link href="/ranking/formulas">
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
        <FilterSelect
          label="Period type"
          value={periodType}
          options={periodTypes}
          placeholder="Period type"
          onChange={(value) => {
            const nextType = (value || "DAILY") as RankingPeriodType;
            setPeriodType(nextType);
            setPeriod(defaultPeriod(nextType));
          }}
        />
        <FilterInput value={period} placeholder="Period, ví dụ 2026-06" onChange={setPeriod} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={rows}
        getRowKey={(row) => row.id}
        isLoading={isLoading}
        error={error}
        emptyTitle="Chưa có ranking"
        emptyDescription="Chưa có snapshot cho period này. Generate snapshot để tạo mới."
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
          }
        }}
        title="Generate ranking snapshot"
        description={`Tạo snapshot ranking mới cho period type ${periodType}.`}
        confirmLabel="Generate"
        isSubmitting={isSubmitting}
        onConfirm={handleGenerate}
      >
        {generateError ? <p className="text-sm text-accent">{generateError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
