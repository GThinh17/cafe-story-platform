import { SettingsShell } from "@/components/settings/settings-shell";
import { EditProfileForm } from "@/components/profile/edit-profile-form";

export default async function EditProfilePage({
  params,
}: {
  params: Promise<{ username: string }>;
}) {
  const { username } = await params;

  return (
    <SettingsShell>
      <EditProfileForm routeUsername={username} />
    </SettingsShell>
  );
}
