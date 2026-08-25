import { ReviewerBadgesDetail } from "@/components/reviewer-dashboard/reviewer-badges-detail";
import { ReviewerDashboardShell } from "@/components/reviewer-dashboard/reviewer-dashboard-shell";

export default function ReviewerBadgesRoute() {
  return (
    <ReviewerDashboardShell>
      <ReviewerBadgesDetail />
    </ReviewerDashboardShell>
  );
}
