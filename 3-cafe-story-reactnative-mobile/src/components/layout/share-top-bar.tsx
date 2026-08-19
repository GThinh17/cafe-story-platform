import type { ComponentType } from "react";
import { Pressable } from "react-native";
import { Image, StyleSheet, View } from "react-native";

import { colors, spacing } from "../../theme";
import { BrandIcon } from "../ui/brand-icon";
import { t } from "../../features/i18n";

type OptionIconProps = {
  color?: string;
  size?: number;
  strokeWidth?: number;
};

type ShareTopBarProps = {
  onRightPress?: () => void;
  rightAccessibilityLabel?: string;
  rightIcon: ComponentType<OptionIconProps>;
  showRightBadge?: boolean;
};

const cafeStoryWordmark = require("../../../assets/brand/cafestory-wordmark.png");

export function ShareTopBar({
  onRightPress,
  rightAccessibilityLabel = "Open options",
  rightIcon: RightIcon,
  showRightBadge = false,
}: ShareTopBarProps) {
  return (
    <View style={styles.container}>
      <View
        accessibilityLabel={t("Cafe Story")}
        accessibilityRole="image"
        style={styles.brandGroup}
      >
        <BrandIcon size={31} />
        <Image
          accessibilityIgnoresInvertColors
          resizeMode="contain"
          source={cafeStoryWordmark}
          style={styles.wordmark}
        />
      </View>

      <Pressable
        accessibilityLabel={rightAccessibilityLabel}
        accessibilityRole="button"
        disabled={!onRightPress}
        onPress={onRightPress}
        style={({ pressed }) => [
          styles.iconButton,
          pressed && onRightPress ? styles.iconButtonPressed : null,
        ]}
      >
        <RightIcon color={colors.primary} size={23} strokeWidth={2.4} />
        {showRightBadge ? <View style={styles.badge} /> : null}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    backgroundColor: colors.danger,
    borderColor: colors.surface,
    borderRadius: 5,
    borderWidth: 2,
    height: 10,
    position: "absolute",
    right: 9,
    top: 9,
    width: 10,
  },
  container: {
    alignItems: "center",
    backgroundColor: colors.background,
    flexDirection: "row",
    justifyContent: "space-between",
    minHeight: 64,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.sm,
    width: "100%",
  },
  brandGroup: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
  },
  iconButton: {
    alignItems: "center",
    borderRadius: 22,
    height: 44,
    justifyContent: "center",
    position: "relative",
    width: 44,
  },
  iconButtonPressed: {
    opacity: 0.76,
    transform: [{ scale: 0.98 }],
  },
  wordmark: {
    height: 34,
    width: 138,
  },
});
