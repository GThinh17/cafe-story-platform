import { Image, ScrollView, StyleSheet, Text, View } from "react-native";
import { Avatar, Button, ProfileTopBar, Screen } from "../../components";
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
  const { logout, user } = useAuth();
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
        <View style={styles.summary}>
          <Avatar initials={initialsFor(displayName)} size={88} uri={avatarUri} />
          <View style={styles.stats}>
            <View style={styles.statItem}>
              <Text style={styles.statValue}>42</Text>
              <Text style={styles.statLabel}>posts</Text>
            </View>
            <View style={styles.statItem}>
              <Text style={styles.statValue}>118</Text>
              <Text style={styles.statLabel}>cafes</Text>
            </View>
            <View style={styles.statItem}>
              <Text style={styles.statValue}>3.2k</Text>
              <Text style={styles.statLabel}>followers</Text>
            </View>
          </View>
        </View>

        <View style={styles.bio}>
          <Text style={styles.title}>{displayName}</Text>
          <Text style={styles.meta}>
            Collecting calm cafes, good filters, and corners worth returning to.
          </Text>
          <Text style={styles.link}>cafestory.vn/{userName}</Text>
        </View>

        <View style={styles.actions}>
          <Button label="Edit profile" variant="outlined" />
          <Button label="Logout" onPress={logout} variant="secondary" />
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
  actions: {
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  bio: {
    gap: spacing.xs,
    paddingHorizontal: spacing.lg,
  },
  content: {
    gap: spacing.lg,
    paddingBottom: 112,
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
  highlights: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  link: {
    color: colors.tertiary,
    fontSize: typography.label,
    fontWeight: "900",
  },
  meta: {
    color: colors.foreground,
    fontSize: typography.label,
    lineHeight: 20,
  },
  statItem: {
    alignItems: "center",
    flex: 1,
    gap: 2,
  },
  stats: {
    alignItems: "center",
    flex: 1,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  statLabel: {
    color: colors.muted,
    fontSize: typography.caption,
  },
  statValue: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  summary: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },
  title: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
});
