import Link from "next/link";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";

export type CafePageHighlight = {
  alt: string;
  href: string;
  image?: string | null;
  label: string;
};

type CafePageHighlightsProps = {
  cafes: CafePageHighlight[];
};

export function CafePageHighlights({ cafes }: CafePageHighlightsProps) {
  if (cafes.length === 0) {
    return null;
  }

  return (
    <section className="flex justify-start overflow-x-auto">
      <div className="flex max-w-full gap-7">
        {cafes.map((cafe) => (
          <Link
            className="grid w-20 shrink-0 justify-items-center gap-2 text-xs font-black"
            href={cafe.href}
            key={cafe.href}
          >
            <span className="size-16 overflow-hidden rounded-full border border-border bg-surface p-1 sm:size-20">
              <span className="block size-full overflow-hidden rounded-full bg-surface-muted">
                <img
                  alt={cafe.alt}
                  className="size-full max-w-none rounded-full object-cover"
                  decoding="async"
                  height="72"
                  loading="lazy"
                  onError={(event) => {
                    event.currentTarget.src = DEFAULT_AVATAR_IMAGE;
                  }}
                  src={cafe.image?.trim() || DEFAULT_AVATAR_IMAGE}
                  width="72"
                />
              </span>
            </span>
            <span className="block w-20 truncate text-center">{cafe.label}</span>
          </Link>
        ))}
      </div>
    </section>
  );
}

type LegacyProfileStory = {
  alt: string;
  image: string;
  label: string;
};

export function ProfileStoryHighlights({
  stories,
}: {
  stories: LegacyProfileStory[];
}) {
  return (
    <CafePageHighlights
      cafes={stories.map((story) => ({
        alt: story.alt,
        href: "#",
        image: story.image,
        label: story.label,
      }))}
    />
  );
}
