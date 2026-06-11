import { Send } from "lucide-react-native";
import { ScrollView, StyleSheet } from "react-native";
import { BlogFeedList, Screen, ShareTopBar, StoryRail } from "../../components";
import { useAuth } from "../../features/auth";
import { mockBlogFeed, mockHomeFeedStories } from "../../mocks";
import { spacing } from "../../theme";
import type { StoryItem } from "../../types";

export function HomeScreen() {
  const { user } = useAuth();

  const stories: StoryItem[] = [
    {
      avatarUri: user?.userAvatar,
      id: "story-me",
      initials: user?.userName?.slice(0, 2).toUpperCase() ?? "ME",
      isSelf: true,
      label: "Your story",
    },
    ...mockHomeFeedStories,
  ];

  return (
    <Screen padded={false}>
      <ShareTopBar
        rightAccessibilityLabel="Open messages"
        rightIcon={Send}
        showRightBadge
      />
      <ScrollView
        contentContainerStyle={styles.feedContent}
        showsVerticalScrollIndicator={false}
      >
        <StoryRail stories={stories} />
        <BlogFeedList blogs={mockBlogFeed} />
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  feedContent: {
    paddingBottom: 112,
  },
});
