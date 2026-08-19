import { ActivityIndicator, StyleSheet, View } from "react-native";
import { Text } from "react-native";
import { colors, spacing, typography } from "../../theme";
import { t } from "../../features/i18n";

type LoadingStateProps = {
  label?: string;
};

export function LoadingState({ label = "Loading..." }: LoadingStateProps) {
  return (
    <View style={styles.container}>
      <ActivityIndicator color={colors.primary} />
      <Text style={styles.label}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    flex: 1,
    gap: spacing.md,
    justifyContent: "center",
  },
  label: {
    color: colors.muted,
    fontSize: typography.label,
  },
});
