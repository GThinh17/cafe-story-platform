import { ReviewerDashboardShell } from "@/components/reviewer-dashboard/reviewer-dashboard-shell";
import { ReviewerEarningsDetail } from "@/components/reviewer-dashboard/reviewer-earnings-detail";

export default function ReviewerEarningsRoute() {
  return (
    <ReviewerDashboardShell>
      <ReviewerEarningsDetail />
    </ReviewerDashboardShell>
  );
}
