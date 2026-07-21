import * as ImagePicker from "expo-image-picker";
import { Check, ChevronDown, ImagePlus, MapPin, X } from "lucide-react-native";
import { useEffect, useMemo, useState } from "react";
import {
  Image,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  View,
} from "react-native";

import {
  createAdCampaign,
  getRegionCities,
  getRegionProvinces,
  getRegionWards,
  uploadAdImageToCloudinary,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  AdTargetRegionRequest,
  CafePageResponse,
  PaymentResponse,
  RegionCityResponse,
  RegionProvinceResponse,
  RegionWardResponse,
} from "../../types";
import { Button } from "../ui/button";
import { TextField } from "../ui/text-field";

type AdCampaignFormProps = {
  cafePages: CafePageResponse[];
  initialPaymentId?: string;
  onCreated: () => Promise<void> | void;
  payments: PaymentResponse[];
};

type PickerOption = { id: string; label: string };
type PickerState =
  | { kind: "cafe"; options: PickerOption[] }
  | { kind: "payment"; options: PickerOption[] }
  | { kind: "province"; options: PickerOption[] }
  | { kind: "city"; options: PickerOption[] }
  | { kind: "ward"; options: PickerOption[] }
  | null;

function isHttpUrl(value: string) {
  return /^https?:\/\/[^\s]+$/i.test(value);
}

function paymentLabel(payment: PaymentResponse) {
  const amount = Number(payment.amount ?? 0).toLocaleString("en-US");
  return `${amount} VND · ${payment.paymentId.slice(0, 8)}`;
}

