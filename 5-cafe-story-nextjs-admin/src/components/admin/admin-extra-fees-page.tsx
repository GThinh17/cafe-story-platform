"use client";

import { useMemo, useState } from "react";
import { PencilIcon, PowerIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import {
  AdminDataTable,
  AdminPagination,
  AdminRowActions,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { AdminStatusBadge } from "@/components/admin/admin-status-badge";
import {
  BooleanFilterSelect,
  FilterSelect,
  FormTextarea,
  PAGE_SIZE,
  Toolbar,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  createExtraFee,
  getExtraFees,
  updateExtraFee,
  updateExtraFeeStatus,
} from "@/lib/api/admin";
import type { ExtraFee, ExtraFeeRequest, ExtraFeeType } from "@/types/admin";

const feeTypes: ExtraFeeType[] = ["REVIEWER_REGISTRATION", "CAFE_PAGE_OPENING"];

type PendingExtraFeeAction = { type: "status"; fee: ExtraFee; status: boolean };

type FeeFormState = {
  extraFeeId?: string;
  name: string;
  description: string;
  feeType: ExtraFeeType;
  price: string;
  durationMonths: string;
  maxMembers: string;
  status: boolean;
};

const emptyForm: FeeFormState = {
  name: "",
  description: "",
  feeType: "REVIEWER_REGISTRATION",
  price: "0",
  durationMonths: "",
  maxMembers: "",
  status: true,
};

function formFromFee(fee: ExtraFee): FeeFormState {
  return {
    extraFeeId: fee.extraFeeId,
    name: fee.name,
    description: fee.description ?? "",
    feeType: fee.feeType,
    price: String(fee.price),
    durationMonths: fee.durationMonths ? String(fee.durationMonths) : "",
    maxMembers: fee.maxMembers ? String(fee.maxMembers) : "",
    status: Boolean(fee.status),
  };
}

function toRequest(form: FeeFormState): ExtraFeeRequest {
  return {
    name: form.name.trim(),
    description: form.description.trim() || null,
    feeType: form.feeType,
    price: Number(form.price),
    durationMonths: form.durationMonths ? Number(form.durationMonths) : null,
    maxMembers: form.maxMembers ? Number(form.maxMembers) : null,
    status: form.status,
  };
}

export function AdminExtraFeesPage() {
  const [status, setStatus] = useState<boolean | null>(null);
  const [pendingAction, setPendingAction] = useState<PendingExtraFeeAction | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState<FeeFormState>(emptyForm);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resource = usePagedAdminResource(
    (page, signal) => getExtraFees({ status, page, size: PAGE_SIZE }, signal),
    [status],
  );

  const columns = useMemo<AdminTableColumn<ExtraFee>[]>(
    () => [
      {
        header: "Fee",
        cell: (fee) => (
          <div className="max-w-sm">
            <p className="font-bold text-espresso">{fee.name}</p>
            <p className="mt-1 text-sm text-muted">{fee.description || fee.feeType}</p>
          </div>
        ),
      },
      { header: "Type", cell: (fee) => fee.feeType },
      { header: "Price", cell: (fee) => fee.price.toLocaleString() },
      {
        header: "Duration",
        cell: (fee) =>
          fee.durationMonths ? `${fee.durationMonths} months` : "One time",
      },
      { header: "Members", cell: (fee) => fee.maxMembers ?? "—" },
      { header: "Status", cell: (fee) => <AdminStatusBadge value={fee.status} /> },
      {
        header: "",
        className: "w-12 text-right",
        cell: (fee) => (
          <AdminRowActions
            actions={[
              {
                label: "Edit fee",
                icon: PencilIcon,
                onSelect: () => {
                  setForm(formFromFee(fee));
                  setActionError(null);
                  setFormOpen(true);
                },
              },
              {
                label: fee.status ? "Disable" : "Enable",
                icon: PowerIcon,
                destructive: Boolean(fee.status),
                onSelect: () =>
                  setPendingAction({ type: "status", fee, status: !fee.status }),
              },
            ]}
          />
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
      await updateExtraFeeStatus(pendingAction.fee.extraFeeId, pendingAction.status);

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

  async function handleSubmitForm() {
    const request = toRequest(form);

    if (!request.name || Number.isNaN(request.price)) {
      setActionError("Name and numeric price are required.");
      return;
    }

    setIsSubmitting(true);
    setActionError(null);

    try {
      if (form.extraFeeId) {
        await updateExtraFee(form.extraFeeId, request);
      } else {
        await createExtraFee(request);
      }

      setFormOpen(false);
      setForm(emptyForm);
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
        title="Extra Fees"
        description="Create and manage reviewer registration and cafe page opening fees."
        actions={
          <Button
            type="button"
            onClick={() => {
              setForm(emptyForm);
              setActionError(null);
              setFormOpen(true);
            }}
          >
            Create fee
          </Button>
        }
      />
      <Toolbar onRefresh={resource.refetch}>
        <BooleanFilterSelect
          label="Status"
          value={status}
          onChange={setStatus}
          trueLabel="Enabled"
          falseLabel="Disabled"
        />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(fee) => fee.extraFeeId}
        isLoading={resource.isLoading}
        error={resource.error}
        onRowClick={(fee) => {
          setForm(formFromFee(fee));
          setActionError(null);
          setFormOpen(true);
        }}
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />

      <Dialog open={formOpen} onOpenChange={setFormOpen}>
        <DialogContent className="max-w-2xl p-5">
          <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-2">
              <DialogTitle>{form.extraFeeId ? "Edit fee" : "Create fee"}</DialogTitle>
              <DialogDescription>
                Fields map to the backend ExtraFeeRequestDTO contract.
              </DialogDescription>
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <Input
                value={form.name}
                placeholder="Name"
                onChange={(event) => setForm({ ...form, name: event.target.value })}
              />
              <Input
                type="number"
                min={0}
                value={form.price}
                placeholder="Price"
                onChange={(event) => setForm({ ...form, price: event.target.value })}
              />
              <FilterSelect
                label="Fee type"
                value={form.feeType}
                options={feeTypes}
                placeholder="Fee type"
                onChange={(feeType) =>
                  setForm({ ...form, feeType: feeType || "REVIEWER_REGISTRATION" })
                }
              />
              <BooleanFilterSelect
                label="Status"
                value={form.status}
                onChange={(nextStatus) =>
                  setForm({ ...form, status: nextStatus ?? true })
                }
                trueLabel="Enabled"
                falseLabel="Disabled"
              />
              <Input
                type="number"
                min={1}
                value={form.durationMonths}
                placeholder="Duration months"
                onChange={(event) =>
                  setForm({ ...form, durationMonths: event.target.value })
                }
              />
              <Input
                type="number"
                min={1}
                value={form.maxMembers}
                placeholder="Max members"
                onChange={(event) =>
                  setForm({ ...form, maxMembers: event.target.value })
                }
              />
              <div className="sm:col-span-2">
                <FormTextarea
                  value={form.description}
                  placeholder="Description"
                  onChange={(description) => setForm({ ...form, description })}
                />
              </div>
            </div>
            {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
            <div className="flex justify-end gap-2">
              <Button
                type="button"
                variant="outline"
                disabled={isSubmitting}
                onClick={() => setFormOpen(false)}
              >
                Cancel
              </Button>
              <Button type="button" disabled={isSubmitting} onClick={handleSubmitForm}>
                {isSubmitting ? "Saving..." : "Save"}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <AdminConfirmDialog
        open={Boolean(pendingAction)}
        onOpenChange={(open) => {
          if (!open) {
            setPendingAction(null);
            setActionError(null);
          }
        }}
        title="Confirm extra fee action"
        description="This updates the selected fee immediately."
        confirmLabel="Apply"
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
