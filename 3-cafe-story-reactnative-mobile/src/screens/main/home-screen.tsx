import { Send } from "lucide-react-native";
import { useCallback, useEffect, useState } from "react";
import { RefreshControl, ScrollView, StyleSheet } from "react-native";
import {
  BlogFeedList,
  EmptyState,
  LoadingState,
  Screen,
  ShareTopBar,
  StoryRail,
} from "../../components";
import { useAuth } from "../../features/auth";
import { mockHomeFeedStories } from "../../mocks";
import { getBlogFeed } from "../../services/api";
import { spacing } from "../../theme";
import type { BlogFeedResponse, StoryItem } from "../../types";

export function HomeScreen() {
  const { user } = useAuth();
  const [blogs, setBlogs] = useState<BlogFeedResponse[]>([]);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const loadFeed = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    setError("");

    try {
      const response = await getBlogFeed({ page: 0, size: 20 });
      setBlogs(response);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load feed.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    void loadFeed();
  }, [loadFeed]);

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
        refreshControl={
          <RefreshControl
            onRefresh={() => void loadFeed(true)}
            refreshing={isRefreshing}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        <StoryRail stories={stories} />
        {isLoading ? (
          <LoadingState label="Loading feed..." />
        ) : error ? (
          <EmptyState description={error} title="Feed unavailable" />
        ) : blogs.length ? (
          <BlogFeedList blogs={blogs} />
        ) : (
          <EmptyState
            description="New cafe stories will appear here when they are ready."
            title="No posts yet"
          />
        )}
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  feedContent: {
    paddingBottom: 112,
  },
});
