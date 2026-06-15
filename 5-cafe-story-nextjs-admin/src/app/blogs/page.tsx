import { AdminBlogsPage } from "@/components/admin/admin-blogs-page";
import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";

export default function BlogsPage() {
  return (
    <AdminDashboardShell>
      <AdminBlogsPage />
    </AdminDashboardShell>
  );
}
