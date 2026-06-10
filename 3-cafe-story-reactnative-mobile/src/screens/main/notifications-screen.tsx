import { EmptyState, Screen } from "../../components";

export function NotificationsScreen() {
  return (
    <Screen>
      <EmptyState
        description="Likes, follows, comments, and cafe updates will appear here."
        title="Notifications"
      />
    </Screen>
  );
}
