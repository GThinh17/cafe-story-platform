import { StyleSheet, View, useWindowDimensions } from "react-native";
import { colors, spacing } from "../../theme";

const GRID_GAP = 2;
const COLUMN_COUNT = 3;

export function ProfileSkeleton() {
  const { width } = useWindowDimensions();
  const gridItemSize = (width - GRID_GAP * (COLUMN_COUNT - 1)) / COLUMN_COUNT;

  return (
    <View style={styles.container}>
      <View style={styles.identity}>
        <View style={styles.avatar} />

        <View style={styles.identityContent}>
          <View style={[styles.block, styles.name]} />

          <View style={styles.stats}>
            {[0, 1, 2].map((item) => (
              <View key={item} style={styles.statItem}>
                <View style={[styles.block, styles.statValue]} />
                <View style={[styles.block, styles.statLabel]} />
              </View>
            ))}
          </View>
        </View>
      </View>

      <View style={styles.bio}>
        <View style={[styles.block, styles.bioLine]} />
        <View style={[styles.block, styles.bioLineShort]} />
      </View>

      <View style={styles.chips}>
        <View style={[styles.block, styles.chip]} />
        <View style={[styles.block, styles.chipSmall]} />
      </View>

      <View style={styles.actions}>
        <View style={[styles.block, styles.action]} />
        <View style={[styles.block, styles.action]} />
        <View style={[styles.block, styles.actionIcon]} />
      </View>

      <View style={styles.tabs}>
        {[0, 1, 2, 3].map((item) => (
          <View key={item} style={styles.tab}>
            <View style={[styles.block, styles.tabIcon]} />
          </View>
        ))}
      </View>

      <View style={styles.grid}>
        {Array.from({ length: 9 }).map((_, index) => (
          <View
            key={index}
            style={[
              styles.gridItem,
              {
                height: gridItemSize,
                width: gridItemSize,
              },
            ]}
          />
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  action: {
    flex: 1,
    height: 36,
  },

  actionIcon: {
    height: 36,
    width: 42,
  },

  actions: {
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },

  avatar: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 44,
    height: 88,
    width: 88,
  },

  bio: {
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },

  bioLine: {
    height: 14,
    width: "72%",
  },

  bioLineShort: {
    height: 14,
    width: "48%",
  },

  block: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
  },

  chip: {
    height: 36,
    width: 132,
  },

  chipSmall: {
    height: 36,
    width: 72,
  },

  chips: {
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },

  container: {
    gap: spacing.md,
    paddingBottom: 112,
    paddingTop: spacing.md,
  },

  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: GRID_GAP,
  },

  gridItem: {
    backgroundColor: colors.surfaceMuted,
  },

  identity: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
  },

  identityContent: {
    flex: 1,
    gap: spacing.md,
  },

  name: {
    height: 18,
    width: "62%",
  },

  statItem: {
    flex: 1,
    gap: spacing.xs,
  },

  statLabel: {
    height: 10,
    width: "56%",
  },

  statValue: {
    height: 16,
    width: "38%",
  },

  stats: {
    flexDirection: "row",
    justifyContent: "space-between",
  },

  tab: {
    alignItems: "center",
    flex: 1,
    height: 48,
    justifyContent: "center",
  },

  tabIcon: {
    borderRadius: 10,
    height: 24,
    width: 24,
  },

  tabs: {
    flexDirection: "row",
    paddingTop: spacing.sm,
  },
});
