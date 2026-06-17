"use client";

import { useEffect, useMemo, useState } from "react";
import {
  AdminDataTable,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import {
  AdminDetailDialog,
  AdminDetailField,
  AdminDetailGrid,
} from "@/components/admin/admin-detail-dialog";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { formatDate, shortId, Toolbar } from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { getReviewerFormulaThresholds, getReviewerFormulas } from "@/lib/api/admin";
import type { ReviewerBadgeThreshold, ReviewerScoringFormula } from "@/types/admin";

export function AdminRankingFormulaPage() {
  const [rows, setRows] = useState<ReviewerScoringFormula[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailFormula, setDetailFormula] = useState<ReviewerScoringFormula | null>(null);
  const [thresholds, setThresholds] = useState<ReviewerBadgeThreshold[]>([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  function load(signal?: AbortSignal) {
    setIsLoading(true);
    setError(null);

    return getReviewerFormulas(signal)
      .then((response) => setRows(response))
      .catch((requestError: unknown) => {
        if (signal?.aborted) {
          return;
        }

        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to load formulas.",
        );
      })
      .finally(() => {
        if (!signal?.aborted) {
          setIsLoading(false);
        }
      });
  }

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);

    return () => controller.abort();
  }, []);

  function openThresholds(formula: ReviewerScoringFormula) {
    const controller = new AbortController();
    setDetailFormula(formula);
    setDetailOpen(true);
    setThresholds([]);
    setDetailError(null);
    setDetailLoading(true);

    getReviewerFormulaThresholds(formula.id, controller.signal)
      .then((response) => setThresholds(response))
      .catch((requestError: unknown) => {
        if (controller.signal.aborted) {
          return;
        }

        setDetailError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to load thresholds.",
        );
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setDetailLoading(false);
        }
      });
  }

  const columns = useMemo<AdminTableColumn<ReviewerScoringFormula>[]>(
    () => [
      {
        header: "Formula",
        cell: (row) => (
          <div className="min-w-0">
            <p className="font-bold text-espresso">{shortId(row.id)}</p>
            <p className="text-xs text-muted">{row.description || "—"}</p>
          </div>
        ),
      },
      {
        header: "Weights (like/comment/share)",
        cell: (row) => `${row.likeWeight} / ${row.commentWeight} / ${row.shareWeight}`,
      },
      {
        header: "Payout (like/comment/share)",
        cell: (row) =>
          `${row.likePayoutAmount} / ${row.commentPayoutAmount} / ${row.sharePayoutAmount}`,
      },
      {
        header: "Status",
        cell: (row) => (
          <Badge variant={row.active ? "default" : "outline"}>
            {row.active ? "Active" : "Inactive"}
          </Badge>
        ),
      },
      { header: "Created", cell: (row) => formatDate(row.createdAt) },
      {
        header: "Actions",
        cell: (row) => (
          <Button type="button" variant="outline" size="sm" onClick={() => openThresholds(row)}>
            View thresholds
          </Button>
        ),
      },
    ],
    [],
  );

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Scoring Formulas"
        description="Công thức tính điểm reviewer dùng để tạo ranking snapshot. Formula active là formula đang áp dụng."
      />
      <Toolbar onRefresh={() => load()}>{null}</Toolbar>
      <AdminDataTable
        columns={columns}
        rows={rows}
        getRowKey={(row) => row.id}
        isLoading={isLoading}
        error={error}
        emptyTitle="Chưa có formula"
        emptyDescription="Chưa có scoring formula nào được tạo."
      />
      <AdminDetailDialog
        open={detailOpen}
        onOpenChange={setDetailOpen}
        title="Badge thresholds"
        description={detailFormula ? `Formula ${shortId(detailFormula.id)}` : undefined}
        isLoading={detailLoading}
        error={detailError}
      >
        {thresholds.length ? (
          <AdminDetailGrid>
            {thresholds.map((threshold) => (
              <AdminDetailField label={threshold.badge} key={threshold.id}>
                Min score: {threshold.minScore}
              </AdminDetailField>
            ))}
          </AdminDetailGrid>
        ) : (
          <p className="text-sm text-muted">Formula này chưa cấu hình threshold.</p>
        )}
      </AdminDetailDialog>
    </div>
  );
}
