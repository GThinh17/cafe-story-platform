import { StyleSheet, Text, View } from "react-native";
import { colors, spacing, typography } from "../../theme";

type EmptyStateProps = {
  description?: string;
  title: string;
};

export function EmptyState({ description, title }: EmptyStateProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      {description ? <Text style={styles.description}>{description}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    flex: 1,
    gap: spacing.sm,
    justifyContent: "center",
    padding: spacing.xl,
  },
  description: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 22,
    textAlign: "center",
  },
  title: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
    textAlign: "center",
  },
});