export function AdCampaignForm({ cafePages, initialPaymentId, onCreated, payments }: AdCampaignFormProps) {
  const [cafePageId, setCafePageId] = useState(cafePages[0]?.id ?? "");
  const [paymentId, setPaymentId] = useState(
    payments.some((payment) => payment.paymentId === initialPaymentId)
      ? initialPaymentId ?? ""
      : payments[0]?.paymentId ?? "",
  );
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [targetUrl, setTargetUrl] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [activateNow, setActivateNow] = useState(true);
  const [targets, setTargets] = useState<AdTargetRegionRequest[]>([]);
  const [provinces, setProvinces] = useState<RegionProvinceResponse[]>([]);
  const [cities, setCities] = useState<RegionCityResponse[]>([]);
  const [wards, setWards] = useState<RegionWardResponse[]>([]);
  const [provinceCode, setProvinceCode] = useState("");
  const [cityCode, setCityCode] = useState("");
  const [wardCode, setWardCode] = useState("");
  const [picker, setPicker] = useState<PickerState>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void getRegionProvinces().then(setProvinces).catch(() => setProvinces([]));
  }, []);

  useEffect(() => {
    if (!provinceCode) {
      setCities([]);
      return;
    }
    void getRegionCities(provinceCode).then(setCities).catch(() => setCities([]));
  }, [provinceCode]);

  useEffect(() => {
    if (!cityCode) {
      setWards([]);
      return;
    }
    void getRegionWards({ cityCode, provinceCode }).then(setWards).catch(() => setWards([]));
  }, [cityCode, provinceCode]);

  useEffect(() => {
    if (!payments.some((payment) => payment.paymentId === paymentId)) {
      setPaymentId(payments[0]?.paymentId ?? "");
    }
  }, [paymentId, payments]);

  const selectedCafe = cafePages.find((page) => page.id === cafePageId);
  const selectedPayment = payments.find((payment) => payment.paymentId === paymentId);
  const selectedProvince = provinces.find((province) => province.provinceCode === provinceCode);
  const selectedCity = cities.find((city) => city.cityCode === cityCode);
  const selectedWard = wards.find((ward) => ward.wardCode === wardCode);

  const pickerValue = useMemo(() => {
    if (!picker) return "";
    if (picker.kind === "cafe") return cafePageId;
    if (picker.kind === "payment") return paymentId;
    if (picker.kind === "province") return provinceCode;
    if (picker.kind === "city") return cityCode;
    return wardCode;
  }, [cafePageId, cityCode, paymentId, picker, provinceCode, wardCode]);

  function selectOption(id: string) {
    if (!picker) return;
    if (picker.kind === "cafe") setCafePageId(id);
    if (picker.kind === "payment") setPaymentId(id);
    if (picker.kind === "province") {
      setProvinceCode(id);
      setCityCode("");
      setWardCode("");
    }
    if (picker.kind === "city") {
      setCityCode(id);
      setWardCode("");
    }
    if (picker.kind === "ward") setWardCode(id);
    setPicker(null);
  }

  async function chooseImage() {
    setError(null);
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      setError("Photo library permission is required to select an ad image.");
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      allowsEditing: true,
      aspect: [1, 1],
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.85,
    });
    if (result.canceled || !result.assets[0]) return;
    const asset = result.assets[0];
    setIsUploading(true);
    try {
      setImageUrl(
        await uploadAdImageToCloudinary({
          name: asset.fileName,
          type: asset.mimeType,
          uri: asset.uri,
        }),
      );
    } catch (uploadError) {
      setError(uploadError instanceof Error ? uploadError.message : "Unable to upload ad image.");
    } finally {
      setIsUploading(false);
    }
  }

  function addTarget() {
    if (!selectedProvince) {
      setError("Choose at least a province before adding a target region.");
      return;
    }
    const target: AdTargetRegionRequest = {
      city: selectedCity?.name ?? null,
      province: selectedProvince.name,
      ward: selectedWard?.name ?? null,
    };
    const key = `${target.province}|${target.city ?? ""}|${target.ward ?? ""}`;
    setTargets((current) =>
      current.some((item) => `${item.province}|${item.city ?? ""}|${item.ward ?? ""}` === key)
        ? current
        : [...current, target],
    );
    setProvinceCode("");
    setCityCode("");
    setWardCode("");
    setError(null);
  }

  async function submit() {
    const normalizedTitle = title.trim();
    const normalizedDescription = description.trim();
    const normalizedTargetUrl = targetUrl.trim();
    if (!cafePageId || !paymentId) {
      setError("Choose a cafe page and an unused paid Ads payment.");
      return;
    }
    if (!normalizedTitle || normalizedTitle.length > 160) {
      setError("Title must contain 1 to 160 characters.");
      return;
    }
    if (normalizedDescription.length > 1000) {
      setError("Description cannot exceed 1,000 characters.");
      return;
    }
    if (normalizedTargetUrl && !isHttpUrl(normalizedTargetUrl)) {
      setError("Target URL must start with http:// or https://.");
      return;
    }
    if (imageUrl && !isHttpUrl(imageUrl)) {
      setError("Creative image URL must use http:// or https://.");
      return;
    }
    setIsSubmitting(true);
    setError(null);
    try {
      await createAdCampaign({
        activateNow,
        cafePageId,
        description: normalizedDescription || null,
        imageUrl: imageUrl || null,
        paymentId,
        targetRegions: targets,
        targetUrl: normalizedTargetUrl || null,
        title: normalizedTitle,
      });
      setTitle("");
      setDescription("");
      setTargetUrl("");
      setImageUrl("");
      setTargets([]);
      await onCreated();
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "Unable to create campaign.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <View style={styles.card}>
      <View style={styles.heading}>
        <Text style={styles.eyebrow}>NEW CAMPAIGN</Text>
        <Text style={styles.title}>Turn a paid Ads package into a campaign</Text>
        <Text style={styles.description}>Each payment can be used once. Leave targeting empty for nationwide delivery.</Text>
      </View>

      <Selector
        label="Cafe page"
        onPress={() => setPicker({ kind: "cafe", options: cafePages.map((page) => ({ id: page.id, label: page.name || "Unnamed cafe" })) })}
        value={selectedCafe?.name || "Choose a cafe page"}
      />
      <Selector
        label="Paid Ads package"
        onPress={() => setPicker({ kind: "payment", options: payments.map((payment) => ({ id: payment.paymentId, label: paymentLabel(payment) })) })}
        value={selectedPayment ? paymentLabel(selectedPayment) : "Choose an unused payment"}
      />
      <TextField label="Title" maxLength={160} onChangeText={setTitle} placeholder="Summer coffee discovery" value={title} />
      <TextField
        label="Description"
        maxLength={1000}
        multiline
        onChangeText={setDescription}
        placeholder="Tell CafeStory members what makes this promotion useful."
        style={styles.multiline}
        textAlignVertical="top"
        value={description}
      />
      <TextField
        autoCapitalize="none"
        keyboardType="url"
        label="Target URL (optional)"
        onChangeText={setTargetUrl}
        placeholder="https://your-cafe.example/menu"
        value={targetUrl}
      />

      <View style={styles.creative}>
        {imageUrl ? <Image source={{ uri: imageUrl }} style={styles.preview} /> : <ImagePlus color={colors.secondary} size={30} />}
        <View style={styles.creativeCopy}>
          <Text style={styles.fieldLabel}>CREATIVE IMAGE</Text>
          <Text style={styles.helper}>Use a square 1:1 image for a consistent feed card.</Text>
        </View>
        <Button isLoading={isUploading} label={imageUrl ? "Replace" : "Upload"} onPress={chooseImage} variant="outlined" />
      </View>

      <View style={styles.targetBox}>
        <View style={styles.targetHeading}>
          <MapPin color={colors.primary} size={20} />
          <View style={styles.targetCopy}>
            <Text style={styles.fieldLabel}>TARGET REGIONS</Text>
            <Text style={styles.helper}>Add multiple areas or keep this nationwide.</Text>
          </View>
        </View>
        <Selector
          label="Province"
          onPress={() => setPicker({ kind: "province", options: provinces.map((province) => ({ id: province.provinceCode, label: province.name })) })}
          value={selectedProvince?.name || "Choose province"}
        />
        <Selector
          disabled={!provinceCode}
          label="City (optional)"
          onPress={() => setPicker({ kind: "city", options: cities.map((city) => ({ id: city.cityCode, label: city.name })) })}
          value={selectedCity?.name || "All cities"}
        />
        <Selector
          disabled={!cityCode}
          label="Ward (optional)"
          onPress={() => setPicker({ kind: "ward", options: wards.map((ward) => ({ id: ward.wardCode, label: ward.name })) })}
          value={selectedWard?.name || "All wards"}
        />
        <Button label="Add target region" onPress={addTarget} variant="outlined" />
        {targets.length ? (
          <View style={styles.chips}>
            {targets.map((target, index) => (
              <Pressable
                accessibilityLabel={`Remove ${target.province} target`}
                key={`${target.province}-${target.city}-${target.ward}`}
                onPress={() => setTargets((current) => current.filter((_, itemIndex) => itemIndex !== index))}
                style={styles.chip}
              >
                <Text style={styles.chipText}>{[target.province, target.city, target.ward].filter(Boolean).join(" · ")}</Text>
                <X color={colors.primary} size={14} />
              </Pressable>
            ))}
          </View>
        ) : (
          <Text style={styles.nationwide}>Nationwide delivery</Text>
        )}
      </View>

      <View style={styles.switchRow}>
        <View style={styles.switchCopy}>
          <Text style={styles.switchTitle}>Activate immediately</Text>
          <Text style={styles.helper}>Turn this off to save the campaign as a draft.</Text>
        </View>
        <Switch onValueChange={setActivateNow} thumbColor={colors.white} trackColor={{ false: colors.border, true: colors.primary }} value={activateNow} />
      </View>

      {error ? <Text style={styles.error}>{error}</Text> : null}
      <Button disabled={isUploading} isLoading={isSubmitting} label={activateNow ? "Create and activate" : "Save draft"} onPress={submit} />

      <OptionPicker onClose={() => setPicker(null)} onSelect={selectOption} options={picker?.options ?? []} selectedId={pickerValue} title={picker ? `Choose ${picker.kind}` : "Choose option"} visible={Boolean(picker)} />
    </View>
  );
}

