import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";
import { AdminPayoutPage } from "@/components/admin/admin-payout-page";

export default function PayoutPage() {
  return (
    <AdminDashboardShell>
      <AdminPayoutPage />
    </AdminDashboardShell>
  );
}
