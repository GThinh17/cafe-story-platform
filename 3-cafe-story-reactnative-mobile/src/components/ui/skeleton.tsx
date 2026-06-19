import { StyleSheet, View, type StyleProp, type ViewStyle } from "react-native";

import { colors, spacing } from "../../theme";

type SkeletonListProps = {
  count?: number;
};

type ListRowSkeletonListProps = SkeletonListProps & {
  padded?: boolean;
};

function SkeletonBlock({
  height,
  style,
  width = "100%",
}: {
  height: number;
  style?: StyleProp<ViewStyle>;
  width?: number | `${number}%`;
}) {
  return (
    <View
      style={[
        styles.block,
        {
          height,
          width,
        },
        style,
      ]}
    />
  );
}

export function FeedCardSkeletonList({ count = 3 }: SkeletonListProps) {
  return (
    <View style={styles.feedList}>
      {Array.from({ length: count }, (_, index) => (
        <View key={index} style={styles.feedCard}>
          <View style={styles.row}>
            <SkeletonBlock height={40} style={styles.avatar} width={40} />
            <View style={styles.flexColumn}>
              <SkeletonBlock height={12} width="46%" />
              <SkeletonBlock height={10} width="30%" />
            </View>
          </View>
          <SkeletonBlock height={12} width="88%" />
          <SkeletonBlock height={12} width="68%" />
          <SkeletonBlock height={180} style={styles.media} />
          <View style={styles.actionRow}>
            <SkeletonBlock height={18} width={70} />
            <SkeletonBlock height={18} width={70} />
            <SkeletonBlock height={18} width={70} />
          </View>
        </View>
      ))}
    </View>
  );
}

export function CommentSkeletonList({ count = 5 }: SkeletonListProps) {
  return (
    <View style={styles.commentList}>
      {Array.from({ length: count }, (_, index) => (
        <View key={index} style={styles.commentRow}>
          <SkeletonBlock height={38} style={styles.avatar} width={38} />
          <View style={styles.commentBody}>
            <SkeletonBlock height={12} width="42%" />
            <SkeletonBlock height={12} width={index % 2 === 0 ? "84%" : "64%"} />
            <SkeletonBlock height={10} width="28%" />
          </View>
        </View>
      ))}
    </View>
  );
}

export function ListRowSkeletonList({
  count = 6,
  padded = true,
}: ListRowSkeletonListProps) {
  return (
    <View style={[styles.rowList, padded && styles.rowListPadded]}>
      {Array.from({ length: count }, (_, index) => (
        <View key={index} style={styles.listRow}>
          <SkeletonBlock height={52} style={styles.avatar} width={52} />
          <View style={styles.flexColumn}>
            <SkeletonBlock height={13} width="54%" />
            <SkeletonBlock height={11} width={index % 2 === 0 ? "78%" : "48%"} />
          </View>
        </View>
      ))}
    </View>
  );
}

export function PostGridSkeleton({ count = 9 }: SkeletonListProps) {
  return (
    <View style={styles.grid}>
      {Array.from({ length: count }, (_, index) => (
        <View key={index} style={styles.gridTile}>
          <SkeletonBlock height={120} style={styles.gridBlock} />
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  actionRow: {
    flexDirection: "row",
    gap: spacing.lg,
  },
  avatar: {
    borderRadius: 999,
  },
  block: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
  },
  commentBody: {
    flex: 1,
    gap: spacing.xs,
    paddingTop: 2,
  },
  commentList: {
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.lg,
  },
  commentRow: {
    flexDirection: "row",
    gap: spacing.md,
  },
  feedCard: {
    backgroundColor: colors.background,
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.lg,
  },
  feedList: {
    gap: 2,
    paddingTop: 2,
  },
  flexColumn: {
    flex: 1,
    gap: spacing.xs,
  },
  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 2,
    width: "100%",
  },
  gridBlock: {
    borderRadius: 0,
  },
  gridTile: {
    aspectRatio: 1,
    flexBasis: "33%",
    flexGrow: 1,
  },
  listRow: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
  },
  media: {
    borderRadius: 14,
  },
  row: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
  },
  rowList: {
    gap: spacing.sm,
  },
  rowListPadded: {
    paddingHorizontal: spacing.lg,
  },
});
