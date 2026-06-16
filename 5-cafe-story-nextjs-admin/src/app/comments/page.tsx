import { AdminCommentsPage } from "@/components/admin/admin-comments-page";
import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";

export default function CommentsPage() {
  return (
    <AdminDashboardShell>
      <AdminCommentsPage />
    </AdminDashboardShell>
  );
}
