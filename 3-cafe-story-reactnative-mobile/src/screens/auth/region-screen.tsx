import { useNavigation } from "@react-navigation/native";
import { Pressable } from "../../features/i18n/localized-native";
import { Text } from "../../features/i18n/localized-native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { CheckCircle2, MapPin } from "lucide-react-native";
import { useEffect, useMemo, useState } from "react";
import { ScrollView, StyleSheet, View } from "react-native";

import { Button, LoadingState, Screen, TextField } from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { AuthStackParamList } from "../../navigation";
import {
  getRegionCities,
  getRegionProvinces,
  getRegionWards,
  updateMyRegion,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  RegionCityResponse,
  RegionProvinceResponse,
  RegionWardResponse,
} from "../../types";

export function RegionScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<AuthStackParamList>>();
  const { refreshCurrentUser } = useAuth();
  const [provinces, setProvinces] = useState<RegionProvinceResponse[]>([]);
  const [cities, setCities] = useState<RegionCityResponse[]>([]);
  const [wards, setWards] = useState<RegionWardResponse[]>([]);
  const [selectedProvince, setSelectedProvince] =
    useState<RegionProvinceResponse | null>(null);
  const [selectedCity, setSelectedCity] = useState<RegionCityResponse | null>(
    null,
  );
  const [selectedWard, setSelectedWard] = useState<RegionWardResponse | null>(
    null,
  );
  const [street, setStreet] = useState("");
  const [error, setError] = useState("");
  const [isProvinceLoading, setIsProvinceLoading] = useState(true);
  const [isCityLoading, setIsCityLoading] = useState(false);
  const [isWardLoading, setIsWardLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadProvinces() {
      setIsProvinceLoading(true);
      setError("");

      try {
        const response = await getRegionProvinces();

        if (isMounted) {
          setProvinces(response);
        }
      } catch (requestError) {
        if (isMounted) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : "Unable to load provinces.",
          );
        }
      } finally {
        if (isMounted) {
          setIsProvinceLoading(false);
        }
      }
    }

    void loadProvinces();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;

    async function loadCities() {
      if (!selectedProvince) {
        setCities([]);
        setSelectedCity(null);
        return;
      }

      setIsCityLoading(true);
      setError("");

      try {
        const response = await getRegionCities(selectedProvince.provinceCode);

        if (isMounted) {
          setCities(response);
          setSelectedCity(response.length === 1 ? response[0] : null);
        }
      } catch (requestError) {
        if (isMounted) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : "Unable to load cities.",
          );
        }
      } finally {
        if (isMounted) {
          setIsCityLoading(false);
        }
      }
    }

    void loadCities();

    return () => {
      isMounted = false;
    };
  }, [selectedProvince]);

  useEffect(() => {
    let isMounted = true;

    async function loadWards() {
      if (!selectedProvince || !selectedCity) {
        setWards([]);
        setSelectedWard(null);
        return;
      }

      setIsWardLoading(true);
      setError("");

      try {
        const response = await getRegionWards({
          cityCode: selectedCity.cityCode,
          provinceCode: selectedProvince.provinceCode,
        });

        if (isMounted) {
          setWards(response);
          setSelectedWard(null);
        }
      } catch (requestError) {
        if (isMounted) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : "Unable to load wards.",
          );
        }
      } finally {
        if (isMounted) {
          setIsWardLoading(false);
        }
      }
    }

    void loadWards();

    return () => {
      isMounted = false;
    };
  }, [selectedCity, selectedProvince]);

  const canContinue = useMemo(
    () =>
      Boolean(
        selectedProvince
          && selectedCity
          && selectedWard
          && street.trim()
          && !isSubmitting,
      ),
    [isSubmitting, selectedCity, selectedProvince, selectedWard, street],
  );

  function handleProvincePress(province: RegionProvinceResponse) {
    setSelectedProvince(province);
    setSelectedCity(null);
    setSelectedWard(null);
    setCities([]);
    setWards([]);
  }

  function handleCityPress(city: RegionCityResponse) {
    setSelectedCity(city);
    setSelectedWard(null);
    setWards([]);
  }

  async function handleContinue() {
    if (!selectedProvince || !selectedCity || !selectedWard || !street.trim()) {
      setError("Province, city, ward, and street are required.");
      return;
    }

    setIsSubmitting(true);
    setError("");

    try {
      await updateMyRegion({
        city: selectedCity.name,
        cityCode: selectedCity.cityCode,
        province: selectedProvince.name,
        provinceCode: selectedProvince.provinceCode,
        street: street.trim(),
        ward: selectedWard.name,
        wardCode: selectedWard.wardCode,
      });
      await refreshCurrentUser();
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to save your region.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Screen>
      <ScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.header}>
          <Text style={styles.eyebrow}>CafeStory</Text>
          <Text style={styles.title}>Choose your region</Text>
          <Text style={styles.description}>
            Tell us where you usually explore cafes so your feed can feel closer
            to home.
          </Text>
        </View>

        <View style={styles.form}>
          {isProvinceLoading ? (
            <LoadingState label="Loading provinces..." />
          ) : (
            <SelectionGroup
              emptyLabel="No provinces available."
              label="Province"
              onSelect={handleProvincePress}
              options={provinces}
              selectedCode={selectedProvince?.provinceCode}
              valueKey="provinceCode"
            />
          )}

          {selectedProvince ? (
            isCityLoading ? (
              <LoadingState label="Loading cities..." />
            ) : (
              <SelectionGroup
                emptyLabel="No cities available for this province."
                label="City"
                onSelect={handleCityPress}
                options={cities}
                selectedCode={selectedCity?.cityCode}
                valueKey="cityCode"
              />
            )
          ) : null}

          {selectedCity ? (
            isWardLoading ? (
              <LoadingState label="Loading wards..." />
            ) : (
              <SelectionGroup
                emptyLabel="No wards available for this city."
                label="Ward"
                onSelect={setSelectedWard}
                options={wards}
                selectedCode={selectedWard?.wardCode}
                valueKey="wardCode"
              />
            )
          ) : null}

          <TextField
            label="Street"
            onChangeText={setStreet}
            placeholder="Nguyen Hue Street"
            value={street}
          />

          {error ? <Text style={styles.error}>{error}</Text> : null}

          <Button
            disabled={!canContinue}
            isLoading={isSubmitting}
            label="Continue"
            onPress={handleContinue}
          />

          <Pressable
            accessibilityRole="button"
            onPress={() => navigation.navigate(routes.login)}
            style={styles.switch}
          >
            <Text style={styles.switchText}>Back to sign in</Text>
          </Pressable>
        </View>
      </ScrollView>
    </Screen>
  );
}

