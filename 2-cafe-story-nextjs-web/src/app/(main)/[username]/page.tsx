import { CafeSuggestionList } from "@/components/cafe/cafe-suggestion-list";
import { PageShell } from "@/components/layout/page-shell";
import { ProfilePageContent } from "@/components/profile/profile-page-content";
import { mockCafeSummaries } from "@/mocks/cafes";
export default async function UserProfilePage({
  params,
}: {
  params: Promise<{ username: string }>;
}) {
  const { username } = await params;
  return (
    <PageShell aside={<CafeSuggestionList cafes={mockCafeSummaries} />}>
      <ProfilePageContent username={username} />
    </PageShell>
  );
}
