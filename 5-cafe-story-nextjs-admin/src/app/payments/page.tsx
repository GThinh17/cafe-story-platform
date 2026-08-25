import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";
import { AdminPaymentsPage } from "@/components/admin/admin-payments-page";

export default function PaymentsPage() {
  return (
    <AdminDashboardShell>
      <AdminPaymentsPage />
    </AdminDashboardShell>
  );
}
