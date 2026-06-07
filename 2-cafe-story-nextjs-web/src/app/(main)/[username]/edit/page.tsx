import { PageShell } from "@/components/layout/page-shell";
import { EditProfileForm } from "@/components/profile/edit-profile-form";

export default async function EditProfilePage({
  params,
}: {
  params: Promise<{ username: string }>;
}) {
  const { username } = await params;

  return (
    <PageShell>
      <EditProfileForm routeUsername={username} />
    </PageShell>
  );
}
