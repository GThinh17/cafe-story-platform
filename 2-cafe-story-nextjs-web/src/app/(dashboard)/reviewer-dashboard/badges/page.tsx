import { ReviewerBadgesDetail } from "@/features/reviewer-dashboard/components/reviewer-badges-detail";
import { ReviewerDashboardShell } from "@/features/reviewer-dashboard/components/reviewer-dashboard-shell";

export default function ReviewerBadgesRoute() {
  return (
    <ReviewerDashboardShell>
      <ReviewerBadgesDetail />
    </ReviewerDashboardShell>
  );
}
