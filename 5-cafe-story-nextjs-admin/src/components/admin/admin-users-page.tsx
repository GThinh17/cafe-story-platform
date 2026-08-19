"use client";

import { useMemo, useState } from "react";
import { EyeIcon, PowerIcon, StarIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
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
  BooleanFilterSelect,
  FilterInput,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  Toolbar,
  useAdminDetailResource,
  useDebouncedValue,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { UserCell } from "@/components/admin/user-cell";
import { getUsers, updateUserRoles, updateUserStatus } from "@/lib/api/admin";
import {
  formatNumber,
  localizeApiError,
  useEnumLabel,
  useI18n,
  useUiText,
} from "@/features/i18n";
import type { AdminUser, UserRole } from "@/types/admin";

const roles: UserRole[] = ["USER", "REVIEWER", "ADMIN", "CAFE_PAGE"];

type PendingUserAction =
  | { type: "status"; user: AdminUser; accountStatus: boolean }
  | { type: "roles"; user: AdminUser; roles: UserRole[] };

export function AdminUsersPage() {
  const { locale, localeTag, t } = useI18n();
  const ui = useUiText();
  const enumLabel = useEnumLabel();
  const [search, setSearch] = useState("");
  const [accountStatus, setAccountStatus] = useState<boolean | null>(null);
  const [role, setRole] = useState<UserRole | "">("");
  const [pendingAction, setPendingAction] = useState<PendingUserAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const detail = useAdminDetailResource<AdminUser>();
  const debouncedSearch = useDebouncedValue(search);

  const resource = usePagedAdminResource(
    (page, signal) =>
      getUsers({ search: debouncedSearch, accountStatus, role, page, size: PAGE_SIZE }, signal),
    [debouncedSearch, accountStatus, role],
  );

  function openDetail(user: AdminUser) {
    // Row already has everything the dialog renders — skip the redundant GET.
    void detail.load(async () => user);
  }

  const columns = useMemo<AdminTableColumn<AdminUser>[]>(
    () => [
      {
        header: "User",
        cell: (user) => (
          <UserCell
            name={user.userName}
            avatar={user.userAvatar}
            subtitle={user.userFullName || user.userEmail}
          />
        ),
      },
      { header: "Email", cell: (user) => user.userEmail },
      {
        header: "Roles",
        cell: (user) => (
          <div className="flex flex-wrap gap-1">
            {user.roles.map((userRole) => (
              <Badge variant="secondary" key={userRole}>
                {enumLabel(userRole)}
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
            {ui("{likes} likes · {followers} followers", {
              likes: formatNumber(user.userLike ?? 0, localeTag),
              followers: formatNumber(user.userFollower ?? 0, localeTag),
            })}
          </span>
        ),
      },
      {
        header: "",
        className: "w-12 text-right",
        cell: (user) => (
          <AdminRowActions
            actions={[
              {
                label: "View detail",
                icon: EyeIcon,
                onSelect: () => openDetail(user),
              },
              {
                label: ui(user.accountStatus ? "Deactivate" : "Activate"),
                icon: PowerIcon,
                destructive: user.accountStatus,
                onSelect: () =>
                  setPendingAction({
                    type: "status",
                    user,
                    accountStatus: !user.accountStatus,
                  }),
              },
              {
                label: ui(
                  user.roles.includes("REVIEWER")
                    ? "Remove reviewer role"
                    : "Grant reviewer role",
                ),
                icon: StarIcon,
                onSelect: () =>
                  setPendingAction({
                    type: "roles",
                    user,
                    roles: user.roles.includes("REVIEWER")
                      ? user.roles.filter((item) => item !== "REVIEWER")
                      : Array.from(new Set([...user.roles, "REVIEWER"])),
                  }),
              },
            ]}
          />
        ),
      },
    ],
    [detail, enumLabel, localeTag, ui],
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
      setActionError(localizeApiError(requestError, locale, t, "common.error.action"));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title="Users"
        description="Search users, filter by status or role, and manage account activation."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterInput label="Search" value={search} placeholder="Name, email, username" onChange={setSearch} />
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
        onRowClick={openDetail}
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
                    {enumLabel(userRole)}
                  </Badge>
                ))}
              </div>
            </AdminDetailField>
            <AdminDetailField label="Engagement">
              {ui("{likes} likes / {followers} followers", {
                likes: formatNumber(detail.data.userLike ?? 0, localeTag),
                followers: formatNumber(detail.data.userFollower ?? 0, localeTag),
              })}
            </AdminDetailField>
            <AdminDetailField label="Region">{detail.data.regionId || "-"}</AdminDetailField>
            <AdminDetailField label="Loaded at">{formatDate(new Date().toISOString(), localeTag)}</AdminDetailField>
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