function Selector({ disabled = false, label, onPress, value }: { disabled?: boolean; label: string; onPress: () => void; value: string }) {
  return (
    <View style={styles.selectorGroup}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <Pressable disabled={disabled} onPress={onPress} style={[styles.selector, disabled && styles.disabled]}>
        <Text numberOfLines={1} style={styles.selectorText}>{value}</Text>
        <ChevronDown color={colors.muted} size={18} />
      </Pressable>
    </View>
  );
}

function OptionPicker({ onClose, onSelect, options, selectedId, title, visible }: { onClose: () => void; onSelect: (id: string) => void; options: PickerOption[]; selectedId: string; title: string; visible: boolean }) {
  return (
    <Modal animationType="slide" onRequestClose={onClose} transparent visible={visible}>
      <Pressable onPress={onClose} style={styles.backdrop}>
        <Pressable style={styles.sheet}>
          <View style={styles.sheetHeader}>
            <Text style={styles.sheetTitle}>{title}</Text>
            <Pressable accessibilityLabel="Close options" onPress={onClose}><X color={colors.foreground} size={22} /></Pressable>
          </View>
          <ScrollView contentContainerStyle={styles.options}>
            {options.length ? options.map((option) => (
              <Pressable key={option.id} onPress={() => onSelect(option.id)} style={styles.option}>
                <Text style={styles.optionText}>{option.label}</Text>
                {selectedId === option.id ? <Check color={colors.primary} size={20} /> : null}
              </Pressable>
            )) : <Text style={styles.emptyOptions}>No options are available.</Text>}
          </ScrollView>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: { backgroundColor: "rgba(33,29,28,0.42)", flex: 1, justifyContent: "flex-end" },
  card: { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: 12, borderWidth: 1, gap: spacing.lg, padding: spacing.lg },
  chip: { alignItems: "center", backgroundColor: colors.primarySoft, borderRadius: 999, flexDirection: "row", gap: spacing.xs, paddingHorizontal: spacing.md, paddingVertical: spacing.sm },
  chipText: { color: colors.primary, fontSize: 11, fontWeight: "800" },
  chips: { flexDirection: "row", flexWrap: "wrap", gap: spacing.sm },
  creative: { alignItems: "center", backgroundColor: colors.surfaceMuted, borderRadius: 12, flexDirection: "row", gap: spacing.md, padding: spacing.md },
  creativeCopy: { flex: 1, gap: spacing.xs },
  description: { color: colors.muted, fontSize: typography.caption, lineHeight: 19 },
  disabled: { opacity: 0.5 },
  emptyOptions: { color: colors.muted, paddingVertical: spacing.xl, textAlign: "center" },
  error: { color: colors.danger, fontSize: typography.caption, fontWeight: "700", lineHeight: 18 },
  eyebrow: { color: colors.secondary, fontSize: 10, fontWeight: "900", letterSpacing: 1.2 },
  fieldLabel: { color: colors.muted, fontSize: typography.caption, fontWeight: "800", letterSpacing: 0.7, textTransform: "uppercase" },
  heading: { gap: spacing.xs },
  helper: { color: colors.muted, fontSize: 11, lineHeight: 16 },
  multiline: { minHeight: 104, paddingTop: spacing.md },
  nationwide: { color: colors.secondary, fontSize: typography.caption, fontStyle: "italic" },
  option: { alignItems: "center", borderBottomColor: colors.border, borderBottomWidth: 1, flexDirection: "row", justifyContent: "space-between", minHeight: 52, paddingVertical: spacing.md },
  options: { paddingBottom: spacing.xl },
  optionText: { color: colors.foreground, flex: 1, fontSize: typography.label },
  preview: { borderRadius: 8, height: 58, width: 82 },
  selector: { alignItems: "center", borderColor: colors.border, borderRadius: 12, borderWidth: 1, flexDirection: "row", justifyContent: "space-between", minHeight: 52, paddingHorizontal: spacing.lg },
  selectorGroup: { gap: spacing.sm },
  selectorText: { color: colors.foreground, flex: 1, fontSize: typography.label },
  sheet: { backgroundColor: colors.surface, borderTopLeftRadius: 20, borderTopRightRadius: 20, maxHeight: "72%", padding: spacing.xl },
  sheetHeader: { alignItems: "center", flexDirection: "row", justifyContent: "space-between", paddingBottom: spacing.lg },
  sheetTitle: { color: colors.foreground, fontSize: typography.body, fontWeight: "900", textTransform: "capitalize" },
  switchCopy: { flex: 1, gap: spacing.xs },
  switchRow: { alignItems: "center", flexDirection: "row", gap: spacing.lg, justifyContent: "space-between" },
  switchTitle: { color: colors.foreground, fontSize: typography.label, fontWeight: "900" },
  targetBox: { borderColor: colors.border, borderRadius: 12, borderWidth: 1, gap: spacing.md, padding: spacing.md },
  targetCopy: { flex: 1, gap: 2 },
  targetHeading: { alignItems: "center", flexDirection: "row", gap: spacing.sm },
  title: { color: colors.foreground, fontSize: 20, fontWeight: "900", lineHeight: 26 },
});
