import { ChevronLeft, LogOut, Shield, UserRound } from "lucide-react-native";
import { Pressable } from "../../features/i18n/localized-native";
import { Text } from "../../features/i18n/localized-native";
import { useState } from "react";
import { ScrollView, StyleSheet, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";

import { Screen } from "../../components";
import { useAuth } from "../../features/auth";
import { LanguageSelector } from "../../features/i18n";
import type { RootStackParamList } from "../../navigation";
import { colors, spacing, typography } from "../../theme";

type SettingsNavigation = NativeStackNavigationProp<RootStackParamList>;

export function SettingsScreen() {
  const navigation = useNavigation<SettingsNavigation>();
  const { logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleLogout() {
    setIsLoggingOut(true);
    setError(null);

    try {
      await logout();
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to log out. Please try again.",
      );
      setIsLoggingOut(false);
    }
  }

  return (
    <Screen padded={false}>
      <View style={styles.topBar}>
        <Pressable
          accessibilityLabel="Go back"
          accessibilityRole="button"
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <ChevronLeft color={colors.foreground} size={32} strokeWidth={2.4} />
        </Pressable>

        <Text style={styles.title}>Settings</Text>

        <View style={styles.iconButton} />
      </View>

      <ScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        <LanguageSelector />

        <View style={styles.section}>
          <SettingsRow
            Icon={UserRound}
            description="Profile details, bio, and account preferences."
            label="Account"
          />
          <SettingsRow
            Icon={Shield}
            description="Security and privacy controls for your CafeStory account."
            label="Privacy and security"
          />
        </View>

        {error ? <Text style={styles.errorText}>{error}</Text> : null}

        <Pressable
          accessibilityLabel="Log out"
          accessibilityRole="button"
          disabled={isLoggingOut}
          onPress={handleLogout}
          style={({ pressed }) => [
            styles.logoutButton,
            pressed && styles.pressed,
            isLoggingOut && styles.disabled,
          ]}
        >
          <LogOut color={colors.danger} size={20} strokeWidth={2.5} />
          <Text style={styles.logoutText}>
            {isLoggingOut ? "Logging out..." : "Log out"}
          </Text>
        </Pressable>
      </ScrollView>
    </Screen>
  );
}

type SettingsRowProps = {
  Icon: typeof UserRound;
  description: string;
  label: string;
};

function SettingsRow({ Icon, description, label }: SettingsRowProps) {
  return (
    <Pressable
      accessibilityRole="button"
      style={({ pressed }) => [styles.row, pressed && styles.pressed]}
    >
      <View style={styles.rowIcon}>
        <Icon color={colors.foreground} size={22} strokeWidth={2.3} />
      </View>

      <View style={styles.rowCopy}>
        <Text style={styles.rowLabel}>{label}</Text>
        <Text numberOfLines={2} style={styles.rowDescription}>
          {description}
        </Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  content: {
    gap: spacing.lg,
    paddingBottom: 96,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.sm,
  },
  disabled: {
    opacity: 0.58,
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 18,
  },
  iconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  logoutButton: {
    alignItems: "center",
    alignSelf: "stretch",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.sm,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: spacing.lg,
  },
  logoutText: {
    color: colors.danger,
    fontSize: typography.body,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.72,
  },
  row: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 72,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
  },
  rowCopy: {
    flex: 1,
    gap: 3,
  },
  rowDescription: {
    color: colors.muted,
    fontSize: typography.caption,
    lineHeight: 17,
  },
  rowIcon: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    height: 42,
    justifyContent: "center",
    width: 42,
  },
  rowLabel: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "800",
  },
  section: {
    gap: spacing.sm,
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "900",
    textAlign: "center",
  },
  topBar: {
    alignItems: "center",
    flexDirection: "row",
    minHeight: 64,
    paddingHorizontal: spacing.sm,
  },
});
