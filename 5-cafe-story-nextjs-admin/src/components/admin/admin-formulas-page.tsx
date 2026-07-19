"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { FlaskConicalIcon, PlusIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import {
  AdminDataTable,
  AdminRowActions,
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
import {
  activateFormula,
  createFormula,
  getFormulas,
  getFormulaThresholds,
} from "@/lib/api/admin";
import type { ReviewerBadgeThreshold, ReviewerFormula } from "@/types/admin";

function LabeledInput({
  label,
  value,
  onChange,
  step,
  min,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  step?: string;
  min?: string;
}) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-semibold text-muted">{label}</span>
      <input
        type="number"
        step={step}
        min={min}
        className="flex h-9 w-full rounded-md border border-input bg-surface px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}

const DEFAULT_FORM = {
  likeWeight: "1",
  commentWeight: "5",
  shareWeight: "3",
  likePayoutAmount: "100",
  commentPayoutAmount: "500",
  sharePayoutAmount: "300",
  ironMultiplier: "1.00",
  bronzeMultiplier: "1.20",
  silverMultiplier: "1.50",
  goldMultiplier: "2.00",
  diamondMultiplier: "3.00",
  description: "",
};

export function AdminFormulasPage() {
  const [rows, setRows] = useState<ReviewerFormula[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [form, setForm] = useState(DEFAULT_FORM);
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  const [activatingId, setActivatingId] = useState<string | null>(null);

  const [detailOpen, setDetailOpen] = useState(false);
  const [detailFormula, setDetailFormula] = useState<ReviewerFormula | null>(null);
  const [thresholds, setThresholds] = useState<ReviewerBadgeThreshold[]>([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  const load = useCallback((signal?: AbortSignal) => {
    setIsLoading(true);
    setError(null);
    return getFormulas(signal)
      .then(setRows)
      .catch((err: unknown) => {
        if (signal?.aborted) return;
        setError(err instanceof Error ? err.message : "Không tải được formulas.");
      })
      .finally(() => {
        if (!signal?.aborted) setIsLoading(false);
      });
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load]);

  function openThresholds(formula: ReviewerFormula) {
    const controller = new AbortController();
    setDetailFormula(formula);
    setDetailOpen(true);
    setThresholds([]);
    setDetailError(null);
    setDetailLoading(true);
    getFormulaThresholds(formula.id, controller.signal)
      .then(setThresholds)
      .catch((err: unknown) => {
        if (controller.signal.aborted) return;
        setDetailError(err instanceof Error ? err.message : "Không tải được thresholds.");
      })
      .finally(() => {
        if (!controller.signal.aborted) setDetailLoading(false);
      });
  }

  async function handleCreate() {
    setIsCreating(true);
    setCreateError(null);
    try {
      await createFormula({
        likeWeight: Number(form.likeWeight),
        commentWeight: Number(form.commentWeight),
        shareWeight: Number(form.shareWeight),
        likePayoutAmount: Number(form.likePayoutAmount),
        commentPayoutAmount: Number(form.commentPayoutAmount),
        sharePayoutAmount: Number(form.sharePayoutAmount),
        ironMultiplier: Number(form.ironMultiplier),
        bronzeMultiplier: Number(form.bronzeMultiplier),
        silverMultiplier: Number(form.silverMultiplier),
        goldMultiplier: Number(form.goldMultiplier),
        diamondMultiplier: Number(form.diamondMultiplier),
        description: form.description || null,
      });
      setIsCreateOpen(false);
      setForm(DEFAULT_FORM);
      void load();
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : "Tạo formula thất bại.");
    } finally {
      setIsCreating(false);
    }
  }

  async function handleActivate(id: string) {
    setActivatingId(id);
    try {
      await activateFormula(id);
      void load();
    } finally {
      setActivatingId(null);
    }
  }

  function setField(key: keyof typeof DEFAULT_FORM) {
    return (v: string) => setForm((prev) => ({ ...prev, [key]: v }));
  }

  const columns = useMemo<AdminTableColumn<ReviewerFormula>[]>(
    () => [
      {
        header: "Formula",
        cell: (row) => (
          <div className="min-w-0">
            <p className="font-bold text-espresso">{shortId(row.id)}</p>
            <p className="text-xs text-muted">{row.description ?? "—"}</p>
          </div>
        ),
      },
      {
        header: "Status",
        className: "w-24",
        cell: (row) => (
          <Badge variant={row.active ? "default" : "outline"}>
            {row.active ? "Active" : "Inactive"}
          </Badge>
        ),
      },
      {
        header: "Weights (like/comment/share)",
        cell: (row) => `${row.likeWeight} / ${row.commentWeight} / ${row.shareWeight}`,
      },
      {
        header: "Payout (like/comment/share)",
        cell: (row) =>
          `${row.likePayoutAmount.toLocaleString()} / ${row.commentPayoutAmount.toLocaleString()} / ${row.sharePayoutAmount.toLocaleString()}`,
      },
      {
        header: "Multipliers (Iron→Diamond)",
        cell: (row) =>
          `×${row.ironMultiplier} / ×${row.bronzeMultiplier} / ×${row.silverMultiplier} / ×${row.goldMultiplier} / ×${row.diamondMultiplier}`,
      },
      { header: "Created", cell: (row) => formatDate(row.createdAt) },
      {
        header: "",
        className: "w-12 text-right",
        cell: (row) => (
          <AdminRowActions
            actions={[
              {
                label: "Thresholds",
                onSelect: () => openThresholds(row),
              },
              ...(row.active
                ? []
                : [
                    {
                      label: "Activate",
                      disabled: activatingId === row.id,
                      onSelect: () => handleActivate(row.id),
                    },
                  ]),
            ]}
          />
        ),
      },
    ],
    [activatingId],
  );

  return (
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title="Formulas"
        description="Quản lý scoring weights, payout amounts và badge multipliers trong một chỗ. Chỉ một formula được active tại một thời điểm."
        actions={
          <Button type="button" onClick={() => setIsCreateOpen(true)}>
            <PlusIcon data-icon="inline-start" />
            Tạo Formula
          </Button>
        }
      />

      <Toolbar onRefresh={() => load()}>{null}</Toolbar>

      <AdminDataTable
        columns={columns}
        rows={rows}
        getRowKey={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRowClick={openThresholds}
        emptyTitle="No formulas"
        emptyDescription="Tạo formula đầu tiên để cấu hình scoring và payout cho reviewer."
      />

      <AdminConfirmDialog
        open={isCreateOpen}
        onOpenChange={(open) => {
          setIsCreateOpen(open);
          if (!open) {
            setCreateError(null);
            setForm(DEFAULT_FORM);
          }
        }}
        title="Create formula"
        description="Formula mới sẽ ở trạng thái Inactive. Activate để áp dụng."
        confirmLabel="Create"
        isSubmitting={isCreating}
        onConfirm={handleCreate}
      >
        <div className="flex flex-col gap-4">
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted">Scoring Weights</p>
            <div className="grid grid-cols-3 gap-3">
              <LabeledInput label="Like weight" value={form.likeWeight} onChange={setField("likeWeight")} min="0" />
              <LabeledInput label="Comment weight" value={form.commentWeight} onChange={setField("commentWeight")} min="0" />
              <LabeledInput label="Share weight" value={form.shareWeight} onChange={setField("shareWeight")} min="0" />
            </div>
          </div>
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted">Payout Amounts</p>
            <div className="grid grid-cols-3 gap-3">
              <LabeledInput label="Like payout" value={form.likePayoutAmount} onChange={setField("likePayoutAmount")} min="0" />
              <LabeledInput label="Comment payout" value={form.commentPayoutAmount} onChange={setField("commentPayoutAmount")} min="0" />
              <LabeledInput label="Share payout" value={form.sharePayoutAmount} onChange={setField("sharePayoutAmount")} min="0" />
            </div>
          </div>
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted">Badge Multipliers</p>
            <div className="grid grid-cols-5 gap-3">
              <LabeledInput label="×Iron" value={form.ironMultiplier} onChange={setField("ironMultiplier")} step="0.01" min="1" />
              <LabeledInput label="×Bronze" value={form.bronzeMultiplier} onChange={setField("bronzeMultiplier")} step="0.01" min="1" />
              <LabeledInput label="×Silver" value={form.silverMultiplier} onChange={setField("silverMultiplier")} step="0.01" min="1" />
              <LabeledInput label="×Gold" value={form.goldMultiplier} onChange={setField("goldMultiplier")} step="0.01" min="1" />
              <LabeledInput label="×Diamond" value={form.diamondMultiplier} onChange={setField("diamondMultiplier")} step="0.01" min="1" />
            </div>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-xs font-semibold text-muted">Description (optional)</span>
            <input
              type="text"
              className="flex h-9 w-full rounded-md border border-input bg-surface px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              placeholder="Notes about this formula..."
              value={form.description}
              onChange={(e) => setField("description")(e.target.value)}
            />
          </div>
          {createError ? <p className="text-sm text-accent">{createError}</p> : null}
        </div>
      </AdminConfirmDialog>

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
          <p className="text-sm text-muted">Formula này chưa có threshold nào.</p>
        )}
      </AdminDetailDialog>
    </div>
  );
}
