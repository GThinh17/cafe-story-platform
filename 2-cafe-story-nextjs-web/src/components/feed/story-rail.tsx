import type { StoryItem } from "@/types/feed";

type StoryRailProps = {
  stories: StoryItem[];
};

export function StoryRail({ stories }: StoryRailProps) {
  return (
    <div className="w-full overflow-x-clip">
      <div className="grid grid-cols-[repeat(auto-fit,64px)] gap-x-4 gap-y-4">
        {stories.map((story) => (
          <button
            className="grid w-16 justify-items-center text-center"
            key={story.name}
            type="button"
          >
            <span className="grid h-[64px] w-[64px] place-items-center rounded-full bg-[linear-gradient(135deg,var(--primary),var(--accent))] p-[4px]">
              <img
                alt={`${story.name} cafe story`}
                className="h-[56px] w-[56px] rounded-full object-cover"
                decoding="async"
                height="56"
                loading="lazy"
                src={story.image}
                width="56"
              />
            </span>
            <span className="mt-2 block w-16 truncate text-xs font-semibold leading-4 text-foreground">
              {story.name}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}
