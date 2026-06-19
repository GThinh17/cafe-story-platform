import { Button } from "@/components/ui/button";
import { ExploreIcon } from "@/components/cafe/explore-icon";
import { cn } from "@/lib/utils";
import type { CafeCategory } from "@/types/cafe";

type CafeCategoryListProps = {
  categories: CafeCategory[];
  activeCategoryId?: string;
  onSelect?: (id: string | undefined) => void;
};

const chipClass = (isActive: boolean) =>
  cn(
    "inline-flex h-10 shrink-0 items-center gap-2 rounded-full border px-5 text-xs font-medium transition",
    isActive
      ? "border-espresso bg-espresso text-white hover:bg-espresso/90"
      : "border-line-soft bg-surface text-coffee-muted hover:border-primary hover:text-primary",
  );

export function CafeCategoryList({
  categories,
  activeCategoryId,
  onSelect,
}: CafeCategoryListProps) {
  const allActive = activeCategoryId === undefined;

  return (
    <div className="flex gap-2 overflow-x-auto pb-1 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
      <Button
        className={chipClass(allActive)}
        onClick={() => onSelect?.(undefined)}
        type="button"
        variant={allActive ? "default" : "outline"}
      >
        Tất cả
      </Button>
      {categories.map((category) => {
        const isActive = category.id === activeCategoryId;

        return (
          <Button
            className={chipClass(isActive)}
            key={category.id}
            onClick={() => onSelect?.(category.id)}
            type="button"
            variant={isActive ? "default" : "outline"}
          >
            <ExploreIcon name={category.icon} />
            {category.label}
          </Button>
        );
      })}
    </div>
  );
}
