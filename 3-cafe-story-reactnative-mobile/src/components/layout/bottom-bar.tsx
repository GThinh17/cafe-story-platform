import type { BottomTabBarProps } from "@react-navigation/bottom-tabs";
import { Bell, House, Search, SquarePlus, User } from "lucide-react-native";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { routes } from "../../navigation/routes";
import type { MainTabParamList } from "../../navigation/types";
import { colors, spacing, typography } from "../../theme";

type TabName = keyof MainTabParamList;
type TabIcon = typeof House;

const tabItems: Record<TabName, { Icon: TabIcon; label: string }> = {
  [routes.home]: {
    Icon: House,
    label: "Home",
  },
  [routes.explore]: {
    Icon: Search,
    label: "Explore",
  },
  [routes.create]: {
    Icon: SquarePlus,
    label: "Post",
  },
  [routes.notifications]: {
    Icon: Bell,
    label: "Alerts",
  },
  [routes.profile]: {
    Icon: User,
    label: "Profile",
  },
};

function isTabName(name: string): name is TabName {
  return name in tabItems;
}

export function BottomBar({
  descriptors,
  navigation,
  state,
}: BottomTabBarProps) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.shell, { paddingBottom: Math.max(insets.bottom, 10) }]}>
      <View style={styles.container}>
        {state.routes.map((route, index) => {
          if (!isTabName(route.name)) {
            return null;
          }

          const { Icon, label } = tabItems[route.name];
          const isFocused = state.index === index;
          const options = descriptors[route.key]?.options;

          const onPress = () => {
            const event = navigation.emit({
              canPreventDefault: true,
              target: route.key,
              type: "tabPress",
            });

            if (!isFocused && !event.defaultPrevented) {
              navigation.navigate(route.name);
            }
          };

          const onLongPress = () => {
            navigation.emit({
              target: route.key,
              type: "tabLongPress",
            });
          };

          return (
            <Pressable
              accessibilityLabel={options?.tabBarAccessibilityLabel}
              accessibilityRole="button"
              accessibilityState={isFocused ? { selected: true } : {}}
              key={route.key}
              onLongPress={onLongPress}
              onPress={onPress}
              style={[styles.item, isFocused && styles.itemActive]}
              testID={options?.tabBarButtonTestID}
            >
              <Icon
                color={isFocused ? colors.white : colors.foreground}
                size={22}
                strokeWidth={isFocused ? 2.7 : 2.1}
              />
              {isFocused ? <Text style={styles.label}>{label}</Text> : null}
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    alignSelf: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 28,
    borderWidth: 1,
    elevation: 8,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 58,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    shadowColor: colors.primary,
    shadowOffset: {
      height: 6,
      width: 0,
    },
    shadowOpacity: 0.12,
    shadowRadius: 16,
  },
  item: {
    alignItems: "center",
    borderRadius: 24,
    flexDirection: "row",
    height: 46,
    justifyContent: "center",
    minWidth: 46,
    paddingHorizontal: spacing.md,
  },
  itemActive: {
    backgroundColor: colors.primary,
    gap: spacing.xs,
    minWidth: 94,
  },
  label: {
    color: colors.white,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  shell: {
    backgroundColor: "transparent",
    bottom: 0,
    left: 0,
    paddingHorizontal: spacing.xl,
    paddingTop: spacing.sm,
    position: "absolute",
    right: 0,
  },
});
