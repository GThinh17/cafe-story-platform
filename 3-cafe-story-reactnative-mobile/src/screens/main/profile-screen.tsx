import { UserPlus } from "lucide-react-native";
import {
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { Avatar, ProfileTopBar, Screen } from "../../components";
import { useAuth } from "../../features/auth";
import { mockBlogFeed, mockHomeFeedStories } from "../../mocks";
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

  const postImages = mockBlogFeed.flatMap((blog) => blog.imageUrls).slice(0, 6);

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

        <ScrollView
          contentContainerStyle={styles.highlights}
          horizontal
          showsHorizontalScrollIndicator={false}
        >
          {mockHomeFeedStories.slice(0, 4).map((story) => (
            <View key={story.id} style={styles.highlight}>
              <Avatar initials={story.initials} size={58} uri={story.avatarUri} />

              <Text numberOfLines={1} style={styles.highlightLabel}>
                {story.label}
              </Text>
            </View>
          ))}
        </ScrollView>

        <View style={styles.grid}>
          {postImages.map((imageUrl, index) => (
            <Image
              key={`${imageUrl}-${index}`}
              resizeMode="cover"
              source={{ uri: imageUrl }}
              style={styles.gridImage}
            />
          ))}
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
    alignItems: "center",
    flex: 1,
    flexDirection: "row",
    justifyContent: "space-between",
  },

  statItem: {
    alignItems: "center",
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

  highlights: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },

  highlight: {
    alignItems: "center",
    gap: spacing.xs,
    width: 74,
  },

  highlightLabel: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "700",
    maxWidth: 72,
    textAlign: "center",
  },

  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 2,
  },

  gridImage: {
    aspectRatio: 1,
    backgroundColor: colors.surfaceMuted,
    width: "33%",
  },
  nameBlock: {
    gap: 2,
  },

  userName: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "600",
  },
});