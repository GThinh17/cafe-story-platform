import { ReviewerDashboardOverview } from "@/components/reviewer-dashboard/reviewer-dashboard-overview";
import { ReviewerDashboardShell } from "@/components/reviewer-dashboard/reviewer-dashboard-shell";

export function ReviewerDashboardPage() {
  return (
    <ReviewerDashboardShell>
      <ReviewerDashboardOverview />
    </ReviewerDashboardShell>
  );
}
