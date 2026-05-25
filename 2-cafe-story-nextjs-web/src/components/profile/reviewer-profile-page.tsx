import { ProfileReviewGrid } from "@/components/profile/profile-review-grid";
import { ProfileStoryHighlights } from "@/components/profile/profile-story-highlights";
import type { ProfileReview } from "@/types/review";
import type { ReviewerProfile } from "@/types/user";

type ReviewerProfilePageProps = {
  posts: ProfileReview[];
  reviewer: ReviewerProfile;
};

const statLabels: Record<keyof ReviewerProfile["stats"], string> = {
  reviews: "reviews",
  followers: "followers",
  following: "following",
};

export function ReviewerProfilePage({
  posts,
  reviewer,
}: ReviewerProfilePageProps) {
  return (
    <div className="space-y-8">
      <section className="w-full overflow-hidden border-b border-border pb-8">
        <div className="flex items-start gap-7 sm:gap-12">
          <div className="h-24 w-24 shrink-0 overflow-hidden rounded-full bg-gradient-to-tr from-rating via-accent to-primary p-[3px] sm:h-36 sm:w-36">
            <div className="h-full w-full overflow-hidden rounded-full border-4 border-background bg-surface-muted">
              <img
                alt={`${reviewer.displayName} avatar`}
                className="aspect-square h-full w-full max-w-none rounded-full object-cover"
                decoding="async"
                height="136"
                src={reviewer.avatarImage}
                width="136"
              />
            </div>
          </div>

          <div className="min-w-0 flex-1 space-y-5">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="truncate text-xl font-normal text-foreground">
                {reviewer.username}
              </h1>
            </div>

            <div className="flex flex-wrap gap-x-8 gap-y-2 text-sm">
              {Object.entries(reviewer.stats).map(([label, value]) => (
                <p key={label}>
                  <span className="font-black">{value}</span>{" "}
                  <span className="text-muted">
                    {statLabels[label as keyof ReviewerProfile["stats"]]}
                  </span>
                </p>
              ))}
            </div>

            <div className="min-w-0 space-y-1 text-sm leading-6">
              <p className="font-black">{reviewer.displayName}</p>
              <p className="font-semibold text-muted">{reviewer.title}</p>
              <p className="max-w-full break-words">{reviewer.bio}</p>
              <p className="text-muted">{reviewer.location}</p>
            </div>

            <div className="flex flex-wrap gap-3 pt-1">
              <button
                className="h-9 rounded-md bg-primary px-6 text-sm font-black text-white transition hover:bg-primary-strong"
                type="button"
              >
                Follow
              </button>
              <button
                className="h-9 rounded-md bg-surface-muted px-5 text-sm font-black transition hover:bg-border"
                type="button"
              >
                Message
              </button>
            </div>
          </div>
        </div>

        <div className="mt-8 flex flex-wrap gap-3">
          <span className="rounded-full bg-surface-muted px-4 py-2 text-xs font-black uppercase tracking-[0.08em] text-foreground">
            {reviewer.badge}
          </span>
          <span className="rounded-full border border-border px-4 py-2 text-xs font-black uppercase tracking-[0.08em] text-muted">
            Cafe reviewer
          </span>
        </div>
      </section>

      <ProfileStoryHighlights stories={reviewer.visualDiary} />
      <ProfileReviewGrid reviews={posts} />
    </div>
  );
}
