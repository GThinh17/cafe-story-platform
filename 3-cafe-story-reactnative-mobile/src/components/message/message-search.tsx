import { Search } from "lucide-react-native";
import { TextInput } from "react-native";
import { StyleSheet, View } from "react-native";
import { colors, spacing, typography } from "../../theme";
import { t } from "../../features/i18n";

type MessageSearchProps = {
  onChangeText: (value: string) => void;
  value: string;
};

export function MessageSearch({ onChangeText, value }: MessageSearchProps) {
  return (
    <View style={styles.container}>
      <Search color={colors.muted} size={19} strokeWidth={2.2} />
      <TextInput
        autoCapitalize="none"
        onChangeText={onChangeText}
        placeholder={t("Search")}
        placeholderTextColor={colors.muted}
        style={styles.input}
        value={value}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 18,
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 42,
    paddingHorizontal: spacing.md,
  },
  input: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    paddingVertical: spacing.sm,
  },
});
