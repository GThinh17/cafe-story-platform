import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type { ReviewerProfile } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerProfileSummaryProps = {
  profile: ReviewerProfile;
};

const numberFormatter = new Intl.NumberFormat("en", {
  notation: "compact",
  maximumFractionDigits: 1,
});

export function ReviewerProfileSummary({
  profile,
}: ReviewerProfileSummaryProps) {
  const location = [
    profile.region.street,
    profile.region.ward,
    profile.region.area,
    profile.region.city,
  ]
    .filter(Boolean)
    .join(", ");

  return (
    <Card className="overflow-hidden">
      <div className="flex flex-col gap-5 p-5 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex min-w-0 items-center gap-4">
          <img
            alt={`${profile.name} avatar`}
            className="size-20 shrink-0 rounded-full border-4 border-surface-muted object-cover"
            decoding="async"
            src={profile.avatar}
          />
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="truncate text-xl font-black text-espresso">
                {profile.name}
              </h2>
              <Badge variant="rating">{profile.badge}</Badge>
            </div>
            <p className="mt-1 text-sm font-semibold text-muted">
              {profile.role} - {location}
            </p>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-3 text-center sm:min-w-[300px]">
          <div className="rounded-md bg-surface-muted px-3 py-3">
            <p className="text-lg font-black text-espresso">
              {numberFormatter.format(profile.follower)}
            </p>
            <p className="text-xs font-semibold text-muted">Followers</p>
          </div>
          <div className="rounded-md bg-surface-muted px-3 py-3">
            <p className="text-lg font-black text-espresso">
              {numberFormatter.format(profile.follow)}
            </p>
            <p className="text-xs font-semibold text-muted">Following</p>
          </div>
          <div className="rounded-md bg-surface-muted px-3 py-3">
            <p className="text-lg font-black text-espresso">
              {numberFormatter.format(profile.like)}
            </p>
            <p className="text-xs font-semibold text-muted">Likes</p>
          </div>
        </div>
      </div>

      <div className="border-t border-line-soft px-5 py-4">
        <p className="text-sm font-semibold text-muted">Total score</p>
        <p className="mt-1 text-3xl font-black text-primary">
          {numberFormatter.format(profile.score)}
        </p>
      </div>
    </Card>
  );
}
