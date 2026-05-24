import { ActivityList } from "@/components/notification/activity-list";
import { mockActivityNotifications } from "@/mocks/users";

export default function NotificationsPage() {
  return <ActivityList items={mockActivityNotifications} />;
}
