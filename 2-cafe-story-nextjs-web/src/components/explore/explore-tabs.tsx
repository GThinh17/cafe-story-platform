"use client";

export type ExploreTab = "cafes" | "reviewers" | "trending";

type ExploreTabsProps = {
  activeTab: ExploreTab;
  onChange: (tab: ExploreTab) => void;
};

const tabs: Array<{ label: string; value: ExploreTab }> = [
  { label: "Cafes", value: "cafes" },
  { label: "Reviewers", value: "reviewers" },
  { label: "Trending", value: "trending" },
];

export function ExploreTabs({ activeTab, onChange }: ExploreTabsProps) {
  return (
    <div
      aria-label="Explore categories"
      className="flex gap-2 overflow-x-auto pb-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      role="tablist"
    >
      {tabs.map((tab) => {
        const isActive = activeTab === tab.value;

        return (
          <button
            aria-selected={isActive}
            className={[
              "shrink-0 rounded-full border px-6 py-2 text-xs font-black transition-colors",
              isActive
                ? "border-primary bg-primary text-surface"
                : "border-border bg-surface text-foreground hover:border-primary/50 hover:text-primary",
            ].join(" ")}
            key={tab.value}
            onClick={() => onChange(tab.value)}
            role="tab"
            type="button"
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
