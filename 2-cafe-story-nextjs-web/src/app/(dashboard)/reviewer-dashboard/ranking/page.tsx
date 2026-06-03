import { ReviewerDashboardShell } from "@/features/reviewer-dashboard/components/reviewer-dashboard-shell";
import { ReviewerRankingDetail } from "@/features/reviewer-dashboard/components/reviewer-ranking-detail";

export default function ReviewerRankingRoute() {
  return (
    <ReviewerDashboardShell>
      <ReviewerRankingDetail />
    </ReviewerDashboardShell>
  );
}
