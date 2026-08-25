import { ArrowLeft, Camera, CheckCircle2, Image as ImageIcon, MapPin } from "lucide-react-native";
import { Text } from "react-native";
import { Pressable, TextInput } from "react-native";
import { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator, Image, KeyboardAvoidingView, Modal, Platform, ScrollView, StyleSheet, View } from "react-native";

import {
  createRegion,
  getRegionCities,
  getRegionProvinces,
  getRegionWards,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  CafePageResponse,
  CafePageUpdateRequest,
  RegionCityResponse,
  RegionProvinceResponse,
  RegionWardResponse,
} from "../../types";
import { Avatar } from "../ui/avatar";
import { LoadingState } from "../ui/loading-state";
import { TextField } from "../ui/text-field";
import { t } from "../../features/i18n";

const DESCRIPTION_MAX_LENGTH = 300;

type EditCafePageModalProps = {
  cafePage: CafePageResponse | null;
  error?: string | null;
  isSaving: boolean;
  isUploadingAvatar?: boolean;
  isUploadingCover?: boolean;
  onAvatarPress?: () => void;
  onClose: () => void;
  onCoverPress?: () => void;
  onSave: (request: CafePageUpdateRequest) => void;
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

export function EditCafePageModal({
  cafePage,
  error,
  isSaving,
  isUploadingAvatar = false,
  isUploadingCover = false,
  onAvatarPress,
  onClose,
  onCoverPress,
  onSave,
  visible,
}: EditCafePageModalProps) {
  const [name, setName] = useState("");
  const [address, setAddress] = useState("");
  const [description, setDescription] = useState("");
  const [provinces, setProvinces] = useState<RegionProvinceResponse[]>([]);
  const [cities, setCities] = useState<RegionCityResponse[]>([]);
  const [wards, setWards] = useState<RegionWardResponse[]>([]);
  const [selectedProvince, setSelectedProvince] =
    useState<RegionProvinceResponse | null>(null);
  const [selectedCity, setSelectedCity] = useState<RegionCityResponse | null>(null);
  const [selectedWard, setSelectedWard] = useState<RegionWardResponse | null>(null);
  const [isProvinceLoading, setIsProvinceLoading] = useState(false);
  const [isCityLoading, setIsCityLoading] = useState(false);
  const [isWardLoading, setIsWardLoading] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);

  useEffect(() => {
    if (!visible) {
      return;
    }

    setName(cafePage?.name ?? "");
    setAddress(cafePage?.address ?? "");
    setDescription(cafePage?.description ?? "");
    setCities([]);
    setWards([]);
    setSelectedProvince(null);
    setSelectedCity(null);
    setSelectedWard(null);
    setLocalError(null);
  }, [cafePage, visible]);

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
            province.provinceCode === cafePage?.regionProvinceCode
            || province.name === cafePage?.regionProvince,
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
    cafePage?.regionProvince,
    cafePage?.regionProvinceCode,
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
          province.provinceCode === cafePage?.regionProvinceCode
          || province.name === cafePage?.regionProvince;
        const existingCity = shouldUseExistingCity
          ? response.find(
            (city) =>
              city.cityCode === cafePage?.regionCityCode
              || city.name === cafePage?.regionCity,
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
    cafePage?.regionCity,
    cafePage?.regionCityCode,
    cafePage?.regionProvince,
    cafePage?.regionProvinceCode,
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
          (city.cityCode === cafePage?.regionCityCode
            || city.name === cafePage?.regionCity)
          && (province.provinceCode === cafePage?.regionProvinceCode
            || province.name === cafePage?.regionProvince);
        const existingWard = shouldUseExistingWard
          ? response.find(
            (ward) =>
              ward.wardCode === cafePage?.regionWardCode
              || ward.name === cafePage?.regionWard,
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
    cafePage?.regionCity,
    cafePage?.regionCityCode,
    cafePage?.regionProvince,
    cafePage?.regionProvinceCode,
    cafePage?.regionWard,
    cafePage?.regionWardCode,
    selectedCity,
    selectedProvince,
    visible,
  ]);

  const canSave = useMemo(
    () => Boolean(name.trim() && !isSaving && !isUploadingAvatar && !isUploadingCover),
    [isSaving, isUploadingAvatar, isUploadingCover, name],
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
    if (!name.trim()) {
      setLocalError("Cafe name is required.");
      return;
    }

    setLocalError(null);
    let regionId = cafePage?.regionId ?? null;

    try {
      if (selectedProvince && selectedCity && selectedWard) {
        if (!address.trim()) {
          setLocalError("Address is required when updating cafe location.");
          return;
        }

        const nextRegion = await createRegion(
          {
            city: selectedCity.name,
            cityCode: selectedCity.cityCode,
            province: selectedProvince.name,
            provinceCode: selectedProvince.provinceCode,
            street: address.trim(),
            ward: selectedWard.name,
            wardCode: selectedWard.wardCode,
          },
          "FULL_ADDRESS",
        );
        regionId = nextRegion.regionId;
      }
    } catch (requestError) {
      setLocalError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to update cafe location.",
      );
      return;
    }

    onSave({
      address: address.trim() || null,
      avatarUrl: cafePage?.avatarUrl ?? null,
      coverUrl: cafePage?.coverUrl ?? null,
      description: description.trim() || null,
      name: name.trim(),
      regionId,
      status: cafePage?.status ?? null,
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
            accessibilityLabel={t("Close edit cafe page")}
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

          <Text numberOfLines={1} style={styles.headerTitle}>{t("Edit page")}</Text>

          <Pressable
            accessibilityLabel={t("Save cafe page")}
            accessibilityRole="button"
            disabled={!canSave}
            onPress={() => {
              void handleSave();
            }}
            style={({ pressed }) => [
              styles.saveButton,
              pressed && styles.pressed,
              !canSave && styles.disabled,
            ]}
          >
            {isSaving ? (
              <ActivityIndicator color={colors.link} />
            ) : (
              <Text style={styles.saveText}>{t("Save")}</Text>
            )}
          </Pressable>
        </View>

        <ScrollView
          contentContainerStyle={styles.content}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <View style={styles.mediaSection}>
            <Pressable
              accessibilityLabel={t("Edit cafe cover image")}
              accessibilityRole="button"
              disabled={isSaving || isUploadingCover}
              onPress={onCoverPress}
              style={({ pressed }) => [
                styles.coverPicker,
                pressed && styles.pressed,
                (isSaving || isUploadingCover) && styles.disabled,
              ]}
            >
              {cafePage?.coverUrl ? (
                <Image resizeMode="cover" source={{ uri: cafePage.coverUrl }} style={styles.coverImage} />
              ) : (
                <View style={styles.coverFallback}>
                  <ImageIcon color={colors.secondaryStrong} size={34} strokeWidth={2.5} />
                  <Text style={styles.coverFallbackText}>{t("Add cover")}</Text>
                </View>
              )}
              <View style={styles.coverOverlay}>
                {isUploadingCover ? (
                  <ActivityIndicator color={colors.white} />
                ) : (
                  <>
                    <Camera color={colors.white} size={18} strokeWidth={2.6} />
                    <Text style={styles.coverOverlayText}>{t("Edit cover")}</Text>
                  </>
                )}
              </View>
            </Pressable>

            <View style={styles.avatarSection}>
              <Pressable
                accessibilityLabel={t("Edit cafe avatar")}
                accessibilityRole="button"
                disabled={isSaving || isUploadingAvatar}
                onPress={onAvatarPress}
                style={({ pressed }) => [
                  styles.avatarCamera,
                  pressed && styles.pressed,
                  (isSaving || isUploadingAvatar) && styles.disabled,
                ]}
              >
                {isUploadingAvatar ? (
                  <ActivityIndicator color={colors.foreground} />
                ) : (
                  <Camera color={colors.foreground} size={30} strokeWidth={2.7} />
                )}
              </Pressable>

              <View style={styles.avatarCircle}>
                <Avatar
                  initials={name.slice(0, 2).toUpperCase() || "CS"}
                  size={104}
                  uri={cafePage?.avatarUrl}
                />
              </View>
            </View>
          </View>

          <View style={styles.fields}>
            <TextField label={t("Cafe name")} onChangeText={setName} value={name} />
            <TextField
              label={t("Address")}
              onChangeText={setAddress}
              placeholder={t("Street or display address")}
              value={address}
            />

            <View style={styles.fieldWrapper}>
              <Text style={styles.fieldLabel}>{t("Description")}</Text>
              <TextInput
                maxLength={DESCRIPTION_MAX_LENGTH}
                multiline
                onChangeText={setDescription}
                placeholder={t("Tell people what makes your cafe special")}
                placeholderTextColor={colors.muted}
                style={styles.descriptionInput}
                textAlignVertical="top"
                value={description}
              />
              <Text style={styles.counterText}>
                {description.length}/{DESCRIPTION_MAX_LENGTH}
              </Text>
            </View>
          </View>

          <View style={styles.regionSection}>
            <Text style={styles.regionTitle}>{t("Location")}</Text>
            <Text style={styles.regionDescription}>{t("Pick province, city, and ward to help people discover this cafe.")}</Text>

            <SelectionGroup
              emptyLabel={t("No provinces available.")}
              isLoading={isProvinceLoading}
              label={t("Province")}
              onSelect={handleProvincePress}
              options={provinces}
              selectedCode={selectedProvince?.provinceCode}
              valueKey="provinceCode"
            />

            {selectedProvince ? (
              <SelectionGroup
                emptyLabel={t("No cities available for this province.")}
                isLoading={isCityLoading}
                label={t("City")}
                onSelect={handleCityPress}
                options={cities}
                selectedCode={selectedCity?.cityCode}
                valueKey="cityCode"
              />
            ) : null}

            {selectedCity ? (
              <SelectionGroup
                emptyLabel={t("No wards available for this city.")}
                isLoading={isWardLoading}
                label={t("Ward")}
                onSelect={setSelectedWard}
                options={wards}
                selectedCode={selectedWard?.wardCode}
                valueKey="wardCode"
              />
            ) : null}
          </View>

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
      <Text style={styles.selectionLabel}>{label}</Text>
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
  avatarCamera: {
    alignItems: "center",
    backgroundColor: colors.secondarySoft,
    borderRadius: 52,
    height: 104,
    justifyContent: "center",
    width: 104,
  },
  avatarCircle: {
    alignItems: "center",
    backgroundColor: colors.secondarySoft,
    borderRadius: 52,
    height: 104,
    justifyContent: "center",
    width: 104,
  },
  avatarSection: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xl,
    justifyContent: "center",
    marginTop: -36,
  },
  content: {
    gap: spacing.lg,
    paddingBottom: 56,
  },
  counterText: {
    alignSelf: "flex-end",
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  coverFallback: {
    alignItems: "center",
    backgroundColor: colors.secondarySoft,
    gap: spacing.sm,
    height: "100%",
    justifyContent: "center",
    width: "100%",
  },
  coverFallbackText: {
    color: colors.secondaryStrong,
    fontSize: typography.label,
    fontWeight: "900",
  },
  coverImage: {
    height: "100%",
    width: "100%",
  },
  coverOverlay: {
    alignItems: "center",
    backgroundColor: "rgba(33, 29, 28, 0.54)",
    borderRadius: 999,
    bottom: spacing.md,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 34,
    paddingHorizontal: spacing.md,
    position: "absolute",
    right: spacing.md,
  },
  coverOverlayText: {
    color: colors.white,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  coverPicker: {
    backgroundColor: colors.surfaceMuted,
    height: 174,
    overflow: "hidden",
    width: "100%",
  },
  descriptionInput: {
    borderColor: colors.border,
    borderRadius: 12,
    borderWidth: 1,
    color: colors.foreground,
    fontSize: typography.body,
    minHeight: 116,
    padding: spacing.lg,
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
    paddingHorizontal: spacing.xl,
  },
  fieldLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
    letterSpacing: 0.8,
    textTransform: "uppercase",
  },
  fieldWrapper: {
    gap: spacing.sm,
  },
  fields: {
    gap: spacing.lg,
    paddingHorizontal: spacing.xl,
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
  mediaSection: {
    paddingBottom: spacing.md,
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
  regionDescription: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 20,
  },
  regionSection: {
    gap: spacing.md,
    paddingHorizontal: spacing.xl,
  },
  regionTitle: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
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
  selectionGroup: {
    gap: spacing.sm,
  },
  selectionLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
    letterSpacing: 0.8,
    textTransform: "uppercase",
  },
});
