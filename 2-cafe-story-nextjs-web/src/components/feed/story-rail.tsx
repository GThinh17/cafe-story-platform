import type { StoryItem } from "@/types/feed";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";

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
            <span className="grid size-16 place-items-center rounded-full bg-[linear-gradient(135deg,var(--primary),var(--accent))] p-1">
              <Avatar className="size-14">
                <AvatarImage
                alt={`${story.name} cafe story`}
                src={story.image}
                />
                <AvatarFallback>{story.name.slice(0, 1)}</AvatarFallback>
              </Avatar>
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
