type ProfileStory = {
  alt: string;
  image: string;
  label: string;
};

type ProfileStoryHighlightsProps = {
  stories: ProfileStory[];
};

export function ProfileStoryHighlights({ stories }: ProfileStoryHighlightsProps) {
  if (stories.length === 0) {
    return null;
  }

  return (
    <section className="flex justify-center overflow-x-auto px-1 py-1">
      <div className="flex max-w-full gap-7">
        {stories.map((story) => (
          <button
            className="grid w-20 shrink-0 justify-items-center gap-2 text-xs font-black"
            key={story.image}
            type="button"
          >
            <span className="h-16 w-16 overflow-hidden rounded-full border border-border bg-surface p-1 sm:h-20 sm:w-20">
              <span className="block h-full w-full overflow-hidden rounded-full bg-surface-muted">
                <img
                  alt={story.alt}
                  className="aspect-square h-full w-full max-w-none rounded-full object-cover"
                  decoding="async"
                  height="72"
                  loading="lazy"
                  src={story.image}
                  width="72"
                />
              </span>
            </span>
            <span className="block w-20 truncate text-center">{story.label}</span>
          </button>
        ))}
      </div>
    </section>
  );
}
