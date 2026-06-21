import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { useEffect, useMemo, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { Button, Screen, TextField } from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import { getUsernameSuggestions } from "../../services/api";
import type { AuthStackParamList } from "../../navigation";
import { colors, spacing, typography } from "../../theme";

function localUsernameFallback(value: string) {
  const base = value
    .trim()
    .toLowerCase()
    .replace(/@.*$/, "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, ".")
    .replace(/^\.+|\.+$/g, "");

  if (!base) {
    return [];
  }

  return [
    base,
    `${base}.${Math.floor(100 + Math.random() * 900)}`,
    `${base}_${Math.floor(1000 + Math.random() * 9000)}`,
  ];
}

export function RegisterScreen() {
  const { register } = useAuth();
  const navigation =
    useNavigation<NativeStackNavigationProp<AuthStackParamList>>();
  const [userFullName, setUserFullName] = useState("");
  const [userEmail, setUserEmail] = useState("");
  const [userName, setUserName] = useState("");
  const [usernameSuggestions, setUsernameSuggestions] = useState<string[]>([]);
  const [didEditUsername, setDidEditUsername] = useState(false);
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const suggestionSource = useMemo(
    () => userFullName.trim() || userEmail.trim().replace(/@.*$/, ""),
    [userEmail, userFullName],
  );

  useEffect(() => {
    let isActive = true;

    async function loadSuggestions() {
      if (suggestionSource.length < 2) {
        setUsernameSuggestions([]);
        return;
      }

      try {
        const response = await getUsernameSuggestions(suggestionSource);
        const suggestions = response.suggestions?.length
          ? response.suggestions
          : localUsernameFallback(suggestionSource);

        if (!isActive) {
          return;
        }

        setUsernameSuggestions(suggestions.slice(0, 3));
        if (!didEditUsername && !userName.trim() && suggestions[0]) {
          setUserName(suggestions[0]);
        }
      } catch {
        const suggestions = localUsernameFallback(suggestionSource);

        if (!isActive) {
          return;
        }

        setUsernameSuggestions(suggestions);
        if (!didEditUsername && !userName.trim() && suggestions[0]) {
          setUserName(suggestions[0]);
        }
      }
    }

    const timeoutId = setTimeout(() => {
      void loadSuggestions();
    }, 320);

    return () => {
      isActive = false;
      clearTimeout(timeoutId);
    };
  }, [didEditUsername, suggestionSource, userName]);

  async function handleRegister() {
    setError("");

    if (!userEmail.trim() || !userName.trim() || !password) {
      setError("Email, username, and password are required.");
      return;
    }

    setIsSubmitting(true);

    try {
      await register({
        password,
        userEmail: userEmail.trim(),
        userFullName: userFullName.trim() || undefined,
        userName: userName.trim(),
      });
      navigation.navigate(routes.region);
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
          onChangeText={(nextUserName) => {
            setDidEditUsername(true);
            setUserName(nextUserName);
          }}
          placeholder="cafestory_user"
          value={userName}
        />
        {usernameSuggestions.length ? (
          <View style={styles.suggestionRow}>
            {usernameSuggestions.map((suggestion) => (
              <Pressable
                accessibilityLabel={`Use username ${suggestion}`}
                accessibilityRole="button"
                key={suggestion}
                onPress={() => {
                  setDidEditUsername(true);
                  setUserName(suggestion);
                }}
                style={({ pressed }) => [
                  styles.suggestionChip,
                  userName === suggestion && styles.suggestionChipActive,
                  pressed && styles.pressed,
                ]}
              >
                <Text
                  style={[
                    styles.suggestionText,
                    userName === suggestion && styles.suggestionTextActive,
                  ]}
                >
                  {suggestion}
                </Text>
              </Pressable>
            ))}
          </View>
        ) : null}
        <TextField
          label="Password"
          onChangeText={setPassword}
          placeholder="Create a password"
          secureTextEntry
          value={password}
        />
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <Button
          disabled={
            isSubmitting || !userEmail.trim() || !userName.trim() || !password
          }
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
  pressed: {
    opacity: 0.72,
  },
  suggestionChip: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 999,
    borderWidth: 1,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  suggestionChipActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  suggestionRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm,
    marginTop: -spacing.sm,
  },
  suggestionText: {
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  suggestionTextActive: {
    color: colors.white,
  },
  switchText: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "800",
  },
});
