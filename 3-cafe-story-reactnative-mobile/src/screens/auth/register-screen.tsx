import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { Button, Screen, TextField } from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { AuthStackParamList } from "../../navigation";
import { colors, spacing, typography } from "../../theme";

export function RegisterScreen() {
  const { register } = useAuth();
  const navigation =
    useNavigation<NativeStackNavigationProp<AuthStackParamList>>();
  const [userFullName, setUserFullName] = useState("");
  const [userEmail, setUserEmail] = useState("");
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleRegister() {
    setError("");
    setIsSubmitting(true);

    try {
      await register({
        password,
        userEmail,
        userFullName,
        userName,
      });
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to create account.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Screen>
      <View style={styles.header}>
        <Text style={styles.eyebrow}>CafeStory</Text>
        <Text style={styles.title}>Create account</Text>
        <Text style={styles.description}>
          Start saving cafe notes, reviews, and places worth returning to.
        </Text>
      </View>

      <View style={styles.form}>
        <TextField
          label="Full name"
          onChangeText={setUserFullName}
          placeholder="Gia Thinh"
          value={userFullName}
        />
        <TextField
          autoCapitalize="none"
          keyboardType="email-address"
          label="Email"
          onChangeText={setUserEmail}
          placeholder="hello@cafestory.com"
          value={userEmail}
        />
        <TextField
          autoCapitalize="none"
          label="Username"
          onChangeText={setUserName}
          placeholder="cafestory_user"
          value={userName}
        />
        <TextField
          label="Password"
          onChangeText={setPassword}
          placeholder="Create a password"
          secureTextEntry
          value={password}
        />
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <Button
          disabled={!userEmail || !userName || !password}
          isLoading={isSubmitting}
          label="Create account"
          onPress={handleRegister}
        />
        <Pressable
          accessibilityRole="button"
          onPress={() => navigation.navigate(routes.login)}
          style={styles.switch}
        >
          <Text style={styles.switchText}>I already have an account</Text>
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
    marginTop: spacing.xl,
  },
  header: {
    gap: spacing.sm,
    marginTop: spacing.xl,
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
