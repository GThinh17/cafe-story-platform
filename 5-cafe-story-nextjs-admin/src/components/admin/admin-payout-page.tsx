"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { BanknoteIcon, CalendarIcon, ListOrderedIcon } from "lucide-react";
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
  useDebouncedValue,
  FilterSelect,
  shortId,
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
import {
  generateMonthlyPayout,
  generatePayoutIncome,
  getMonthlyPayouts,
  getPayoutIncome,
  updatePayoutStatus,
} from "@/lib/api/admin";
import {
  formatCurrency,
  formatDate,
  formatNumber,
  localizeApiError,
  useEnumLabel,
  useI18n,
  useUiText,
} from "@/features/i18n";
import type {
  AdminPayout,
  AdminPayoutStatus,
  ReviewerBadge,
  ReviewerIncome,
} from "@/types/admin";

type ViewType = "DAILY" | "MONTHLY";

const VIEW_OPTIONS = ["DAILY", "MONTHLY"] as const;
const SORT_OPTIONS = ["desc", "asc"] as const;
const STATUS_OPTIONS: readonly AdminPayoutStatus[] = [
  "PENDING",
  "APPROVED",
  "PAID",
  "CANCELLED",
];

const badgeVariant: Record<ReviewerBadge, "default" | "secondary" | "outline"> = {
  IRON: "outline",
  BRONZE: "secondary",
  SILVER: "secondary",
  GOLD: "default",
  DIAMOND: "default",
};

const statusVariant: Record<AdminPayoutStatus, "default" | "secondary" | "outline"> = {
  PENDING: "outline",
  APPROVED: "secondary",
  PAID: "default",
  CANCELLED: "outline",
};

// Server là nơi duy nhất định nghĩa luồng trạng thái hợp lệ
// (AdminPayoutServiceImpl.validateStatusTransition) và trả về qua
// allowedTransitions. Không chép lại luật ở client để khỏi lệch.
function nextStatuses(row: AdminPayout): AdminPayoutStatus[] {
  return row.allowedTransitions ?? [];
}

