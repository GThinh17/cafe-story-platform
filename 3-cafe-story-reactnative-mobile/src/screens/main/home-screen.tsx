import { useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
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
import {
  getBlogFeed,
  getBlogLikesByUser,
  getBlogSavesByUser,
  getFollowingByUserId,
} from "../../services/api";
import { spacing } from "../../theme";
import { routes } from "../../navigation";
import type { MainTabParamList, RootStackParamList } from "../../navigation";
import type {
  BlogFeedResponse,
  BlogLikeResponse,
  BlogSaveResponse,
  StoryItem,
  UserFollowResponse,
} from "../../types";

function applyViewerState(
  blogs: BlogFeedResponse[],
  following: UserFollowResponse[],
  likes: BlogLikeResponse[],
  saves: BlogSaveResponse[],
  currentUserId?: string,
) {
  const followingUserIds = new Set(
    following.map((item) => item.followingUserId),
  );
  const likedBlogIds = new Set(likes.map((item) => item.blogId));
  const savedBlogIds = new Set(saves.map((item) => item.blogId));

  return blogs.map((blog) => ({
    ...blog,
    isFollow:
      blog.authorUserId === currentUserId
        ? false
        : followingUserIds.has(blog.authorUserId),
    isLike: likedBlogIds.has(blog.blogId),
    isSave: savedBlogIds.has(blog.blogId),
  }));
}

export function HomeScreen() {
  const navigation =
    useNavigation<
      BottomTabNavigationProp<MainTabParamList, typeof routes.home> &
        NativeStackNavigationProp<RootStackParamList>
    >();
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

      if (!user?.userId) {
        setBlogs(response);
        return;
      }

      const [following, likes, saves] = await Promise.all([
        getFollowingByUserId(user.userId).catch(() => []),
        getBlogLikesByUser(user.userId).catch(() => []),
        getBlogSavesByUser(user.userId).catch(() => []),
      ]);

      setBlogs(applyViewerState(response, following, likes, saves, user.userId));
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
  }, [user?.userId]);

  useEffect(() => {
    void loadFeed();
  }, [loadFeed]);

  useEffect(() => {
    return navigation.addListener("tabPress", () => {
      void loadFeed(true);
    });
  }, [loadFeed, navigation]);

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
        onRightPress={() => navigation.navigate(routes.conversations)}
        rightAccessibilityLabel="Open messages"
        rightIcon={Send}
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
