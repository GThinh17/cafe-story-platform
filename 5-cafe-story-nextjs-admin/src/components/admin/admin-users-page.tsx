"use client";

import { useMemo, useState } from "react";
import { EyeIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import {
  AdminDataTable,
  AdminPagination,
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
  BooleanFilterSelect,
  FilterInput,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  Toolbar,
  useAdminDetailResource,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { getAdminUser, getUsers, updateUserRoles, updateUserStatus } from "@/lib/api/admin";
import type { AdminUser, UserRole } from "@/types/admin";

const roles: UserRole[] = ["USER", "REVIEWER", "ADMIN", "CAFE_PAGE"];

type PendingUserAction =
  | { type: "status"; user: AdminUser; accountStatus: boolean }
  | { type: "roles"; user: AdminUser; roles: UserRole[] };

export function AdminUsersPage() {
  const [search, setSearch] = useState("");
  const [accountStatus, setAccountStatus] = useState<boolean | null>(null);
  const [role, setRole] = useState<UserRole | "">("");
  const [pendingAction, setPendingAction] = useState<PendingUserAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const detail = useAdminDetailResource<AdminUser>();

  const resource = usePagedAdminResource(
    (page, signal) =>
      getUsers({ search, accountStatus, role, page, size: PAGE_SIZE }, signal),
    [search, accountStatus, role],
  );

  const columns = useMemo<AdminTableColumn<AdminUser>[]>(
    () => [
      {
        header: "User",
        cell: (user) => (
          <div className="min-w-0">
            <p className="font-bold text-espresso">{user.userName}</p>
            <p className="text-xs text-muted">{user.userFullName || user.userEmail}</p>
          </div>
        ),
      },
      { header: "Email", cell: (user) => user.userEmail },
      {
        header: "Roles",
        cell: (user) => (
          <div className="flex flex-wrap gap-1">
            {user.roles.map((userRole) => (
              <Badge variant="secondary" key={userRole}>
                {userRole}
              </Badge>
            ))}
          </div>
        ),
      },
      {
        header: "Status",
        cell: (user) => <AdminStatusBadge value={user.accountStatus} />,
      },
      {
        header: "Engagement",
        cell: (user) => (
          <span className="text-muted">
            {user.userLike ?? 0} likes · {user.userFollower ?? 0} followers
          </span>
        ),
      },
      {
        header: "Actions",
        className: "w-80",
        cell: (user) => (
          <div className="flex flex-wrap gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => detail.load((signal) => getAdminUser(user.userId, signal))}
            >
              <EyeIcon data-icon="inline-start" />
              View
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() =>
                setPendingAction({
                  type: "status",
                  user,
                  accountStatus: !user.accountStatus,
                })
              }
            >
              {user.accountStatus ? "Deactivate" : "Activate"}
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() =>
                setPendingAction({
                  type: "roles",
                  user,
                  roles: user.roles.includes("REVIEWER")
                    ? user.roles.filter((item) => item !== "REVIEWER")
                    : Array.from(new Set([...user.roles, "REVIEWER"])),
                })
              }
            >
              Reviewer
            </Button>
          </div>
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
      if (pendingAction.type === "status") {
        const updatedUser = await updateUserStatus(
          pendingAction.user.userId,
          pendingAction.accountStatus,
        );
        if (detail.data?.userId === updatedUser.userId) {
          detail.setData(updatedUser);
        }
      } else {
        const updatedUser = await updateUserRoles(pendingAction.user.userId, pendingAction.roles);
        if (detail.data?.userId === updatedUser.userId) {
          detail.setData(updatedUser);
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
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Users"
        description="Search users, filter by status or role, and manage account activation."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterInput value={search} placeholder="Search users" onChange={setSearch} />
        <BooleanFilterSelect label="Account status" value={accountStatus} onChange={setAccountStatus} />
        <FilterSelect
          label="Role"
          value={role}
          options={roles}
          placeholder="All roles"
          onChange={setRole}
        />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(user) => user.userId}
        isLoading={resource.isLoading}
        error={resource.error}
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />
      <AdminDetailDialog
        open={detail.open}
        onOpenChange={detail.setOpen}
        title="User detail"
        description={detail.data ? detail.data.userId : "Latest detail from admin API"}
        isLoading={detail.isLoading}
        error={detail.error}
      >
        {detail.data ? (
          <AdminDetailGrid>
            <AdminDetailField label="Username">{detail.data.userName}</AdminDetailField>
            <AdminDetailField label="Full name">{detail.data.userFullName || "-"}</AdminDetailField>
            <AdminDetailField label="Email">{detail.data.userEmail}</AdminDetailField>
            <AdminDetailField label="Phone">{detail.data.userPhone ?? "-"}</AdminDetailField>
            <AdminDetailField label="Status">
              <AdminStatusBadge value={detail.data.accountStatus} />
            </AdminDetailField>
            <AdminDetailField label="Roles">
              <div className="flex flex-wrap gap-1">
                {detail.data.roles.map((userRole) => (
                  <Badge variant="secondary" key={userRole}>
                    {userRole}
                  </Badge>
                ))}
              </div>
            </AdminDetailField>
            <AdminDetailField label="Engagement">
              {detail.data.userLike ?? 0} likes / {detail.data.userFollower ?? 0} followers
            </AdminDetailField>
            <AdminDetailField label="Region">{detail.data.regionId || "-"}</AdminDetailField>
            <AdminDetailField label="Loaded at">{formatDate(new Date().toISOString())}</AdminDetailField>
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
        title="Confirm user update"
        description="This changes the selected user account immediately."
        confirmLabel="Update user"
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
