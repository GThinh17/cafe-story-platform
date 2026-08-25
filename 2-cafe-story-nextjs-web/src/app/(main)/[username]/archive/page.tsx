import { ProfileArchive } from "@/components/profile/profile-archive";

export default async function ArchivePage({
  params,
}: {
  params: Promise<{ username: string }>;
}) {
  const { username } = await params;

  return <ProfileArchive routeUsername={username} />;
}
