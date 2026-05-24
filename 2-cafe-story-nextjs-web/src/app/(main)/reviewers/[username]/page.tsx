import { ReviewerProfilePage } from "@/components/profile/reviewer-profile-page";
import { mockReviewerRecentReviews } from "@/mocks/reviews";
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
    <main className="-ml-8 min-h-screen w-[calc(100vw-64px)] max-w-none overflow-x-clip bg-background sm:-ml-14 sm:w-[calc(100vw-72px)] xl:-ml-[248px]">
      <ReviewerProfilePage
        recentReviews={mockReviewerRecentReviews}
        reviewer={reviewer}
      />
    </main>
  );
}
