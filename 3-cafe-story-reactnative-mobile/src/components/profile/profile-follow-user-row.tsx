import { MessageCircle, UserPlus, X } from "lucide-react-native";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { colors, spacing, typography } from "../../theme";
import type { UserResponse } from "../../types";
import { Avatar } from "../ui/avatar";

type ProfileFollowUserRowProps = {
  disabled?: boolean;
  isCurrentUser?: boolean;
  onFollowPress?: (user: UserResponse) => void;
  onMessagePress?: (user: UserResponse) => void;
  onProfilePress?: (user: UserResponse) => void;
  user: UserResponse;
};

function getDisplayName(user: UserResponse) {
  return user.userFullName || user.userName;
}

export function ProfileFollowUserRow({
  disabled = false,
  isCurrentUser = false,
  onFollowPress,
  onMessagePress,
  onProfilePress,
  user,
}: ProfileFollowUserRowProps) {
  const isFollowing = Boolean(user.isFollowing);
  const canMessage = !isCurrentUser;
  const canFollow = !isCurrentUser && !isFollowing;

  return (
    <View style={styles.container}>
      <Pressable
        accessibilityLabel={`Open ${user.userName} profile`}
        accessibilityRole="button"
        onPress={() => onProfilePress?.(user)}
        style={({ pressed }) => [
          styles.identityAction,
          pressed && styles.pressed,
        ]}
      >
        <Avatar size={58} uri={user.userAvatar} />
        <View style={styles.identity}>
          <Text numberOfLines={1} style={styles.username}>
            {user.userName}
          </Text>
          <Text numberOfLines={1} style={styles.name}>
            {getDisplayName(user)}
          </Text>
        </View>
      </Pressable>

      {canFollow ? (
        <Pressable
          accessibilityLabel={`Follow ${user.userName}`}
          accessibilityRole="button"
          disabled={disabled}
          onPress={() => onFollowPress?.(user)}
          style={({ pressed }) => [
            styles.followButton,
            pressed && !disabled && styles.pressed,
          ]}
        >
          <UserPlus color={colors.white} size={16} strokeWidth={2.4} />
          <Text style={styles.followButtonText}>Follow</Text>
        </Pressable>
      ) : canMessage ? (
        <Pressable
          accessibilityLabel={`Message ${user.userName}`}
          accessibilityRole="button"
          disabled={disabled}
          onPress={() => onMessagePress?.(user)}
          style={({ pressed }) => [
            styles.messageButton,
            pressed && !disabled && styles.pressed,
          ]}
        >
          <MessageCircle color={colors.foreground} size={16} strokeWidth={2.4} />
          <Text style={styles.messageButtonText}>Message</Text>
        </Pressable>
      ) : null}

      <View style={styles.trailingIcon}>
        <X color={colors.muted} size={19} strokeWidth={2.2} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 78,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  followButton: {
    alignItems: "center",
    backgroundColor: colors.link,
    borderRadius: 12,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 38,
    minWidth: 106,
    justifyContent: "center",
    paddingHorizontal: spacing.md,
  },
  followButtonText: {
    color: colors.white,
    fontSize: typography.label,
    fontWeight: "900",
  },
  identity: {
    flex: 1,
    justifyContent: "center",
  },
  identityAction: {
    alignItems: "center",
    flex: 1,
    flexDirection: "row",
    gap: spacing.md,
  },
  messageButton: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 12,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 38,
    minWidth: 106,
    justifyContent: "center",
    paddingHorizontal: spacing.md,
  },
  messageButtonText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  name: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "600",
    marginTop: 2,
  },
  pressed: {
    opacity: 0.72,
  },
  trailingIcon: {
    alignItems: "center",
    height: 36,
    justifyContent: "center",
    width: 28,
  },
  username: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
});
