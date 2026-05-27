import { CafeSuggestionList } from "@/components/cafe/cafe-suggestion-list";
import { PageShell } from "@/components/layout/page-shell";
import { ProfilePageContent } from "@/components/profile/profile-page-content";
import { mockCafeSummaries } from "@/mocks/cafes";

export default function ProfilePage() {
  return (
    <PageShell
      aside={<CafeSuggestionList cafes={mockCafeSummaries} />}
    >
      <ProfilePageContent />
    </PageShell>
  );
}
