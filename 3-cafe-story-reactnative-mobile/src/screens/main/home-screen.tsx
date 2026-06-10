import { StyleSheet, Text, View } from "react-native";
import { Screen } from "../../components";
import { colors, spacing, typography } from "../../theme";

export function HomeScreen() {
  return (
    <Screen>
      <View style={styles.section}>
        <Text style={styles.eyebrow}>Feed</Text>
        <Text style={styles.title}>Cafe stories</Text>
        <Text style={styles.description}>
          Home feed foundation is ready for stories, posts, and nearby cafe cards.
        </Text>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  description: {
    color: colors.muted,
    fontSize: typography.body,
    lineHeight: 24,
  },
  eyebrow: {
    color: colors.primary,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  section: {
    gap: spacing.sm,
  },
  title: {
    color: colors.espresso,
    fontSize: typography.heading,
    fontWeight: "900",
  },
});
