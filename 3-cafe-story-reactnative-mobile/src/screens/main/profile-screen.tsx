import { StyleSheet, Text, View } from "react-native";
import { Avatar, Button, Screen } from "../../components";
import { useAuth } from "../../features/auth";
import { colors, spacing, typography } from "../../theme";

function initialsFor(name?: string | null) {
  if (!name) {
    return "CS";
  }

  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0])
    .join("")
    .toUpperCase();
}

export function ProfileScreen() {
  const { logout, user } = useAuth();
  const displayName = user?.userFullName || user?.userName || "Cafe Story user";

  return (
    <Screen>
      <View style={styles.profile}>
        <Avatar
          initials={initialsFor(displayName)}
          size={88}
          uri={user?.userAvatar}
        />
        <View style={styles.info}>
          <Text style={styles.title}>{displayName}</Text>
          <Text style={styles.meta}>{user?.userName}</Text>
          <Text style={styles.meta}>{user?.userEmail}</Text>
        </View>
        <Button label="Logout" onPress={logout} variant="secondary" />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  info: {
    gap: spacing.xs,
  },
  meta: {
    color: colors.muted,
    fontSize: typography.label,
  },
  profile: {
    gap: spacing.xl,
  },
  title: {
    color: colors.espresso,
    fontSize: typography.title,
    fontWeight: "900",
  },
});
