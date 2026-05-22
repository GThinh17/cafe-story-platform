import { CafeSuggestionList } from "@/components/cafe/cafe-suggestion-list";
import { PageShell } from "@/components/layout/page-shell";
import { ProfileHeader } from "@/components/profile/profile-header";
import { ProfileReviewGrid } from "@/components/profile/profile-review-grid";
import { mockCafeSummaries } from "@/mocks/cafes";
import { mockProfileReviews } from "@/mocks/reviews";
import { mockProfileHighlights, mockUserProfile } from "@/mocks/users";

export default function ProfilePage() {
  return (
    <PageShell
      aside={<CafeSuggestionList cafes={mockCafeSummaries} />}
    >
      <ProfileHeader
        highlights={mockProfileHighlights}
        profile={mockUserProfile}
      />
      <ProfileReviewGrid reviews={mockProfileReviews} />
    </PageShell>
  );
}
