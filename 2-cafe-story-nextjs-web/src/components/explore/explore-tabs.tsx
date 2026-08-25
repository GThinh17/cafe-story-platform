"use client";

import { useI18n } from "@/components/providers/locale-provider";
import type { TranslationKey } from "@/lib/i18n";

export type ExploreTab = "cafes" | "reviewers" | "trending";

type ExploreTabsProps = {
  activeTab: ExploreTab;
  onChange: (tab: ExploreTab) => void;
};

const tabs: Array<{ labelKey: TranslationKey; value: ExploreTab }> = [
  { labelKey: "explore.tab.cafes", value: "cafes" },
  { labelKey: "explore.tab.reviewers", value: "reviewers" },
  { labelKey: "explore.tab.trending", value: "trending" },
];

export function ExploreTabs({ activeTab, onChange }: ExploreTabsProps) {
  const { t } = useI18n();

  return (
    <div
      aria-label={t("explore.tabsLabel")}
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
            {t(tab.labelKey)}
          </button>
        );
      })}
    </div>
  );
}
