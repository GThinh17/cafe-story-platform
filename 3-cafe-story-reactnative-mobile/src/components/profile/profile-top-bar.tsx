import { Send, Settings } from "lucide-react-native";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { colors, spacing, typography } from "../../theme";

type ProfileTopBarProps = {
  onMessagePress?: () => void;
  onSettingsPress?: () => void;
  userName: string;
};

export function ProfileTopBar({
  onMessagePress,
  onSettingsPress,
  userName,
}: ProfileTopBarProps) {
  return (
    <View style={styles.container}>
      <Pressable
        accessibilityLabel="Open settings"
        accessibilityRole="button"
        onPress={onSettingsPress}
        style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
      >
        <Settings color={colors.foreground} size={23} strokeWidth={2.2} />
      </Pressable>

      <Text numberOfLines={1} style={styles.title}>
        {userName}
      </Text>

      <Pressable
        accessibilityLabel="Open messages"
        accessibilityRole="button"
        onPress={onMessagePress}
        style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
      >
        <Send color={colors.foreground} size={23} strokeWidth={2.2} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    backgroundColor: colors.background,
    flexDirection: "row",
    justifyContent: "space-between",
    minHeight: 64,
    paddingHorizontal: spacing.sm,
    width: "100%",
  },
  iconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  pressed: {
    opacity: 0.62,
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "900",
    textAlign: "center",
  },
});
