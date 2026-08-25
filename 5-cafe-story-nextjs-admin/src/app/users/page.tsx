import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";
import { AdminUsersPage } from "@/components/admin/admin-users-page";

export default function UsersPage() {
  return (
    <AdminDashboardShell>
      <AdminUsersPage />
    </AdminDashboardShell>
  );
}
