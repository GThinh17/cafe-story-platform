import { ScrollView, StyleSheet } from "react-native";
import { Pressable, Text } from "react-native";

import { colors, spacing, typography } from "../../theme";
import { t, type TranslationKey } from "../../features/i18n";

export type ExploreTab = "all" | "cafes" | "reviewers" | "trending";

type ExploreTabsProps = {
  activeTab: ExploreTab;
  onChange: (tab: ExploreTab) => void;
};

const tabs: Array<{ labelKey: TranslationKey; value: ExploreTab }> = [
  { labelKey: "explore.tab.all", value: "all" },
  { labelKey: "explore.tab.cafes", value: "cafes" },
  { labelKey: "explore.tab.reviewers", value: "reviewers" },
  { labelKey: "explore.tab.trending", value: "trending" },
];

export function ExploreTabs({ activeTab, onChange }: ExploreTabsProps) {
  return (
    <ScrollView
      contentContainerStyle={styles.container}
      horizontal
      showsHorizontalScrollIndicator={false}
    >
      {tabs.map((tab) => {
        const isActive = activeTab === tab.value;
        const label = t(tab.labelKey);

        return (
          <Pressable
            accessibilityLabel={label}
            accessibilityRole="button"
            key={tab.value}
            onPress={() => onChange(tab.value)}
            style={({ pressed }) => [
              styles.tab,
              isActive && styles.tabActive,
              pressed && styles.pressed,
            ]}
          >
            <Text style={[styles.tabText, isActive && styles.tabTextActive]}>
              {label}
            </Text>
          </Pressable>
        );
      })}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  pressed: {
    opacity: 0.72,
  },
  tab: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 20,
    borderWidth: 1,
    minHeight: 38,
    justifyContent: "center",
    paddingHorizontal: spacing.lg,
  },
  tabActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  tabText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  tabTextActive: {
    color: colors.white,
  },
});
