import { ReviewerDashboardOverview } from "@/features/reviewer-dashboard/components/reviewer-dashboard-overview";
import { ReviewerDashboardShell } from "@/features/reviewer-dashboard/components/reviewer-dashboard-shell";

export function ReviewerDashboardPage() {
  return (
    <ReviewerDashboardShell>
      <ReviewerDashboardOverview />
    </ReviewerDashboardShell>
  );
}
