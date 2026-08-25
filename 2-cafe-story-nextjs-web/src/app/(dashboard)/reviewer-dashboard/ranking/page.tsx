import { ReviewerDashboardShell } from "@/components/reviewer-dashboard/reviewer-dashboard-shell";
import { ReviewerRankingDetail } from "@/components/reviewer-dashboard/reviewer-ranking-detail";

export default function ReviewerRankingRoute() {
  return (
    <ReviewerDashboardShell>
      <ReviewerRankingDetail />
    </ReviewerDashboardShell>
  );
}
