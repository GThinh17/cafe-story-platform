import { ScrollView, StyleSheet, View } from "react-native";
import { Text } from "react-native";
import { Avatar } from "../ui/avatar";
import { colors, spacing, typography } from "../../theme";
import type { MockOnlineUser } from "../../types";
import { t } from "../../features/i18n";

type OnlineUserRailProps = {
  users: MockOnlineUser[];
};

export function OnlineUserRail({ users }: OnlineUserRailProps) {
  return (
    <ScrollView
      contentContainerStyle={styles.content}
      horizontal
      showsHorizontalScrollIndicator={false}
    >
      {users.map((user) => (
        <View key={user.id} style={styles.item}>
          <View>
            <Avatar size={62} uri={user.avatarUri} />
            {user.isOnline ? <View style={styles.onlineDot} /> : null}
          </View>
          <Text numberOfLines={1} style={styles.name}>
            {user.name}
          </Text>
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: {
    gap: spacing.lg,
    paddingVertical: spacing.md,
  },
  item: {
    alignItems: "center",
    gap: spacing.xs,
    width: 72,
  },
  name: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "600",
    maxWidth: 70,
  },
  onlineDot: {
    backgroundColor: colors.tertiary,
    borderColor: colors.background,
    borderRadius: 7,
    borderWidth: 2,
    bottom: 2,
    height: 14,
    position: "absolute",
    right: 2,
    width: 14,
  },
});
