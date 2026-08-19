import { Check, Languages } from "lucide-react-native";
import { useState } from "react";
import {
  AccessibilityInfo,
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";

import { colors, spacing, typography } from "../../theme";
import type { LocalePreference } from "./config";
import { useI18n } from "./locale-provider";
import type { TranslationKey } from "./dictionaries/en";

type LanguageSelectorProps = {
  variant?: "cards" | "compact";
};

const preferences: Array<{
  compactKey: TranslationKey;
  descriptionKey: TranslationKey;
  nameKey: TranslationKey;
  value: LocalePreference;
}> = [
  {
    compactKey: "language.compact.system",
    descriptionKey: "language.system.description",
    nameKey: "language.system.name",
    value: "system",
  },
  {
    compactKey: "language.compact.en",
    descriptionKey: "language.english.description",
    nameKey: "language.english.name",
    value: "en",
  },
  {
    compactKey: "language.compact.vi",
    descriptionKey: "language.vietnamese.description",
    nameKey: "language.vietnamese.name",
    value: "vi",
  },
];

export function LanguageSelector({
  variant = "cards",
}: LanguageSelectorProps) {
  const { preference, setLocalePreference, t } = useI18n();
  const [pendingPreference, setPendingPreference] =
    useState<LocalePreference | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function selectPreference(nextPreference: LocalePreference) {
    if (nextPreference === preference || pendingPreference) {
      return;
    }

    setError(null);
    setPendingPreference(nextPreference);

    try {
      await setLocalePreference(nextPreference);
    } catch {
      const message = t("language.saveError");
      setError(message);
      AccessibilityInfo.announceForAccessibility(message);
    } finally {
      setPendingPreference(null);
    }
  }

  if (variant === "compact") {
    return (
      <View style={styles.compactSection}>
        <Text style={styles.srOnly}>{t("language.selectorLabel")}</Text>
        <View
          accessibilityLabel={t("language.selectorLabel")}
          accessibilityRole="radiogroup"
          style={styles.compactRow}
        >
          {preferences.map((item) => {
            const isSelected = preference === item.value;
            const isPending = pendingPreference === item.value;

            return (
              <Pressable
                aria-busy={isPending}
                aria-checked={isSelected}
                aria-disabled={Boolean(pendingPreference)}
                accessibilityLabel={t(item.nameKey)}
                accessibilityRole="radio"
                accessibilityState={{
                  busy: isPending,
                  checked: isSelected,
                  disabled: Boolean(pendingPreference),
                }}
                disabled={Boolean(pendingPreference)}
                key={item.value}
                onPress={() => void selectPreference(item.value)}
                style={({ pressed }) => [
                  styles.compactOption,
                  isSelected && styles.compactOptionSelected,
                  pressed && styles.pressed,
                ]}
              >
                {isPending ? (
                  <ActivityIndicator color={colors.white} size="small" />
                ) : (
                  <Text
                    style={[
                      styles.compactLabel,
                      isSelected && styles.compactLabelSelected,
                    ]}
                  >
                    {t(item.compactKey)}
                  </Text>
                )}
              </Pressable>
            );
          })}
        </View>
        {error ? (
          <Text accessibilityLiveRegion="assertive" style={styles.errorText}>
            {error}
          </Text>
        ) : null}
      </View>
    );
  }

  return (
    <View style={styles.cardSection}>
      <View style={styles.cardHeading}>
        <View style={styles.cardHeadingIcon}>
          <Languages color={colors.primary} size={20} strokeWidth={2.4} />
        </View>
        <View style={styles.cardHeadingCopy}>
          <Text style={styles.title}>{t("language.title")}</Text>
          <Text style={styles.description}>{t("language.description")}</Text>
        </View>
      </View>

      <View accessibilityRole="radiogroup" style={styles.cardOptions}>
        {preferences.map((item) => {
          const isSelected = preference === item.value;
          const isPending = pendingPreference === item.value;

          return (
            <Pressable
              aria-busy={isPending}
              aria-checked={isSelected}
              aria-disabled={Boolean(pendingPreference)}
              accessibilityLabel={t(item.nameKey)}
              accessibilityRole="radio"
              accessibilityState={{
                busy: isPending,
                checked: isSelected,
                disabled: Boolean(pendingPreference),
              }}
              disabled={Boolean(pendingPreference)}
              key={item.value}
              onPress={() => void selectPreference(item.value)}
              style={({ pressed }) => [
                styles.cardOption,
                isSelected && styles.cardOptionSelected,
                pressed && styles.pressed,
              ]}
            >
              <View style={styles.cardOptionCopy}>
                <Text style={styles.cardOptionName}>{t(item.nameKey)}</Text>
                <Text style={styles.cardOptionDescription}>
                  {t(item.descriptionKey)}
                </Text>
              </View>
              <View
                style={[
                  styles.radio,
                  isSelected && styles.radioSelected,
                ]}
              >
                {isPending ? (
                  <ActivityIndicator color={colors.white} size="small" />
                ) : isSelected ? (
                  <Check color={colors.white} size={13} strokeWidth={3} />
                ) : null}
              </View>
            </Pressable>
          );
        })}
      </View>

      {error ? (
        <Text accessibilityLiveRegion="assertive" style={styles.errorText}>
          {error}
        </Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  cardHeading: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
  },
  cardHeadingCopy: {
    flex: 1,
    gap: 3,
  },
  cardHeadingIcon: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    height: 42,
    justifyContent: "center",
    width: 42,
  },
  cardOption: {
    alignItems: "center",
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 66,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  cardOptionCopy: {
    flex: 1,
    gap: 3,
  },
  cardOptionDescription: {
    color: colors.muted,
    fontSize: typography.caption,
    lineHeight: 17,
  },
  cardOptionName: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "800",
  },
  cardOptionSelected: {
    backgroundColor: colors.surfaceMuted,
    borderColor: colors.primary,
  },
  cardOptions: {
    gap: spacing.sm,
  },
  cardSection: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    gap: spacing.md,
    padding: spacing.md,
  },
  compactLabel: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  compactLabelSelected: {
    color: colors.white,
  },
  compactOption: {
    alignItems: "center",
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    justifyContent: "center",
    minHeight: 36,
    minWidth: 54,
    paddingHorizontal: spacing.md,
  },
  compactOptionSelected: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  compactRow: {
    alignItems: "center",
    alignSelf: "flex-end",
    flexDirection: "row",
    gap: spacing.xs,
  },
  compactSection: {
    alignItems: "flex-end",
    gap: spacing.xs,
  },
  description: {
    color: colors.muted,
    fontSize: typography.caption,
    lineHeight: 17,
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  pressed: {
    opacity: 0.72,
  },
  radio: {
    alignItems: "center",
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    height: 20,
    justifyContent: "center",
    width: 20,
  },
  radioSelected: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  srOnly: {
    height: 1,
    opacity: 0,
    position: "absolute",
    width: 1,
  },
  title: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
});
