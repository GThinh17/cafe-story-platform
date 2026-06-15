import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";
import { AdminOverview } from "@/components/admin/admin-overview";

export default function Page() {
  return (
    <AdminDashboardShell>
      <AdminOverview />
    </AdminDashboardShell>
  );
}
