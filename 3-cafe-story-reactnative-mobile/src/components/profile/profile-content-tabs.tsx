import { Bookmark, Grid3X3, Repeat2, UserRound } from "lucide-react-native";
import { Pressable } from "react-native";
import { StyleSheet, View } from "react-native";
import { colors, spacing } from "../../theme";
import type { ProfileContentTab } from "../../types";
import { t, type TranslationKey } from "../../features/i18n";

const tabs: Array<{
  accessibilityLabelKey: TranslationKey;
  icon: typeof Grid3X3;
  value: ProfileContentTab;
}> = [
  {
    accessibilityLabelKey: "profile.tab.posts",
    icon: Grid3X3,
    value: "posts",
  },
  {
    accessibilityLabelKey: "profile.tab.saved",
    icon: Bookmark,
    value: "saved",
  },
  {
    accessibilityLabelKey: "profile.tab.shared",
    icon: Repeat2,
    value: "shared",
  },
  {
    accessibilityLabelKey: "profile.tab.tagged",
    icon: UserRound,
    value: "tagged",
  },
];

type ProfileContentTabsProps = {
  activeTab: ProfileContentTab;
  onChange: (tab: ProfileContentTab) => void;
  visibleTabs?: ProfileContentTab[];
};

export function ProfileContentTabs({
  activeTab,
  onChange,
  visibleTabs,
}: ProfileContentTabsProps) {
  const renderedTabs = visibleTabs
    ? tabs.filter((tab) => visibleTabs.includes(tab.value))
    : tabs;

  return (
    <View style={styles.container}>
      {renderedTabs.map((tab) => {
        const Icon = tab.icon;
        const isActive = activeTab === tab.value;

        return (
          <Pressable
            accessibilityLabel={t(tab.accessibilityLabelKey)}
            accessibilityRole="tab"
            accessibilityState={{ selected: isActive }}
            key={tab.value}
            onPress={() => onChange(tab.value)}
            style={({ pressed }) => [
              styles.tab,
              isActive && styles.tabActive,
              pressed && styles.tabPressed,
            ]}
          >
            <Icon
              color={isActive ? colors.foreground : colors.muted}
              size={tab.value === "posts" ? 23 : 24}
              strokeWidth={tab.value === "posts" ? 2.8 : 2.4}
            />
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-around",
    paddingTop: spacing.sm,
  },

  tab: {
    alignItems: "center",
    borderBottomColor: "transparent",
    borderBottomWidth: 2,
    flex: 1,
    height: 48,
    justifyContent: "center",
  },

  tabActive: {
    borderBottomColor: colors.foreground,
  },

  tabPressed: {
    opacity: 0.72,
  },
});
