import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { Button, Screen, TextField } from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { AuthStackParamList } from "../../navigation";
import { colors, spacing, typography } from "../../theme";

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
      setError("Email/username and password are required.");
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
      <View style={styles.header}>
        <Text style={styles.eyebrow}>CafeStory</Text>
        <Text style={styles.title}>Welcome back</Text>
        <Text style={styles.description}>
          Sign in to keep collecting cafe stories, saved corners, and reviews.
        </Text>
      </View>

      <View style={styles.form}>
        <TextField
          autoCapitalize="none"
          label="Email or username"
          onChangeText={setIdentifier}
          placeholder="hello@cafestory.com"
          value={identifier}
        />
        <TextField
          label="Password"
          onChangeText={setPassword}
          placeholder="Enter your password"
          secureTextEntry
          value={password}
        />
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <Button
          disabled={isSubmitting || !identifier.trim() || !password}
          isLoading={isSubmitting}
          label="Sign in"
          onPress={handleLogin}
        />
        <Pressable
          accessibilityRole="button"
          onPress={() => navigation.navigate(routes.register)}
          style={styles.switch}
        >
          <Text style={styles.switchText}>Create a CafeStory account</Text>
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
