import { CreditCard, Ellipsis, LayoutDashboard, Megaphone, Send, Settings, Store, } from "lucide-react-native";
import { Pressable, Text } from "../../features/i18n/localized-native";
import { Modal, StyleSheet, View } from "react-native";
import { useState, type ReactNode } from "react";

import { colors, spacing, typography } from "../../theme";

type ProfileTopBarProps = {
  onAdsManagerPress?: () => void;
  onCafePagePress?: () => void;
  onMessagePress?: () => void;
  onPaymentPress?: () => void;
  onReviewerDashboardPress?: () => void;
  onSettingsPress?: () => void;
  showAdsManagerAction?: boolean;
  showCafePageAction?: boolean;
  showReviewerDashboardAction?: boolean;
  userName: string;
};

export function ProfileTopBar({
  onAdsManagerPress,
  onCafePagePress,
  onMessagePress,
  onPaymentPress,
  onReviewerDashboardPress,
  onSettingsPress,
  showAdsManagerAction = false,
  showCafePageAction = false,
  showReviewerDashboardAction = false,
  userName,
}: ProfileTopBarProps) {
  const [isOptionsVisible, setIsOptionsVisible] = useState(false);

  function closeOptions() {
    setIsOptionsVisible(false);
  }

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

      <View style={styles.rightActions}>
        <Pressable
          accessibilityLabel="Open profile options"
          accessibilityRole="button"
          onPress={() => setIsOptionsVisible(true)}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <Ellipsis color={colors.foreground} size={25} strokeWidth={2.4} />
        </Pressable>

        <Pressable
          accessibilityLabel="Open messages"
          accessibilityRole="button"
          onPress={onMessagePress}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <Send color={colors.foreground} size={23} strokeWidth={2.2} />
        </Pressable>
      </View>

      <Modal
        animationType="fade"
        onRequestClose={closeOptions}
        transparent
        visible={isOptionsVisible}
      >
        <Pressable
          accessibilityLabel="Close profile options"
          onPress={closeOptions}
          style={styles.menuBackdrop}
        >
          <View style={styles.optionsMenu}>
            <OptionItem
              icon={<CreditCard color={colors.foreground} size={20} strokeWidth={2.4} />}
              label="Payment"
              onPress={() => {
                closeOptions();
                onPaymentPress?.();
              }}
            />
            {showAdsManagerAction ? (
              <OptionItem
                icon={<Megaphone color={colors.foreground} size={20} strokeWidth={2.4} />}
                label="Ads Manager"
                onPress={() => {
                  closeOptions();
                  onAdsManagerPress?.();
                }}
              />
            ) : null}
            {showReviewerDashboardAction ? (
              <OptionItem
                icon={
                  <LayoutDashboard
                    color={colors.foreground}
                    size={20}
                    strokeWidth={2.4}
                  />
                }
                label="Reviewer"
                onPress={() => {
                  closeOptions();
                  onReviewerDashboardPress?.();
                }}
              />
            ) : null}
            {showCafePageAction ? (
              <OptionItem
                icon={<Store color={colors.foreground} size={20} strokeWidth={2.4} />}
                label="Cafe"
                onPress={() => {
                  closeOptions();
                  onCafePagePress?.();
                }}
              />
            ) : null}
          </View>
        </Pressable>
      </Modal>
    </View>
  );
}

function OptionItem({
  icon,
  label,
  onPress,
}: {
  icon: ReactNode;
  label: string;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityLabel={`Open ${label.toLowerCase()}`}
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [styles.optionItem, pressed && styles.pressed]}
    >
      {icon}
      <Text style={styles.optionText}>{label}</Text>
    </Pressable>
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
  menuBackdrop: {
    flex: 1,
  },
  optionItem: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 42,
    paddingHorizontal: spacing.md,
  },
  optionText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  optionsMenu: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    elevation: 6,
    minWidth: 156,
    paddingVertical: spacing.xs,
    position: "absolute",
    right: spacing.md,
    shadowColor: colors.foreground,
    shadowOffset: { height: 4, width: 0 },
    shadowOpacity: 0.14,
    shadowRadius: 10,
    top: 58,
  },
  pressed: {
    opacity: 0.62,
  },
  rightActions: {
    alignItems: "center",
    flexDirection: "row",
    minWidth: 48,
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "900",
    textAlign: "center",
  },
});
