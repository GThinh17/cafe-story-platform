import { Grid3X3, Repeat2, SquarePlay, UserRound } from "lucide-react-native";
import { Pressable, StyleSheet, View } from "react-native";
import { colors, spacing } from "../../theme";

export type ProfileContentTab = "posts" | "videos" | "reposts" | "tagged";

const tabs: Array<{
  accessibilityLabel: string;
  icon: typeof Grid3X3;
  value: ProfileContentTab;
}> = [
  {
    accessibilityLabel: "Show profile posts",
    icon: Grid3X3,
    value: "posts",
  },
  {
    accessibilityLabel: "Show profile videos",
    icon: SquarePlay,
    value: "videos",
  },
  {
    accessibilityLabel: "Show reposted stories",
    icon: Repeat2,
    value: "reposts",
  },
  {
    accessibilityLabel: "Show tagged posts",
    icon: UserRound,
    value: "tagged",
  },
];

type ProfileContentTabsProps = {
  activeTab: ProfileContentTab;
  onChange: (tab: ProfileContentTab) => void;
};

export function ProfileContentTabs({ activeTab, onChange }: ProfileContentTabsProps) {
  return (
    <View style={styles.container}>
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const isActive = activeTab === tab.value;

        return (
          <Pressable
            accessibilityLabel={tab.accessibilityLabel}
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
