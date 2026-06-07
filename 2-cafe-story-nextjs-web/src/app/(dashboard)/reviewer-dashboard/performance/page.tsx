import { ReviewerDashboardShell } from "@/components/reviewer-dashboard/reviewer-dashboard-shell";
import { ReviewerPerformanceDetail } from "@/components/reviewer-dashboard/reviewer-performance-detail";

export default function ReviewerPerformanceRoute() {
  return (
    <ReviewerDashboardShell>
      <ReviewerPerformanceDetail />
    </ReviewerDashboardShell>
  );
}
