import { ArrowLeft, CheckCircle2, MapPin } from "lucide-react-native";
import { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import {
  getRegionCities,
  getRegionProvinces,
  getRegionWards,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  RegionCityResponse,
  RegionProvinceResponse,
  RegionWardResponse,
  UserRegionUpdateRequest,
  UserResponse,
} from "../../types";
import { LoadingState } from "../ui/loading-state";
import { TextField } from "../ui/text-field";

type LocationEditorModalProps = {
  error?: string | null;
  isSaving: boolean;
  onClose: () => void;
  onSave: (request: UserRegionUpdateRequest) => void;
  profile: UserResponse | null;
  visible: boolean;
};

type SelectableRegion =
  | RegionProvinceResponse
  | RegionCityResponse
  | RegionWardResponse;

type SelectionGroupProps<T extends SelectableRegion> = {
  emptyLabel: string;
  isLoading?: boolean;
  label: string;
  onSelect: (option: T) => void;
  options: T[];
  selectedCode?: string | null;
  valueKey: keyof T;
};

export function LocationEditorModal({
  error,
  isSaving,
  onClose,
  onSave,
  profile,
  visible,
}: LocationEditorModalProps) {
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
  const [localError, setLocalError] = useState<string | null>(null);
  const [isProvinceLoading, setIsProvinceLoading] = useState(false);
  const [isCityLoading, setIsCityLoading] = useState(false);
  const [isWardLoading, setIsWardLoading] = useState(false);

  useEffect(() => {
    if (!visible) {
      return;
    }

    setStreet(profile?.regionStreet ?? "");
    setLocalError(null);
    setCities([]);
    setWards([]);
    setSelectedProvince(null);
    setSelectedCity(null);
    setSelectedWard(null);
  }, [profile, visible]);

  useEffect(() => {
    if (!visible) {
      return;
    }

    let isMounted = true;

    async function loadProvinces() {
      setIsProvinceLoading(true);
      setLocalError(null);

      try {
        const response = await getRegionProvinces();

        if (!isMounted) {
          return;
        }

        const existingProvince = response.find(
          (province) =>
            province.provinceCode === profile?.regionProvinceCode
            || province.name === profile?.regionProvince,
        );

        setProvinces(response);
        setSelectedProvince(existingProvince ?? null);
      } catch (requestError) {
        if (isMounted) {
          setLocalError(
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
  }, [
    profile?.regionProvince,
    profile?.regionProvinceCode,
    visible,
  ]);

  useEffect(() => {
    if (!visible || !selectedProvince) {
      setCities([]);
      setSelectedCity(null);
      return;
    }

    const province = selectedProvince;
    let isMounted = true;

    async function loadCities() {
      setIsCityLoading(true);
      setLocalError(null);

      try {
        const response = await getRegionCities(province.provinceCode);

        if (!isMounted) {
          return;
        }

        const shouldUseExistingCity =
          province.provinceCode === profile?.regionProvinceCode
          || province.name === profile?.regionProvince;
        const existingCity = shouldUseExistingCity
          ? response.find(
            (city) =>
              city.cityCode === profile?.regionCityCode
              || city.name === profile?.regionCity,
          )
          : null;

        setCities(response);
        setSelectedCity(existingCity ?? (response.length === 1 ? response[0] : null));
      } catch (requestError) {
        if (isMounted) {
          setLocalError(
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
  }, [
    profile?.regionCity,
    profile?.regionCityCode,
    profile?.regionProvince,
    profile?.regionProvinceCode,
    selectedProvince,
    visible,
  ]);

  useEffect(() => {
    if (!visible || !selectedProvince || !selectedCity) {
      setWards([]);
      setSelectedWard(null);
      return;
    }

    const province = selectedProvince;
    const city = selectedCity;
    let isMounted = true;

    async function loadWards() {
      setIsWardLoading(true);
      setLocalError(null);

      try {
        const response = await getRegionWards({
          cityCode: city.cityCode,
          provinceCode: province.provinceCode,
        });

        if (!isMounted) {
          return;
        }

        const shouldUseExistingWard =
          (city.cityCode === profile?.regionCityCode
            || city.name === profile?.regionCity)
          && (province.provinceCode === profile?.regionProvinceCode
            || province.name === profile?.regionProvince);
        const existingWard = shouldUseExistingWard
          ? response.find(
            (ward) =>
              ward.wardCode === profile?.regionWardCode
              || ward.name === profile?.regionWard,
          )
          : null;

        setWards(response);
        setSelectedWard(existingWard ?? null);
      } catch (requestError) {
        if (isMounted) {
          setLocalError(
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
  }, [
    profile?.regionCity,
    profile?.regionCityCode,
    profile?.regionProvince,
    profile?.regionProvinceCode,
    profile?.regionWard,
    profile?.regionWardCode,
    selectedCity,
    selectedProvince,
    visible,
  ]);

  const canSave = useMemo(
    () =>
      Boolean(
        selectedProvince
          && selectedCity
          && selectedWard
          && street.trim()
          && !isSaving,
      ),
    [isSaving, selectedCity, selectedProvince, selectedWard, street],
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

  function handleSave() {
    if (!selectedProvince || !selectedCity || !selectedWard || !street.trim()) {
      setLocalError("Province, city, ward, and street are required.");
      return;
    }

    setLocalError(null);
    onSave({
      city: selectedCity.name,
      cityCode: selectedCity.cityCode,
      province: selectedProvince.name,
      provinceCode: selectedProvince.provinceCode,
      street: street.trim(),
      ward: selectedWard.name,
      wardCode: selectedWard.wardCode,
    });
  }

  return (
    <Modal animationType="slide" onRequestClose={onClose} visible={visible}>
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        style={styles.modal}
      >
        <View style={styles.header}>
          <Pressable
            accessibilityLabel="Close location editor"
            accessibilityRole="button"
            disabled={isSaving}
            onPress={onClose}
            style={({ pressed }) => [
              styles.headerIconButton,
              pressed && styles.pressed,
              isSaving && styles.disabled,
            ]}
          >
            <ArrowLeft color={colors.foreground} size={32} strokeWidth={2.5} />
          </Pressable>

          <Text numberOfLines={1} style={styles.headerTitle}>
            Update location
          </Text>

          <Pressable
            accessibilityLabel="Save location"
            accessibilityRole="button"
            disabled={!canSave}
            onPress={handleSave}
            style={({ pressed }) => [
              styles.saveButton,
              pressed && styles.pressed,
              !canSave && styles.disabled,
            ]}
          >
            {isSaving ? (
              <ActivityIndicator color={colors.link} />
            ) : (
              <Text style={styles.saveText}>Save</Text>
            )}
          </Pressable>
        </View>

        <ScrollView
          contentContainerStyle={styles.content}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <Text style={styles.description}>
            Keep your profile location accurate so CafeStory can personalize
            local cafe discovery.
          </Text>

          <SelectionGroup
            emptyLabel="No provinces available."
            isLoading={isProvinceLoading}
            label="Province"
            onSelect={handleProvincePress}
            options={provinces}
            selectedCode={selectedProvince?.provinceCode}
            valueKey="provinceCode"
          />

          {selectedProvince ? (
            <SelectionGroup
              emptyLabel="No cities available for this province."
              isLoading={isCityLoading}
              label="City"
              onSelect={handleCityPress}
              options={cities}
              selectedCode={selectedCity?.cityCode}
              valueKey="cityCode"
            />
          ) : null}

          {selectedCity ? (
            <SelectionGroup
              emptyLabel="No wards available for this city."
              isLoading={isWardLoading}
              label="Ward"
              onSelect={setSelectedWard}
              options={wards}
              selectedCode={selectedWard?.wardCode}
              valueKey="wardCode"
            />
          ) : null}

          <TextField
            label="Street"
            onChangeText={setStreet}
            placeholder="Street address"
            value={street}
          />

          {localError || error ? (
            <Text style={styles.errorText}>{localError ?? error}</Text>
          ) : null}
        </ScrollView>
      </KeyboardAvoidingView>
    </Modal>
  );
}

function SelectionGroup<T extends SelectableRegion>({
  emptyLabel,
  isLoading = false,
  label,
  onSelect,
  options,
  selectedCode,
  valueKey,
}: SelectionGroupProps<T>) {
  if (isLoading) {
    return <LoadingState label={`Loading ${label.toLowerCase()}...`} />;
  }

  return (
    <View style={styles.selectionGroup}>
      <Text style={styles.sectionLabel}>{label}</Text>
      {options.length > 0 ? (
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
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
                  <CheckCircle2 color={colors.primary} size={16} strokeWidth={2.5} />
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
    gap: spacing.lg,
    padding: spacing.xl,
    paddingBottom: 56,
  },
  description: {
    color: colors.muted,
    fontSize: typography.body,
    lineHeight: 24,
  },
  disabled: {
    opacity: 0.5,
  },
  emptyText: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 20,
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.label,
    fontWeight: "700",
    lineHeight: 20,
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.white,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    minHeight: 80,
    paddingHorizontal: spacing.lg,
  },
  headerIconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  headerTitle: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "900",
    marginLeft: spacing.md,
  },
  modal: {
    backgroundColor: colors.white,
    flex: 1,
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
  saveButton: {
    alignItems: "center",
    justifyContent: "center",
    minHeight: 44,
    minWidth: 58,
  },
  saveText: {
    color: colors.link,
    fontSize: typography.body,
    fontWeight: "900",
  },
  sectionLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
    letterSpacing: 0.8,
    textTransform: "uppercase",
  },
  selectionGroup: {
    gap: spacing.sm,
  },
});
