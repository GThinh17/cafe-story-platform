import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { ArrowLeft } from "lucide-react-native";
import { useCallback, useEffect, useState } from "react";
import { Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from "react-native";

import { BlogFeedCard, EmptyState, FeedCardSkeletonList, Screen } from "../../components";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import { blogResponseToFeedBlog, getBlogById } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { BlogFeedResponse } from "../../types";

type BlogDetailRouteProp = RouteProp<RootStackParamList, typeof routes.blogDetail>;

export function BlogDetailScreen() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<BlogDetailRouteProp>();
  const { blogId } = route.params;
  const [blog, setBlog] = useState<BlogFeedResponse | null>(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const loadBlog = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    try {
      const response = await getBlogById(blogId);
      setBlog(blogResponseToFeedBlog(response));
      setError("");
    } catch {
      setError("Unable to load this post.");
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [blogId]);

  useEffect(() => {
    void loadBlog();
  }, [loadBlog]);

  return (
    <Screen padded={false}>
      <View style={styles.topBar}>
        <Pressable
          accessibilityLabel="Back"
          accessibilityRole="button"
          hitSlop={10}
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <ArrowLeft color={colors.foreground} size={30} strokeWidth={2.5} />
        </Pressable>
        <Text numberOfLines={1} style={styles.title}>
          Post
        </Text>
        <View style={styles.iconButton} />
      </View>

      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={
          <RefreshControl
            onRefresh={() => void loadBlog(true)}
            refreshing={isRefreshing}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        {isLoading && !blog ? (
          <FeedCardSkeletonList count={1} />
        ) : error ? (
          <EmptyState description="Pull down to retry." title={error} />
        ) : blog ? (
          <BlogFeedCard blog={blog} showFollowButton={false} />
        ) : (
          <EmptyState
            description="This post may have been removed."
            title="Post unavailable"
          />
        )}
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingBottom: 112,
  },
  iconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  pressed: {
    opacity: 0.72,
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
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: StyleSheet.hairlineWidth,
    flexDirection: "row",
    minHeight: 64,
    paddingHorizontal: spacing.sm,
  },
});
