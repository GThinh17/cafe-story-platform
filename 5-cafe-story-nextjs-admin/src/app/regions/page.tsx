import { Suspense } from "react";
import { AdminDashboardShell } from "@/components/admin/admin-dashboard-shell";
import { AdminRegionsPage } from "@/components/admin/admin-regions-page";

export default function RegionsPage() {
  return (
    <AdminDashboardShell>
      <Suspense>
        <AdminRegionsPage />
      </Suspense>
    </AdminDashboardShell>
  );
}
