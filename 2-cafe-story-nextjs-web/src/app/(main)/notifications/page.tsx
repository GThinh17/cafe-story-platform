import { CafeSuggestionList } from "@/components/cafe/cafe-suggestion-list";
import { PageShell } from "@/components/layout/page-shell";
import { ActivityList } from "@/components/notification/activity-list";
import { mockCafeSummaries } from "@/mocks/cafes";
import { mockActivityNotifications } from "@/mocks/users";

export default function NotificationsPage() {
  return (
    <PageShell
      title="Notifications"
      description="Recent cafe replies, follows, saves, and review activity."
      aside={<CafeSuggestionList cafes={mockCafeSummaries} />}
    >
      <ActivityList items={mockActivityNotifications} />
    </PageShell>
  );
}
