import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { Button, Screen, TextField } from "../../components";
import { routes } from "../../navigation";
import type { AuthStackParamList } from "../../navigation";
import { colors, spacing, typography } from "../../theme";

export function RegionScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<AuthStackParamList>>();
  const [city, setCity] = useState("");
  const [province, setProvince] = useState("");
  const [area, setArea] = useState("");

  return (
    <Screen>
      <View style={styles.header}>
        <Text style={styles.eyebrow}>CafeStory</Text>
        <Text style={styles.title}>Choose your region</Text>
        <Text style={styles.description}>
          Tell us where you usually explore cafes so your feed can feel closer
          to home.
        </Text>
      </View>

      <View style={styles.form}>
        <TextField
          label="City"
          onChangeText={setCity}
          placeholder="Ho Chi Minh City"
          value={city}
        />
        <TextField
          label="Province"
          onChangeText={setProvince}
          placeholder="Ho Chi Minh City"
          value={province}
        />
        <TextField
          label="Area"
          onChangeText={setArea}
          placeholder="District 1"
          value={area}
        />

        <Button
          label="Continue"
          onPress={() => navigation.navigate(routes.register)}
        />

        <Pressable
          accessibilityRole="button"
          onPress={() => navigation.navigate(routes.login)}
          style={styles.switch}
        >
          <Text style={styles.switchText}>Back to sign in</Text>
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
  switch: {
    alignItems: "center",
    paddingVertical: spacing.sm,
  },
  switchText: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "800",
  },
  title: {
    color: colors.espresso,
    fontSize: typography.heading,
    fontWeight: "900",
  },
});
