"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { ArrowLeftIcon, PlusIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import {
  AdminDataTable,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { Toolbar } from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { activatePayoutFormula, createPayoutFormula, getPayoutFormulas } from "@/lib/api/admin";
import type { PayoutFormula } from "@/types/admin";

function LabeledInput({
  label,
  value,
  onChange,
  type = "number",
  step,
  min,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  type?: string;
  step?: string;
  min?: string;
}) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-semibold text-muted">{label}</span>
      <input
        type={type}
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

export function AdminPayoutFormulasPage() {
  const [rows, setRows] = useState<PayoutFormula[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [form, setForm] = useState(DEFAULT_FORM);
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  const [activatingId, setActivatingId] = useState<string | null>(null);

  const load = useCallback((signal?: AbortSignal) => {
    setIsLoading(true);
    setError(null);
    return getPayoutFormulas(signal)
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

  async function handleCreate() {
    setIsCreating(true);
    setCreateError(null);
    try {
      await createPayoutFormula({
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
      await activatePayoutFormula(id);
      void load();
    } finally {
      setActivatingId(null);
    }
  }

  function setField(key: keyof typeof DEFAULT_FORM) {
    return (v: string) => setForm((prev) => ({ ...prev, [key]: v }));
  }

  const columns = useMemo<AdminTableColumn<PayoutFormula>[]>(
    () => [
      {
        header: "Active",
        className: "w-20",
        cell: (row) =>
          row.active ? (
            <Badge variant="default">Active</Badge>
          ) : (
            <Badge variant="outline">Inactive</Badge>
          ),
      },
      {
        header: "Like / Comment / Share",
        cell: (row) =>
          `${row.likePayoutAmount.toLocaleString()} / ${row.commentPayoutAmount.toLocaleString()} / ${row.sharePayoutAmount.toLocaleString()}`,
      },
      {
        header: "Iron",
        className: "text-right",
        cell: (row) => `×${row.ironMultiplier}`,
      },
      {
        header: "Bronze",
        className: "text-right",
        cell: (row) => `×${row.bronzeMultiplier}`,
      },
      {
        header: "Silver",
        className: "text-right",
        cell: (row) => `×${row.silverMultiplier}`,
      },
      {
        header: "Gold",
        className: "text-right",
        cell: (row) => `×${row.goldMultiplier}`,
      },
      {
        header: "Diamond",
        className: "text-right",
        cell: (row) => `×${row.diamondMultiplier}`,
      },
      {
        header: "Mô tả",
        cell: (row) => (
          <span className="text-muted">{row.description ?? "—"}</span>
        ),
      },
      {
        header: "",
        className: "w-24",
        cell: (row) =>
          row.active ? null : (
            <Button
              type="button"
              variant="outline"
              size="sm"
              disabled={activatingId === row.id}
              onClick={() => handleActivate(row.id)}
            >
              Activate
            </Button>
          ),
      },
    ],
    [activatingId],
  );

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Payout Formulas"
        description="Payout formula decoupled from scoring. Only one formula is active at a time."
        actions={
          <div className="flex flex-wrap gap-2">
            <Button asChild type="button" variant="outline">
              <Link href="/payout">
                <ArrowLeftIcon data-icon="inline-start" />
                Payouts
              </Link>
            </Button>
            <Button type="button" onClick={() => setIsCreateOpen(true)}>
              <PlusIcon data-icon="inline-start" />
              Tạo Formula
            </Button>
          </div>
        }
      />

      <Toolbar onRefresh={() => load()}>{null}</Toolbar>

      <AdminDataTable
        columns={columns}
        rows={rows}
        getRowKey={(row) => row.id}
        isLoading={isLoading}
        error={error}
        emptyTitle="No payout formulas"
        emptyDescription="Create a formula to start calculating payouts for reviewers."
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
        title="Create payout formula"
        description="New formula will be in Inactive status. Activate to apply."
        confirmLabel="Create"
        isSubmitting={isCreating}
        onConfirm={handleCreate}
      >
        <div className="grid grid-cols-3 gap-3">
          <LabeledInput label="Like payout" value={form.likePayoutAmount} onChange={setField("likePayoutAmount")} min="0" />
          <LabeledInput label="Comment payout" value={form.commentPayoutAmount} onChange={setField("commentPayoutAmount")} min="0" />
          <LabeledInput label="Share payout" value={form.sharePayoutAmount} onChange={setField("sharePayoutAmount")} min="0" />
        </div>
        <div className="grid grid-cols-5 gap-3">
          <LabeledInput label="×Iron" value={form.ironMultiplier} onChange={setField("ironMultiplier")} step="0.01" min="1" />
          <LabeledInput label="×Bronze" value={form.bronzeMultiplier} onChange={setField("bronzeMultiplier")} step="0.01" min="1" />
          <LabeledInput label="×Silver" value={form.silverMultiplier} onChange={setField("silverMultiplier")} step="0.01" min="1" />
          <LabeledInput label="×Gold" value={form.goldMultiplier} onChange={setField("goldMultiplier")} step="0.01" min="1" />
          <LabeledInput label="×Diamond" value={form.diamondMultiplier} onChange={setField("diamondMultiplier")} step="0.01" min="1" />
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
        {createError ? (
          <p className="text-sm text-accent">{createError}</p>
        ) : null}
      </AdminConfirmDialog>
    </div>
  );
}
