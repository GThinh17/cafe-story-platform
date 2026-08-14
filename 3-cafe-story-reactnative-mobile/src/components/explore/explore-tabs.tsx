import { ScrollView, StyleSheet } from "react-native";
import { Pressable, Text } from "../../features/i18n/localized-native";

import { colors, spacing, typography } from "../../theme";

export type ExploreTab = "all" | "cafes" | "reviewers" | "trending";

type ExploreTabsProps = {
  activeTab: ExploreTab;
  onChange: (tab: ExploreTab) => void;
};

const tabs: Array<{ label: string; value: ExploreTab }> = [
  { label: "All", value: "all" },
  { label: "Cafes", value: "cafes" },
  { label: "Reviewers", value: "reviewers" },
  { label: "Trending", value: "trending" },
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

        return (
          <Pressable
            accessibilityLabel={`Show ${tab.label}`}
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
              {tab.label}
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
