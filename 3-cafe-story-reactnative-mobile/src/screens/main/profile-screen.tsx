import { AtSign, Grid3X3, Plus, Repeat2, SquarePlay, UserPlus, UserRound } from "lucide-react-native";
import {
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { Avatar, ProfileTopBar, Screen, UserPostGrid } from "../../components";
import { useAuth } from "../../features/auth";
import { mockUserPosts } from "../../mocks";
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
  const { user } = useAuth();

  const displayName = user?.userFullName || user?.userName || "Cafe Story user";
  const userName = user?.userName || "cafestory";
  const avatarUri = user?.userAvatar;

  return (
    <Screen padded={false}>
      <ProfileTopBar userName={userName} />

      <ScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.identity}>
          <Avatar initials={initialsFor(displayName)} size={88} uri={avatarUri} />

          <View style={styles.identityContent}>
            <Text numberOfLines={1} style={styles.title}>
              {displayName}
            </Text>

            <View style={styles.stats}>
              <View style={styles.statItem}>
                <Text style={styles.statValue}>42</Text>
                <Text style={styles.statLabel}>posts</Text>
              </View>

              <View style={styles.statItem}>
                <Text style={styles.statValue}>3.2k</Text>
                <Text style={styles.statLabel}>followers</Text>
              </View>

              <View style={styles.statItem}>
                <Text style={styles.statValue}>215</Text>
                <Text style={styles.statLabel}>following</Text>
              </View>
            </View>
          </View>
        </View>

        <Text style={styles.description}>
          Collecting calm cafes, good filters, and corners worth returning to.
        </Text>

        <View style={styles.profileChips}>
          <View style={styles.profileChip}>
            <AtSign color={colors.foreground} size={16} strokeWidth={2.4} />
            <Text numberOfLines={1} style={styles.profileChipText}>
              {userName}
            </Text>
          </View>

          <View style={styles.profileChip}>
            <Plus color={colors.muted} size={18} strokeWidth={2.4} />
            <Text style={styles.profileChipMuted}>Add</Text>
          </View>
        </View>

        <View style={styles.actions}>
          <Pressable
            accessibilityLabel="Edit profile"
            accessibilityRole="button"
            style={({ pressed }) => [
              styles.profileActionButton,
              pressed && styles.actionPressed,
            ]}
          >
            <Text style={styles.profileActionText}>Edit Profile</Text>
          </Pressable>

          <Pressable
            accessibilityLabel="Share profile"
            accessibilityRole="button"
            style={({ pressed }) => [
              styles.profileActionButton,
              pressed && styles.actionPressed,
            ]}
          >
            <Text style={styles.profileActionText}>Share Profile</Text>
          </Pressable>

          <Pressable
            accessibilityLabel="Open profile suggestions"
            accessibilityRole="button"
            style={({ pressed }) => [
              styles.addFriendButton,
              pressed && styles.actionPressed,
            ]}
          >
            <UserPlus color={colors.foreground} size={19} strokeWidth={2.5} />
          </Pressable>
        </View>

        <View style={styles.profileTabs}>
          <View style={[styles.profileTab, styles.profileTabActive]}>
            <Grid3X3 color={colors.foreground} size={23} strokeWidth={2.8} />
          </View>
          <View style={styles.profileTab}>
            <SquarePlay color={colors.foreground} size={24} strokeWidth={2.4} />
          </View>
          <View style={styles.profileTab}>
            <Repeat2 color={colors.foreground} size={24} strokeWidth={2.4} />
          </View>
          <View style={styles.profileTab}>
            <UserRound color={colors.muted} size={24} strokeWidth={2.4} />
          </View>
        </View>

        <View style={styles.grid}>
          <UserPostGrid posts={mockUserPosts} />
        </View>
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: {
    gap: spacing.md,
    paddingBottom: 112,
  },

  identity: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },

  identityContent: {
    flex: 1,
    gap: spacing.md,
  },

  title: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },

  stats: {
    alignItems: "flex-start",
    flex: 1,
    flexDirection: "row",
    justifyContent: "space-between",
  },

  statItem: {
    alignItems: "flex-start",
    flex: 1,
    gap: 2,
  },

  statValue: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },

  statLabel: {
    color: colors.muted,
    fontSize: typography.caption,
  },

  description: {
    color: colors.foreground,
    fontSize: typography.label,
    lineHeight: 20,
    paddingHorizontal: spacing.lg,
  },

  actions: {
    alignItems: "center",
    flexDirection: "row",
    gap: 8,
    paddingHorizontal: spacing.lg,
  },

  profileActionButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    flex: 1,
    height: 36,
    justifyContent: "center",
  },

  profileActionText: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "800",
  },

  addFriendButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    height: 36,
    justifyContent: "center",
    width: 42,
  },

  actionPressed: {
    opacity: 0.72,
  },

  profileChips: {
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },

  profileChip: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 36,
    paddingHorizontal: spacing.md,
  },

  profileChipMuted: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "800",
  },

  profileChipText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },

  profileTabs: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-around",
    paddingTop: spacing.sm,
  },

  profileTab: {
    alignItems: "center",
    borderBottomColor: "transparent",
    borderBottomWidth: 2,
    flex: 1,
    height: 48,
    justifyContent: "center",
  },

  profileTabActive: {
    borderBottomColor: colors.foreground,
  },

  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 2,
  },
});
