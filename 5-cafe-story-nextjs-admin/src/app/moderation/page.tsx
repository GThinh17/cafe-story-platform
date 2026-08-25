import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";
import { AdminModerationPage } from "@/components/admin/admin-moderation-page";

export default function ModerationPage() {
  return (
    <AdminDashboardShell>
      <AdminModerationPage />
    </AdminDashboardShell>
  );
}
