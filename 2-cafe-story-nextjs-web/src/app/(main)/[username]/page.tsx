import { PageShell } from "@/components/layout/page-shell";
import { ProfilePageContent } from "@/components/profile/profile-page-content";

export default async function UserProfilePage({
  params,
}: {
  params: Promise<{ username: string }>;
}) {
  const { username } = await params;
  return (
    <PageShell>
      <ProfilePageContent username={username} />
    </PageShell>
  );
}
