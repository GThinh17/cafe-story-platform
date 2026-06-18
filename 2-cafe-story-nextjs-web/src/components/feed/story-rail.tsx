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
    <div className="w-full overflow-x-auto pb-1 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
      <div className="flex gap-4">
        {stories.map((story) => (
          <button
            aria-label={`View ${story.name} story`}
            className="grid w-16 justify-items-center text-center"
            key={story.id}
            type="button"
          >
            <span className="grid size-16 place-items-center rounded-full ring-2 ring-primary/25 ring-offset-1 ring-offset-background">
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
