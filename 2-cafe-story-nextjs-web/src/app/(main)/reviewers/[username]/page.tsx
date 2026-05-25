import { PageShell } from "@/components/layout/page-shell";
import { ReviewerProfilePage } from "@/components/profile/reviewer-profile-page";
import { mockReviewerProfileReviews } from "@/mocks/reviews";
import { mockReviewerProfiles } from "@/mocks/users";

export function generateStaticParams() {
  return mockReviewerProfiles.map((reviewer) => ({
    username: reviewer.username,
  }));
}

export default async function ReviewerProfileRoute({
  params,
}: PageProps<"/reviewers/[username]">) {
  const { username } = await params;
  const reviewer =
    mockReviewerProfiles.find((item) => item.username === username) ??
    mockReviewerProfiles[0];

  return (
    <PageShell>
      <ReviewerProfilePage
        posts={mockReviewerProfileReviews}
        reviewer={reviewer}
      />
    </PageShell>
  );
}
