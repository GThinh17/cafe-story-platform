import { AdminCafePagesPage } from "@/components/admin/admin-cafe-pages-page";
import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";

export default function CafesPage() {
  return (
    <AdminDashboardShell>
      <AdminCafePagesPage />
    </AdminDashboardShell>
  );
}
