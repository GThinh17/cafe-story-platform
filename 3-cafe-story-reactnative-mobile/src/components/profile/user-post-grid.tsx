import { Image, Pressable, StyleSheet, Text, useWindowDimensions } from "react-native";
import type { UserPostPreview } from "../../types";
import { colors, spacing, typography } from "../../theme";

const GRID_GAP = 2;
const COLUMN_COUNT = 3;

type UserPostGridProps = {
  onPostPress?: (post: UserPostPreview) => void;
  posts: UserPostPreview[];
};

export function UserPostGrid({ onPostPress, posts }: UserPostGridProps) {
  const { width } = useWindowDimensions();
  const itemSize = (width - GRID_GAP * (COLUMN_COUNT - 1)) / COLUMN_COUNT;

  return (
    <>
      {posts.map((post) => (
        <PostGridItem
          itemSize={itemSize}
          key={post.id}
          onPress={onPostPress}
          post={post}
        />
      ))}
    </>
  );
}

type PostGridItemProps = {
  itemSize: number;
  onPress?: (post: UserPostPreview) => void;
  post: UserPostPreview;
};

function PostGridItem({ itemSize, onPress, post }: PostGridItemProps) {
  const imageSource = post.image ?? (post.imageUri ? { uri: post.imageUri } : null);

  return (
    <Pressable
      accessibilityLabel={post.caption}
      accessibilityRole="imagebutton"
      onPress={() => onPress?.(post)}
      style={({ pressed }) => [
        styles.item,
        {
          height: itemSize,
          width: itemSize,
        },
        pressed && styles.itemPressed,
      ]}
    >
      {imageSource ? (
        <Image resizeMode="cover" source={imageSource} style={styles.image} />
      ) : (
        <Text numberOfLines={5} style={styles.caption}>
          {post.caption || "CafeStory post"}
        </Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  caption: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 17,
    padding: spacing.sm,
  },

  image: {
    height: "100%",
    width: "100%",
  },

  item: {
    backgroundColor: colors.surfaceMuted,
    overflow: "hidden",
  },

  itemPressed: {
    opacity: 0.86,
  },
});
