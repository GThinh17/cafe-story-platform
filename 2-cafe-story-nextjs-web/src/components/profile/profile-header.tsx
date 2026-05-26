import type { ProfileHighlight, UserProfile } from "@/types/user";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { MoreHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";

type ProfileHeaderProps = {
  highlights: ProfileHighlight[];
  profile: UserProfile;
};

const statLabels: Record<keyof UserProfile["stats"], string> = {
  posts: "posts",
  cafes: "cafes",
  followers: "followers",
};

export function ProfileHeader({ highlights, profile }: ProfileHeaderProps) {
  return (
    <section className="w-full overflow-hidden border-border pb-6">
      <div className="flex items-start gap-7 sm:gap-12">
        <div className="size-24 shrink-0 overflow-hidden rounded-full bg-gradient-to-tr from-rating via-accent to-primary p-[3px] sm:size-36">
          <Avatar className="size-full border-4 border-background">
            <AvatarImage
              alt={`${profile.displayName} avatar`}
              src={profile.avatarImage}
            />
            <AvatarFallback>{profile.displayName.slice(0, 1)}</AvatarFallback>
          </Avatar>
        </div>

        <div className="min-w-0 flex-1 space-y-5">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="truncate text-xl font-normal text-foreground">
              {profile.username}
            </h1>
            
            <Button
              aria-label="Profile settings"
              className="bg-transparent text-lg font-black hover:cursor-pointer hover:bg-transparent focus:ring-0"
              size="icon-sm"
              type="button"
              variant="ghost"
            >
              <MoreHorizontal />
            </Button>
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

      <div className="mt-6 grid grid-cols-4 justify-items-center gap-4 sm:flex sm:justify-start sm:gap-8">
        {highlights.map((highlight) => (
          <button
            className="grid w-20 justify-items-center gap-2 text-xs font-black"
            key={highlight.id}
            type="button"
          >
            <span className="size-16 overflow-hidden rounded-full border border-border bg-surface p-1 sm:size-20">
              <Avatar className="size-full">
                <AvatarImage
                  alt=""
                  src={highlight.image}
                />
                <AvatarFallback>{highlight.label.slice(0, 1)}</AvatarFallback>
              </Avatar>
            </span>
            <span className="block w-20 truncate text-center">{highlight.label}</span>
          </button>
        ))}
      </div>
    </section>
  );
}
