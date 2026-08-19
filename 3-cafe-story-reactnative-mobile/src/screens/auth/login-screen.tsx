import { useNavigation } from "@react-navigation/native";
import { Pressable } from "react-native";
import { Text } from "react-native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { useState } from "react";
import { StyleSheet, View } from "react-native";
import { Button, Screen, TextField } from "../../components";
import { useAuth } from "../../features/auth";
import { LanguageSelector } from "../../features/i18n";
import { routes } from "../../navigation";
import type { AuthStackParamList } from "../../navigation";
import { colors, spacing, typography } from "../../theme";
import { t } from "../../features/i18n";

export function LoginScreen() {
  const { login } = useAuth();
  const navigation =
    useNavigation<NativeStackNavigationProp<AuthStackParamList>>();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleLogin() {
    setError("");

    if (!identifier.trim() || !password) {
      setError(t("Email/username and password are required."));
      return;
    }

    setIsSubmitting(true);

    try {
      await login({ identifier: identifier.trim(), password });
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to sign in.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Screen>
      <LanguageSelector variant="compact" />
      <View style={styles.header}>
        <Text style={styles.eyebrow}>CafeStory</Text>
        <Text style={styles.title}>{t("Welcome back")}</Text>
        <Text style={styles.description}>{t("Sign in to keep collecting cafe stories, saved corners, and reviews.")}</Text>
      </View>

      <View style={styles.form}>
        <TextField
          autoCapitalize="none"
          label={t("Email or username")}
          onChangeText={setIdentifier}
          placeholder="hello@cafestory.com"
          value={identifier}
        />
        <TextField
          label={t("Password")}
          onChangeText={setPassword}
          placeholder={t("Enter your password")}
          secureTextEntry
          value={password}
        />
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <Button
          disabled={isSubmitting || !identifier.trim() || !password}
          isLoading={isSubmitting}
          label={t("Sign in")}
          onPress={handleLogin}
        />
        <Pressable
          accessibilityRole="button"
          onPress={() => navigation.navigate(routes.register)}
          style={styles.switch}
        >
          <Text style={styles.switchText}>{t("Create a CafeStory account")}</Text>
        </Pressable>
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
  error: {
    color: colors.danger,
    fontSize: typography.label,
  },
  eyebrow: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "900",
    letterSpacing: 1,
    textTransform: "uppercase",
  },
  form: {
    gap: spacing.lg,
    marginTop: spacing.xxl,
  },
  header: {
    gap: spacing.sm,
    marginTop: spacing.xxl,
  },
  title: {
    color: colors.espresso,
    fontSize: typography.heading,
    fontWeight: "900",
  },
  switch: {
    alignItems: "center",
    paddingVertical: spacing.sm,
  },
  switchText: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "800",
  },
});
