import { AvatarImage } from "@/components/ui/avatar-image";
import type { ProfileHighlight, UserProfile } from "@/types/user";

type ProfileHeaderProps = {
  highlights: ProfileHighlight[];
  isLoading?: boolean;
  profile: UserProfile;
};

const statLabels: Record<keyof UserProfile["stats"], string> = {
  posts: "posts",
  cafes: "cafes",
  followers: "followers",
};

export function ProfileHeader({
  highlights,
  isLoading = false,
  profile,
}: ProfileHeaderProps) {
  return (
    <section className="w-full overflow-hidden border-b border-border pb-8">
      <div className="flex items-start gap-7 sm:gap-12">
        <div className="h-24 w-24 shrink-0 overflow-hidden rounded-full bg-gradient-to-tr from-rating via-accent to-primary p-[3px] sm:h-36 sm:w-36">
          <div className="h-full w-full overflow-hidden rounded-full border-4 border-background bg-surface-muted">
            <AvatarImage
              alt={`${profile.displayName} avatar`}
              className="aspect-square h-full w-full max-w-none rounded-full object-cover"
              height="136"
              src={profile.avatarImage}
              width="136"
            />
          </div>
        </div>

        <div className="min-w-0 flex-1 space-y-5">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="truncate text-xl font-normal text-foreground">
              {isLoading ? "Loading..." : profile.username}
            </h1>
            <button
              className="h-9 rounded-md bg-surface-muted px-4 text-sm font-black transition hover:bg-border"
              type="button"
            >
              Edit profile
            </button>
            <button
              aria-label="Profile settings"
              className="grid h-9 w-9 place-items-center rounded-md bg-surface-muted text-lg font-black transition hover:bg-border"
              type="button"
            >
              ...
            </button>
          </div>

          <div className="flex flex-wrap gap-x-8 gap-y-2 text-sm">
            {Object.entries(profile.stats).map(([label, value]) => (
              <p key={label}>
                <span className="font-black">{value}</span>{" "}
                <span className="text-muted">
                  {statLabels[label as keyof UserProfile["stats"]]}
                </span>
              </p>
            ))}
          </div>

          <div className="min-w-0 space-y-1 text-sm leading-6">
            <p className="font-black">{profile.displayName}</p>
            {profile.email ? (
              <p className="text-muted">{profile.email}</p>
            ) : null}
            <p className="max-w-full break-words">{profile.bio}</p>
            <p className="text-muted">{profile.location}</p>
            <a
              className="block max-w-full truncate font-black text-primary no-underline"
              href="#"
            >
              {profile.website}
            </a>
          </div>
        </div>
      </div>

      <div className="mt-8 grid grid-cols-4 justify-items-center gap-4 sm:flex sm:justify-start sm:gap-8">
        {highlights.map((highlight) => (
          <button
            className="grid w-20 justify-items-center gap-2 text-xs font-black"
            key={highlight.id}
            type="button"
          >
            <span className="h-16 w-16 overflow-hidden rounded-full border border-border bg-surface p-1 sm:h-20 sm:w-20">
              <span className="block h-full w-full overflow-hidden rounded-full bg-surface-muted">
                <AvatarImage
                  alt=""
                  className="aspect-square h-full w-full max-w-none rounded-full object-cover"
                  height="72"
                  loading="lazy"
                  src={highlight.image}
                  width="72"
                />
              </span>
            </span>
            <span className="block w-20 truncate text-center">{highlight.label}</span>
          </button>
        ))}
      </div>
    </section>
  );
}