type SelectableRegion =
  | RegionProvinceResponse
  | RegionCityResponse
  | RegionWardResponse;

type SelectionGroupProps<T extends SelectableRegion> = {
  emptyLabel: string;
  label: string;
  onSelect: (option: T) => void;
  options: T[];
  selectedCode?: string;
  valueKey: keyof T;
};

function SelectionGroup<T extends SelectableRegion>({
  emptyLabel,
  label,
  onSelect,
  options,
  selectedCode,
  valueKey,
}: SelectionGroupProps<T>) {
  return (
    <View style={styles.selectionGroup}>
      <Text style={styles.fieldLabel}>{label}</Text>
      {options.length > 0 ? (
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          style={styles.optionScroll}
        >
          {options.map((option) => {
            const code = String(option[valueKey]);
            const isSelected = code === selectedCode;

            return (
              <Pressable
                accessibilityLabel={`Select ${option.name}`}
                accessibilityRole="button"
                key={code}
                onPress={() => onSelect(option)}
                style={({ pressed }) => [
                  styles.option,
                  isSelected && styles.optionSelected,
                  pressed && styles.pressed,
                ]}
              >
                {isSelected ? (
                  <CheckCircle2
                    color={colors.primary}
                    size={16}
                    strokeWidth={2.5}
                  />
                ) : (
                  <MapPin color={colors.muted} size={16} strokeWidth={2.2} />
                )}
                <Text
                  numberOfLines={1}
                  style={[
                    styles.optionText,
                    isSelected && styles.optionTextSelected,
                  ]}
                >
                  {option.name}
                </Text>
              </Pressable>
            );
          })}
        </ScrollView>
      ) : (
        <Text style={styles.emptyText}>{emptyLabel}</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingBottom: spacing.xxl,
  },
  description: {
    color: colors.muted,
    fontSize: typography.body,
    lineHeight: 24,
  },
  emptyText: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 20,
  },
  error: {
    color: colors.danger,
    fontSize: typography.label,
    lineHeight: 20,
  },
  eyebrow: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "900",
    letterSpacing: 1,
    textTransform: "uppercase",
  },
  fieldLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
    letterSpacing: 0.8,
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
  option: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.xs,
    marginRight: spacing.sm,
    maxWidth: 220,
    minHeight: 42,
    paddingHorizontal: spacing.md,
  },
  optionScroll: {
    marginRight: -spacing.xl,
  },
  optionSelected: {
    backgroundColor: colors.primarySoft,
    borderColor: colors.primary,
  },
  optionText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "700",
  },
  optionTextSelected: {
    color: colors.primary,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.72,
  },
  selectionGroup: {
    gap: spacing.sm,
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
