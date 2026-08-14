import { ArrowLeft, CheckCircle2, MapPin } from "lucide-react-native";
import { Pressable, Text } from "../../features/i18n/localized-native";
import { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator, Modal, ScrollView, StyleSheet, View } from "react-native";

import {
  createRegion,
  getRegionCities,
  getRegionProvinces,
  getRegionWards,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  CreatePostDraft,
  RegionCityResponse,
  RegionProvinceResponse,
  RegionWardResponse,
} from "../../types";
import { EmptyState } from "../ui/empty-state";
import { LoadingState } from "../ui/loading-state";

type CreatePostLocationPickerModalProps = {
  onApply: (location: NonNullable<CreatePostDraft["location"]>) => void;
  onClose: () => void;
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

export function CreatePostLocationPickerModal({
  onApply,
  onClose,
  visible,
}: CreatePostLocationPickerModalProps) {
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
  const [error, setError] = useState<string | null>(null);
  const [isProvinceLoading, setIsProvinceLoading] = useState(false);
  const [isCityLoading, setIsCityLoading] = useState(false);
  const [isWardLoading, setIsWardLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (!visible) {
      return;
    }

    let isMounted = true;

    async function loadProvinces() {
      setIsProvinceLoading(true);
      setError(null);

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
  }, [visible]);

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
      setError(null);

      try {
        const response = await getRegionCities(province.provinceCode);

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
  }, [selectedProvince, visible]);

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
      setError(null);

      try {
        const response = await getRegionWards({
          cityCode: city.cityCode,
          provinceCode: province.provinceCode,
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
  }, [selectedCity, selectedProvince, visible]);

  const canSave = Boolean(selectedProvince && selectedCity && !isSaving);
  const locationName = useMemo(
    () =>
      [selectedWard?.name, selectedCity?.name, selectedProvince?.name]
        .filter(Boolean)
        .join(", "),
    [selectedCity?.name, selectedProvince?.name, selectedWard?.name],
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

  async function handleSave() {
    if (!selectedProvince || !selectedCity) {
      setError("Province and city are required.");
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      const region = await createRegion(
        {
          city: selectedCity.name,
          cityCode: selectedCity.cityCode,
          province: selectedProvince.name,
          provinceCode: selectedProvince.provinceCode,
          ward: selectedWard?.name,
          wardCode: selectedWard?.wardCode,
        },
        "BLOG_LOCATION",
      );

      onApply({
        name: locationName || region.city || "Selected location",
        regionId: region.regionId,
      });
      onClose();
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to save this location.",
      );
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <Modal animationType="slide" onRequestClose={onClose} visible={visible}>
      <View style={styles.modal}>
        <View style={styles.header}>
          <Pressable
            accessibilityLabel="Close location picker"
            accessibilityRole="button"
            disabled={isSaving}
            onPress={onClose}
            style={({ pressed }) => [
              styles.iconButton,
              pressed && styles.pressed,
              isSaving && styles.disabled,
            ]}
          >
            <ArrowLeft color={colors.foreground} size={30} strokeWidth={2.5} />
          </Pressable>

          <Text numberOfLines={1} style={styles.headerTitle}>
            Select location
          </Text>

          <Pressable
            accessibilityLabel="Apply post location"
            accessibilityRole="button"
            disabled={!canSave}
            onPress={handleSave}
            style={({ pressed }) => [
              styles.doneButton,
              pressed && canSave && styles.pressed,
              !canSave && styles.disabled,
            ]}
          >
            {isSaving ? (
              <ActivityIndicator color={colors.link} />
            ) : (
              <Text style={styles.doneText}>Done</Text>
            )}
          </Pressable>
        </View>

        <ScrollView
          contentContainerStyle={styles.content}
          showsVerticalScrollIndicator={false}
        >
          <Text style={styles.description}>
            Pick a post location. Street is not required for blog posts.
          </Text>

          {isProvinceLoading ? (
            <LoadingState label="Loading provinces..." />
          ) : provinces.length > 0 ? (
            <SelectionGroup
              emptyLabel="No provinces available."
              label="Province"
              onSelect={handleProvincePress}
              options={provinces}
              selectedCode={selectedProvince?.provinceCode}
              valueKey="provinceCode"
            />
          ) : (
            <EmptyState title="No provinces available" />
          )}

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
              label="Ward optional"
              onSelect={setSelectedWard}
              options={wards}
              selectedCode={selectedWard?.wardCode}
              valueKey="wardCode"
            />
          ) : null}

          {locationName ? (
            <View style={styles.selectedBox}>
              <MapPin color={colors.primary} size={20} strokeWidth={2.5} />
              <View style={styles.selectedCopy}>
                <Text style={styles.selectedLabel}>Selected location</Text>
                <Text numberOfLines={2} style={styles.selectedValue}>
                  {locationName}
                </Text>
              </View>
            </View>
          ) : null}

          {error ? <Text style={styles.errorText}>{error}</Text> : null}
        </ScrollView>
      </View>
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
    paddingBottom: 48,
  },
  description: {
    color: colors.muted,
    fontSize: typography.body,
    lineHeight: 24,
  },
  disabled: {
    opacity: 0.5,
  },
  doneButton: {
    alignItems: "center",
    justifyContent: "center",
    minHeight: 44,
    minWidth: 70,
  },
  doneText: {
    color: colors.link,
    fontSize: typography.body,
    fontWeight: "900",
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
    backgroundColor: colors.surface,
    borderBottomColor: colors.border,
    borderBottomWidth: StyleSheet.hairlineWidth,
    flexDirection: "row",
    minHeight: 72,
    paddingHorizontal: spacing.md,
  },
  headerTitle: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "900",
    textAlign: "center",
  },
  iconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 70,
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
  sectionLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
    letterSpacing: 0.8,
    textTransform: "uppercase",
  },
  selectedBox: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderColor: colors.primary,
    borderRadius: 16,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.lg,
  },
  selectedCopy: {
    flex: 1,
    gap: 3,
  },
  selectedLabel: {
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  selectedValue: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
    lineHeight: 22,
  },
  selectionGroup: {
    gap: spacing.sm,
  },
});
