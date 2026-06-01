import { Button } from "@/components/ui/button";
import { ExploreIcon } from "@/components/cafe/explore-icon";
import { cn } from "@/lib/utils";
import type { CafeCategory } from "@/types/cafe";

type CafeCategoryListProps = {
  categories: CafeCategory[];
  activeCategoryId?: string;
};

export function CafeCategoryList({
  categories,
  activeCategoryId = categories[0]?.id,
}: CafeCategoryListProps) {
  return (
    <div className="flex gap-2 overflow-x-auto pb-1 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
      {categories.map((category) => {
        const isActive = category.id === activeCategoryId;

        return (
          <Button
            className={cn(
              "inline-flex h-10 shrink-0 items-center gap-2 rounded-full border px-5 text-xs font-black uppercase tracking-[0.14em] transition",
              isActive
                ? "border-espresso bg-espresso text-white hover:bg-espresso/90"
                : "border-line-soft bg-surface text-coffee-muted hover:border-primary hover:text-primary",
            )}
            key={category.id}
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
