"use client";

import { useMemo, useState } from "react";
import { CheckCircle2Icon, EyeIcon, RotateCcwIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import { UserCell } from "@/components/admin/user-cell";
import {
  AdminDataTable,
  AdminPagination,
  AdminRowActions,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import {
  AdminDetailDialog,
  AdminDetailField,
  AdminDetailGrid,
} from "@/components/admin/admin-detail-dialog";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { AdminStatusBadge } from "@/components/admin/admin-status-badge";
import {
  FilterInput,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  Toolbar,
  useAdminDetailResource,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import {
  getAdminPayment,
  getPayments,
  markBankTransferPaid,
  refundPayment,
} from "@/lib/api/admin";
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
  const detail = useAdminDetailResource<Payment>();

  const resource = usePagedAdminResource(
    (page, signal) =>
      getPayments({ paymentStatus, buyerId, page, size: PAGE_SIZE }, signal),
    [paymentStatus, buyerId],
  );

  const columns = useMemo<AdminTableColumn<Payment>[]>(
    () => [
      { header: "Payment", cell: (payment) => payment.paymentId.slice(0, 8) },
      {
        header: "Buyer",
        cell: (payment) => (
          <UserCell name={payment.buyerUserName} avatar={payment.buyerUserAvatar} />
        ),
      },
      { header: "Product", cell: (payment) => payment.productName ?? "—" },
      { header: "Method", cell: (payment) => payment.paymentMethod },
      {
        header: "Amount",
        cell: (payment) => `${payment.amount.toLocaleString()} ${payment.currency}`,
      },
      { header: "Status", cell: (payment) => <AdminStatusBadge value={payment.paymentStatus} /> },
      { header: "Created", cell: (payment) => formatDate(payment.createdAt) },
      {
        header: "",
        className: "w-12 text-right",
        cell: (payment) => (
          <AdminRowActions
            actions={[
              {
                label: "View detail",
                icon: EyeIcon,
                onSelect: () =>
                  detail.load((signal) =>
                    getAdminPayment(payment.paymentId, signal),
                  ),
              },
              {
                label: "Mark paid",
                icon: CheckCircle2Icon,
                disabled: payment.paymentStatus === "PAID",
                onSelect: () =>
                  setPendingAction({ type: "markPaid", payment }),
              },
              {
                label: "Refund",
                icon: RotateCcwIcon,
                destructive: true,
                disabled: payment.paymentStatus !== "PAID",
                onSelect: () => setPendingAction({ type: "refund", payment }),
              },
            ]}
          />
        ),
      },
    ],
    [detail],
  );

  async function handleConfirm() {
    if (!pendingAction) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);

    try {
      if (pendingAction.type === "markPaid") {
        const updatedPayment = await markBankTransferPaid(pendingAction.payment.paymentId);
        if (detail.data?.paymentId === updatedPayment.paymentId) {
          detail.setData(updatedPayment);
        }
      } else {
        const updatedPayment = await refundPayment(pendingAction.payment.paymentId);
        if (detail.data?.paymentId === updatedPayment.paymentId) {
          detail.setData(updatedPayment);
        }
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
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title="Payments"
        description="Audit payment status, mark bank transfers paid, and issue refunds."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterSelect
          label="Payment status"
          value={paymentStatus}
          options={paymentStatuses}
          placeholder="All statuses"
          onChange={setPaymentStatus}
        />
        <FilterInput label="Buyer ID" value={buyerId} placeholder="Enter user ID" onChange={setBuyerId} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(payment) => payment.paymentId}
        isLoading={resource.isLoading}
        error={resource.error}
        onRowClick={(payment) =>
          detail.load((signal) => getAdminPayment(payment.paymentId, signal))
        }
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />
      <AdminDetailDialog
        open={detail.open}
        onOpenChange={detail.setOpen}
        title="Payment detail"
        description={detail.data ? detail.data.paymentId : "Latest detail from admin API"}
        isLoading={detail.isLoading}
        error={detail.error}
      >
        {detail.data ? (
          <AdminDetailGrid>
            <AdminDetailField label="Payment ID">{detail.data.paymentId}</AdminDetailField>
            <AdminDetailField label="Buyer">
              <UserCell
                name={detail.data.buyerUserFullName || detail.data.buyerUserName}
                avatar={detail.data.buyerUserAvatar}
                subtitle={detail.data.buyerUserName || detail.data.buyerId}
              />
            </AdminDetailField>
            <AdminDetailField label="Status">
              <AdminStatusBadge value={detail.data.paymentStatus} />
            </AdminDetailField>
            <AdminDetailField label="Method">{detail.data.paymentMethod}</AdminDetailField>
            <AdminDetailField label="Amount">
              {detail.data.amount.toLocaleString()} {detail.data.currency}
            </AdminDetailField>
            <AdminDetailField label="Product name">{detail.data.productName || "-"}</AdminDetailField>
            {detail.data.extraFeeType ? (
              <AdminDetailField label="Extra fee type">{detail.data.extraFeeType}</AdminDetailField>
            ) : null}
            <AdminDetailField label="Transfer content">
              {detail.data.transferContent || "-"}
            </AdminDetailField>
            <AdminDetailField label="Payment URL" className="sm:col-span-2">
              {detail.data.paymentUrl || "-"}
            </AdminDetailField>
            <AdminDetailField label="QR code" className="sm:col-span-2">
              {detail.data.qrCodeUrl ? (
                <img
                  alt="Payment QR code"
                  className="max-h-72 rounded-md border border-border object-contain"
                  src={detail.data.qrCodeUrl}
                />
              ) : (
                "-"
              )}
            </AdminDetailField>
            <AdminDetailField label="Created">{formatDate(detail.data.createdAt)}</AdminDetailField>
            <AdminDetailField label="Paid">{formatDate(detail.data.paidAt)}</AdminDetailField>
            <AdminDetailField label="Expired">{formatDate(detail.data.expiredAt)}</AdminDetailField>
          </AdminDetailGrid>
        ) : null}
      </AdminDetailDialog>
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
