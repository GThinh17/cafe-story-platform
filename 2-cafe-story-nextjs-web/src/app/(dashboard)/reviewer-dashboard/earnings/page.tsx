import { ReviewerDashboardShell } from "@/features/reviewer-dashboard/components/reviewer-dashboard-shell";
import { ReviewerEarningsDetail } from "@/features/reviewer-dashboard/components/reviewer-earnings-detail";

export default function ReviewerEarningsRoute() {
  return (
    <ReviewerDashboardShell>
      <ReviewerEarningsDetail />
    </ReviewerDashboardShell>
  );
}
