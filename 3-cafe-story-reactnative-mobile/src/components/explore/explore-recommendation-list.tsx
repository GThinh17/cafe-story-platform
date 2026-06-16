import { StyleSheet, Text, View } from "react-native";

import { colors, spacing, typography } from "../../theme";
import type { RecommendationCardResponse } from "../../types";
import { EmptyState } from "../ui/empty-state";
import { LoadingState } from "../ui/loading-state";
import { ExploreRecommendationCard } from "./explore-recommendation-card";

type ExploreRecommendationListProps = {
  emptyDescription: string;
  emptyTitle: string;
  error?: string | null;
  isLoading?: boolean;
  items: RecommendationCardResponse[];
  onItemPress?: (item: RecommendationCardResponse) => void;
  title: string;
};

export function ExploreRecommendationList({
  emptyDescription,
  emptyTitle,
  error,
  isLoading = false,
  items,
  onItemPress,
  title,
}: ExploreRecommendationListProps) {
  if (isLoading) {
    return <LoadingState label="Loading recommendations..." />;
  }

  if (error) {
    return <EmptyState description="Pull down to try again." title={error} />;
  }

  if (!items.length) {
    return <EmptyState description={emptyDescription} title={emptyTitle} />;
  }

  return (
    <View style={styles.section}>
      <Text style={styles.title}>{title}</Text>
      <View style={styles.list}>
        {items.map((item) => (
          <ExploreRecommendationCard
            item={item}
            key={`${item.targetType}-${item.targetId}`}
            onPress={onItemPress}
          />
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  list: {
    gap: spacing.sm,
  },
  section: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  title: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
});
