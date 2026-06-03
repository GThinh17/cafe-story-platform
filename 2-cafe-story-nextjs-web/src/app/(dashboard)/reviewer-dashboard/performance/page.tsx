import { ReviewerDashboardShell } from "@/features/reviewer-dashboard/components/reviewer-dashboard-shell";
import { ReviewerPerformanceDetail } from "@/features/reviewer-dashboard/components/reviewer-performance-detail";

export default function ReviewerPerformanceRoute() {
  return (
    <ReviewerDashboardShell>
      <ReviewerPerformanceDetail />
    </ReviewerDashboardShell>
  );
}
