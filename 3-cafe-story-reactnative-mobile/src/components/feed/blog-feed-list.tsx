import { StyleSheet, View } from "react-native";

import { BlogFeedCard } from "./blog-feed-card";
import { spacing } from "../../theme";
import type { BlogFeedResponse } from "../../types";

type BlogFeedListProps = {
  blogs: BlogFeedResponse[];
};

export function BlogFeedList({ blogs }: BlogFeedListProps) {
  return (
    <View style={styles.container}>
      {blogs.map((blog) => (
        <BlogFeedCard blog={blog} key={blog.blogId} />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 2,
    paddingTop: 2,
  },
});
