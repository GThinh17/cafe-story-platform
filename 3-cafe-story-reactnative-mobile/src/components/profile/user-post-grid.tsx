import { Image, Pressable, StyleSheet, useWindowDimensions } from "react-native";
import type { UserPostPreview } from "../../types";
import { colors } from "../../theme";

const GRID_GAP = 2;
const COLUMN_COUNT = 3;

type UserPostGridProps = {
  posts: UserPostPreview[];
};

export function UserPostGrid({ posts }: UserPostGridProps) {
  const { width } = useWindowDimensions();
  const itemSize = (width - GRID_GAP * (COLUMN_COUNT - 1)) / COLUMN_COUNT;

  return (
    <>
      {posts.map((post) => (
        <Pressable
          accessibilityLabel={post.caption}
          accessibilityRole="imagebutton"
          key={post.id}
          style={({ pressed }) => [
            styles.item,
            {
              height: itemSize,
              width: itemSize,
            },
            pressed && styles.itemPressed,
          ]}
        >
          <Image resizeMode="cover" source={post.image} style={styles.image} />
        </Pressable>
      ))}
    </>
  );
}

const styles = StyleSheet.create({
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
