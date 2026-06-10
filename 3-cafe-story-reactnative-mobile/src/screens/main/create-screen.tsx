import { EmptyState, Screen } from "../../components";

export function CreateScreen() {
  return (
    <Screen>
      <EmptyState
        description="Review composer and media upload flow will start from this tab."
        title="Create review"
      />
    </Screen>
  );
}