// toISOString() trả giờ UTC: ở UTC+7, trước 07:00 sáng nó lùi thêm một ngày
// nữa và admin generate nhầm income của hôm kia. Format theo giờ local như
// prevMonth()/currentMonth() bên dưới.
function todayMinus1() {
  const d = new Date();
  d.setDate(d.getDate() - 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(
    d.getDate(),
  ).padStart(2, "0")}`;
}

function prevMonth() {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

function currentMonth() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

const PAGE_SIZE = 20;

export function AdminPayoutPage() {
  const { locale, localeTag, t } = useI18n();
  const ui = useUiText();
  const enumLabel = useEnumLabel();
  const [viewType, setViewType] = useState<ViewType>("MONTHLY");
  const [sortDir, setSortDir] = useState<"asc" | "desc">("desc");
  // MONTHLY mặc định tháng trước: payout chỉ tồn tại cho tháng đã kết thúc, để
  // currentMonth() thì mở trang lúc nào cũng thấy bảng rỗng.
  const [month, setMonth] = useState(prevMonth);
  const [reviewerId, setReviewerId] = useState("");
  const debouncedReviewerId = useDebouncedValue(reviewerId);
  const [statusFilter, setStatusFilter] = useState<AdminPayoutStatus | "">("");
  const [page, setPage] = useState(0);

  const [incomeRows, setIncomeRows] = useState<ReviewerIncome[]>([]);
  const [monthlyRows, setMonthlyRows] = useState<AdminPayout[]>([]);
  const [total, setTotal] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Daily income generate modal
  const [isIncomeGenerateOpen, setIsIncomeGenerateOpen] = useState(false);
  const [generateDate, setGenerateDate] = useState(todayMinus1);
  const [isGeneratingIncome, setIsGeneratingIncome] = useState(false);
  const [incomeGenerateError, setIncomeGenerateError] = useState<string | null>(null);

  // Monthly payout generate modal
  const [isMonthlyGenerateOpen, setIsMonthlyGenerateOpen] = useState(false);
  const [generateMonth, setGenerateMonth] = useState(prevMonth);
  const [isGeneratingMonthly, setIsGeneratingMonthly] = useState(false);
  const [monthlyGenerateError, setMonthlyGenerateError] = useState<string | null>(null);

  // Status update modal
  const [selected, setSelected] = useState<AdminPayout | null>(null);
  const [newStatus, setNewStatus] = useState<AdminPayoutStatus | "">("");
  const [note, setNote] = useState("");
  const [isUpdating, setIsUpdating] = useState(false);
  const [updateError, setUpdateError] = useState<string | null>(null);

  const load = useCallback(
    (signal?: AbortSignal) => {
      setIsLoading(true);
      setError(null);
      if (viewType === "DAILY") {
        return getPayoutIncome(
          { month, reviewerId: debouncedReviewerId || undefined, page, size: PAGE_SIZE, sortDir },
          signal,
        )
          .then((res) => {
            setIncomeRows(res.content);
            setTotal(res.totalElements);
          })
          .catch((err: unknown) => {
            if (signal?.aborted) return;
            setError(localizeApiError(err, locale, t));
          })
          .finally(() => {
            if (!signal?.aborted) setIsLoading(false);
          });
      } else {
        return getMonthlyPayouts(
          { month, status: statusFilter || undefined, page, size: PAGE_SIZE, sortDir },
          signal,
        )
          .then((res) => {
            setMonthlyRows(res.content);
            setTotal(res.totalElements);
          })
          .catch((err: unknown) => {
            if (signal?.aborted) return;
            setError(localizeApiError(err, locale, t));
          })
          .finally(() => {
            if (!signal?.aborted) setIsLoading(false);
          });
      }
    },
    [debouncedReviewerId, locale, month, page, sortDir, statusFilter, t, viewType],
  );

  useEffect(() => {
    setPage(0);
    setReviewerId("");
    setStatusFilter("");
    setMonth(viewType === "MONTHLY" ? prevMonth() : currentMonth());
  }, [viewType]);

  useEffect(() => {
    setPage(0);
  }, [month, debouncedReviewerId, statusFilter, sortDir]);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load]);

  async function handleGenerateIncome() {
    setIsGeneratingIncome(true);
    setIncomeGenerateError(null);
    try {
      await generatePayoutIncome(generateDate);
      setIsIncomeGenerateOpen(false);
      // Nhảy bộ lọc sang đúng kỳ vừa generate, nếu không admin bấm xong vẫn
      // nhìn vào tháng cũ và tưởng lệnh không chạy.
      setMonth(generateDate.slice(0, 7));
      // setMonth không đổi giá trị thì effect không chạy lại — vẫn phải reload tay.
      void load();
    } catch (err) {
      setIncomeGenerateError(localizeApiError(err, locale, t, "common.error.action"));
    } finally {
      setIsGeneratingIncome(false);
    }
  }

  async function handleGenerateMonthly() {
    setIsGeneratingMonthly(true);
    setMonthlyGenerateError(null);
    try {
      await generateMonthlyPayout(generateMonth);
      setIsMonthlyGenerateOpen(false);
      setMonth(generateMonth);
      void load();
    } catch (err) {
      setMonthlyGenerateError(localizeApiError(err, locale, t, "common.error.action"));
    } finally {
      setIsGeneratingMonthly(false);
    }
  }

  async function handleUpdateStatus() {
    if (!selected || !newStatus) return;
    setIsUpdating(true);
    setUpdateError(null);
    try {
      await updatePayoutStatus(selected.id, newStatus, note || undefined);
      setSelected(null);
      void load();
    } catch (err) {
      setUpdateError(localizeApiError(err, locale, t, "common.error.action"));
    } finally {
      setIsUpdating(false);
    }
  }

  const incomeColumns = useMemo<AdminTableColumn<ReviewerIncome>[]>(
    () => [
      { header: "Date", cell: (row) => formatDate(`${row.incomeDate}T00:00:00`, localeTag) },
      {
        header: "Reviewer",
        cell: (row) => (
          <UserCell name={row.reviewerUserName} avatar={row.reviewerUserAvatar} />
        ),
      },
      {
        header: "Badge",
        cell: (row) => (
          <Badge variant={badgeVariant[row.badge]}>{enumLabel(row.badge)}</Badge>
        ),
      },
      { header: "Likes", className: "text-right", cell: (row) => formatNumber(row.likeCount, localeTag) },
      { header: "Comments", className: "text-right", cell: (row) => formatNumber(row.commentCount, localeTag) },
      { header: "Shares", className: "text-right", cell: (row) => formatNumber(row.shareCount, localeTag) },
      { header: "Base", className: "text-right", cell: (row) => formatCurrency(row.baseAmount, "VND", localeTag) },
      { header: "×", className: "text-right", cell: (row) => row.badgeMultiplier },
      {
        // Số này nhân hệ số badge của TỪNG NGÀY. Payout tháng lại nhân hệ số
        // badge của cả THÁNG vào tổng base, nên cộng cột này lại sẽ không bằng
        // Final ở tab MONTHLY. Số chốt để chi trả là số ở tab MONTHLY.
        header: "Final (est. daily badge)",
        className: "text-right font-semibold",
        cell: (row) => formatCurrency(row.finalAmount, "VND", localeTag),
      },
    ],
    [enumLabel, localeTag],
  );

  const monthlyColumns = useMemo<AdminTableColumn<AdminPayout>[]>(
    () => [
      { header: "Month", cell: (row) => row.payoutMonth },
      {
        header: "Reviewer",
        cell: (row) => (
          <UserCell name={row.reviewerUserName} avatar={row.reviewerUserAvatar} />
        ),
      },
      {
        header: "Badge",
        cell: (row) => (
          <Badge variant={badgeVariant[row.badge]}>{enumLabel(row.badge)}</Badge>
        ),
      },
      {
        header: "Base",
        className: "text-right",
        cell: (row) => formatCurrency(row.totalBaseAmount, "VND", localeTag),
      },
      { header: "×", className: "text-right", cell: (row) => row.badgeMultiplier },
      {
        header: "Final",
        className: "text-right font-semibold",
        cell: (row) => formatCurrency(row.totalFinalAmount, "VND", localeTag),
      },
      {
        header: "Status",
        cell: (row) => (
          <Badge variant={statusVariant[row.status]}>{enumLabel(row.status)}</Badge>
        ),
      },
      {
        header: "",
        className: "w-24",
        cell: (row) => {
          const options = nextStatuses(row);
          if (options.length === 0) return null;
          return (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => {
                setSelected(row);
                setNewStatus(options[0]);
                setNote("");
                setUpdateError(null);
              }}
            >
              {ui("Update")}
            </Button>
          );
        },
      },
    ],
    [enumLabel, localeTag, ui],
  );

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const currentRowsLength =
    viewType === "DAILY" ? incomeRows.length : monthlyRows.length;
  const pageResponse: PageResponse<unknown> | null = total > 0 || currentRowsLength > 0
    ? {
        content: [],
        totalElements: total,
        totalPages,
        number: page,
        size: PAGE_SIZE,
        first: page === 0,
        last: page + 1 >= totalPages,
      }
    : null;

  return (
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title={viewType === "DAILY" ? "Reviewer Income" : "Monthly Payouts"}
        description={
          viewType === "DAILY"
            ? "Daily income for each reviewer based on interaction counts."
            : "Manage monthly payouts for reviewers. Review and mark as paid."
        }
        actions={
          <div className="flex flex-wrap gap-2">
            <Button asChild type="button" variant="outline">
              <Link href="/formulas">
                <ListOrderedIcon data-icon="inline-start" />
                {ui("Formulas")}
              </Link>
            </Button>
            <Button type="button" variant="outline" onClick={() => setIsIncomeGenerateOpen(true)}>
              <CalendarIcon data-icon="inline-start" />
              {ui("Generate Income")}
            </Button>
            {viewType === "MONTHLY" && (
              <Button type="button" onClick={() => setIsMonthlyGenerateOpen(true)}>
                <BanknoteIcon data-icon="inline-start" />
                {ui("Generate Payout")}
              </Button>
            )}
          </div>
        }
      />

      <Toolbar onRefresh={() => load()}>
        <FilterSelect<ViewType>
          value={viewType}
          options={VIEW_OPTIONS}
          placeholder="View by"
          label="View by"
          onChange={(v) => setViewType(v || "MONTHLY")}
        />
        <FilterInput
          value={month}
          placeholder="yyyy-MM"
          label="Month"
          onChange={setMonth}
        />
        {viewType === "DAILY" && (
          <FilterInput
            value={reviewerId}
            placeholder="Reviewer ID"
            label="Reviewer"
            onChange={setReviewerId}
          />
        )}
        {viewType === "MONTHLY" && (
          <FilterSelect<AdminPayoutStatus>
            value={statusFilter}
            options={STATUS_OPTIONS}
            placeholder="Status"
            label="Status"
            onChange={setStatusFilter}
          />
        )}
        <FilterSelect<"asc" | "desc">
          value={sortDir}
          options={SORT_OPTIONS}
          placeholder="Final ↓ / ↑"
          label="Sort"
          onChange={(v) => setSortDir(v || "desc")}
        />
      </Toolbar>

      {viewType === "DAILY" ? (
        <AdminDataTable
          columns={incomeColumns}
          rows={incomeRows}
          getRowKey={(row) => row.id}
          isLoading={isLoading}
          error={error}
          emptyTitle="No income data"
          emptyDescription="Generate income to create data for this period."
        />
      ) : (
        <AdminDataTable
          columns={monthlyColumns}
          rows={monthlyRows}
          getRowKey={(row) => row.id}
          isLoading={isLoading}
          error={error}
          emptyTitle="No payouts"
          emptyDescription="Generate payout to aggregate this month's income data."
        />
      )}

      <AdminPagination page={pageResponse} onPageChange={setPage} />

      {/* Daily income generate modal */}
      <AdminConfirmDialog
        open={isIncomeGenerateOpen}
        onOpenChange={(open) => {
          setIsIncomeGenerateOpen(open);
          if (!open) {
            setIncomeGenerateError(null);
            setGenerateDate(todayMinus1());
          }
        }}
        title="Generate daily income"
        description="Calculate income for all reviewers on the selected date. Existing data will be updated."
        confirmLabel="Generate"
        isSubmitting={isGeneratingIncome}
        onConfirm={handleGenerateIncome}
      >
        <div className="flex flex-col gap-2">
          <span className="text-sm font-medium">{ui("Date (yyyy-MM-dd)")}</span>
          <input
            type="date"
            className="flex h-10 w-full rounded-md border border-input bg-surface px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={generateDate}
            onChange={(e) => setGenerateDate(e.target.value)}
          />
        </div>
        {incomeGenerateError ? (
          <p className="text-sm text-accent">{incomeGenerateError}</p>
        ) : null}
      </AdminConfirmDialog>

      {/* Monthly payout generate modal */}
      <AdminConfirmDialog
        open={isMonthlyGenerateOpen}
        onOpenChange={(open) => {
          setIsMonthlyGenerateOpen(open);
          if (!open) {
            setMonthlyGenerateError(null);
            setGenerateMonth(prevMonth());
          }
        }}
        title="Generate monthly payout"
        description="Aggregate income for the selected month into payouts. Existing records will be updated."
        confirmLabel="Generate"
        isSubmitting={isGeneratingMonthly}
        onConfirm={handleGenerateMonthly}
      >
        <div className="flex flex-col gap-2">
          <span className="text-sm font-medium">{ui("Month (yyyy-MM)")}</span>
          <input
            type="month"
            className="flex h-10 w-full rounded-md border border-input bg-surface px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={generateMonth}
            onChange={(e) => setGenerateMonth(e.target.value)}
          />
        </div>
        {monthlyGenerateError ? (
          <p className="text-sm text-accent">{monthlyGenerateError}</p>
        ) : null}
      </AdminConfirmDialog>

      {/* Status update modal */}
      <AdminConfirmDialog
        open={selected !== null}
        onOpenChange={(open) => {
          if (!open) setSelected(null);
        }}
        title="Update payout status"
        description={
          selected
            ? ui("Reviewer: {reviewer} · Month: {month} · Final: {amount}", {
                reviewer: shortId(selected.reviewerId),
                month: selected.payoutMonth,
                amount: formatCurrency(selected.totalFinalAmount, "VND", localeTag),
              })
            : ""
        }
        confirmLabel="Confirm"
        isSubmitting={isUpdating}
        onConfirm={handleUpdateStatus}
      >
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <span className="text-sm font-medium">{ui("New status")}</span>
            <Select
              value={newStatus}
              onValueChange={(v) => setNewStatus(v as AdminPayoutStatus)}
            >
              <SelectTrigger className="bg-surface">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  {selected
                    ? nextStatuses(selected).map((s) => (
                        <SelectItem key={s} value={s}>
                          {enumLabel(s)}
                        </SelectItem>
                      ))
                    : null}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>
          <div className="flex flex-col gap-2">
            <span className="text-sm font-medium">{ui("Notes (optional)")}</span>
            <textarea
              className="flex min-h-20 w-full rounded-md border border-input bg-surface px-3 py-2 text-sm ring-offset-background placeholder:text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring resize-none"
              placeholder={ui("Enter notes...")}
              value={note}
              onChange={(e) => setNote(e.target.value)}
            />
          </div>
        </div>
        {updateError ? (
          <p className="text-sm text-accent">{updateError}</p>
        ) : null}
      </AdminConfirmDialog>
    </div>
  );
}
