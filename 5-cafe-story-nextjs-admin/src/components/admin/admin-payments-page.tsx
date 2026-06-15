"use client";

import { useMemo, useState } from "react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import {
  AdminDataTable,
  AdminPagination,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { AdminStatusBadge } from "@/components/admin/admin-status-badge";
import {
  FilterInput,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  Toolbar,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Button } from "@/components/ui/button";
import { getPayments, markBankTransferPaid, refundPayment } from "@/lib/api/admin";
import type { Payment, PaymentStatus } from "@/types/admin";

const paymentStatuses: PaymentStatus[] = [
  "PENDING",
  "PAID",
  "FAILED",
  "CANCELLED",
  "EXPIRED",
  "REFUNDED",
];

type PendingPaymentAction =
  | { type: "markPaid"; payment: Payment }
  | { type: "refund"; payment: Payment };

export function AdminPaymentsPage() {
  const [paymentStatus, setPaymentStatus] = useState<PaymentStatus | "">("");
  const [buyerId, setBuyerId] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingPaymentAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resource = usePagedAdminResource(
    (page, signal) =>
      getPayments({ paymentStatus, buyerId, page, size: PAGE_SIZE }, signal),
    [paymentStatus, buyerId],
  );

  const columns = useMemo<AdminTableColumn<Payment>[]>(
    () => [
      { header: "Payment", cell: (payment) => payment.paymentId.slice(0, 8) },
      { header: "Buyer", cell: (payment) => payment.buyerId.slice(0, 8) },
      { header: "Method", cell: (payment) => payment.paymentMethod },
      {
        header: "Amount",
        cell: (payment) => `${payment.amount.toLocaleString()} ${payment.currency}`,
      },
      { header: "Status", cell: (payment) => <AdminStatusBadge value={payment.paymentStatus} /> },
      { header: "Created", cell: (payment) => formatDate(payment.createdAt) },
      {
        header: "Actions",
        className: "w-56",
        cell: (payment) => (
          <div className="flex flex-wrap gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              disabled={payment.paymentStatus === "PAID"}
              onClick={() => setPendingAction({ type: "markPaid", payment })}
            >
              Mark paid
            </Button>
            <Button
              type="button"
              variant="destructive"
              size="sm"
              disabled={payment.paymentStatus !== "PAID"}
              onClick={() => setPendingAction({ type: "refund", payment })}
            >
              Refund
            </Button>
          </div>
        ),
      },
    ],
    [],
  );

  async function handleConfirm() {
    if (!pendingAction) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);

    try {
      if (pendingAction.type === "markPaid") {
        await markBankTransferPaid(pendingAction.payment.paymentId);
      } else {
        await refundPayment(pendingAction.payment.paymentId);
      }

      setPendingAction(null);
      resource.refetch();
    } catch (requestError) {
      setActionError(
        requestError instanceof Error ? requestError.message : "Action failed.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Payments"
        description="Audit payment status, mark bank transfers paid, and issue refunds."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterSelect
          value={paymentStatus}
          options={paymentStatuses}
          placeholder="Status"
          onChange={setPaymentStatus}
        />
        <FilterInput value={buyerId} placeholder="Buyer ID" onChange={setBuyerId} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(payment) => payment.paymentId}
        isLoading={resource.isLoading}
        error={resource.error}
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />
      <AdminConfirmDialog
        open={Boolean(pendingAction)}
        onOpenChange={(open) => {
          if (!open) {
            setPendingAction(null);
            setActionError(null);
          }
        }}
        title="Confirm payment action"
        description="This updates the payment record immediately."
        confirmLabel="Apply"
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
