import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";
import { AdminRankingPage } from "@/components/admin/admin-ranking-page";

export default function RankingPage() {
  return (
    <AdminDashboardShell>
      <AdminRankingPage />
    </AdminDashboardShell>
  );
}
