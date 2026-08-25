import { ArrowLeft, ChevronRight, X } from "lucide-react-native";
import { useEffect, useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { useI18n, type TranslationKey } from "../../features/i18n";
import { createContentReport, getReportReasons } from "../../services/api";
import { ApiError } from "../../services/api/client";
import { colors, spacing, typography } from "../../theme";
import type { ReportReasonResponse, ReportTargetType } from "../../types";

type ReportPostModalProps = {
  blogId: string;
  onClose: () => void;
  visible: boolean;
};

type ReportContentModalProps = {
  onClose: () => void;
  targetId: string;
  targetType: Extract<ReportTargetType, "BLOG" | "COMMENT">;
  visible: boolean;
};

type ReportReasonCopy = {
  descriptionKey?: TranslationKey;
  labelKey: TranslationKey;
};

const REPORT_REASON_COPY: Record<string, ReportReasonCopy> = {
  BULLYING_OR_UNWANTED_CONTACT: {
    labelKey: "report.reason.bullying",
  },
  DISLIKE_CONTENT: {
    labelKey: "report.reason.dislike",
  },
  FALSE_INFORMATION: {
    labelKey: "report.reason.falseInformation",
  },
  INTELLECTUAL_PROPERTY: {
    descriptionKey: "report.reason.intellectualPropertyDescription",
    labelKey: "report.reason.intellectualProperty",
  },
  NUDITY_OR_SEXUAL_ACTIVITY: {
    labelKey: "report.reason.nudity",
  },
  RESTRICTED_GOODS: {
    labelKey: "report.reason.restrictedGoods",
  },
  SCAM_FRAUD_OR_SPAM: {
    labelKey: "report.reason.scam",
  },
  SELF_HARM_OR_ABNORMAL_EATING: {
    labelKey: "report.reason.selfHarm",
  },
  VIOLENCE_HATE_OR_EXPLOITATION: {
    labelKey: "report.reason.violence",
  },
};

function reportReasonLabel(
  reason: ReportReasonResponse,
  locale: "en" | "vi",
  t: (key: TranslationKey) => string,
) {
  const copy = REPORT_REASON_COPY[reason.code];
  if (copy) {
    return t(copy.labelKey);
  }
  return locale === "vi" ? reason.labelVi : reason.code;
}

function reportReasonDescription(
  reason: ReportReasonResponse,
  t: (key: TranslationKey) => string,
) {
  const descriptionKey = REPORT_REASON_COPY[reason.code]?.descriptionKey;
  return t(descriptionKey ?? "report.common.reasonDescription");
}

function reportRequestError(error: unknown, t: (key: TranslationKey) => string) {
  if (error instanceof ApiError) {
    const rawMessage = error.rawMessage?.toLowerCase() ?? "";
    if (error.statusCode === 403 || rawMessage.includes("own")) {
      return t("report.common.submitForbidden");
    }
    if (
      error.statusCode === 409 ||
      rawMessage.includes("already") ||
      rawMessage.includes("duplicate")
    ) {
      return t("report.common.submitDuplicate");
    }
    if (error.statusCode === 401) {
      return t("common.error.sessionExpired");
    }
    if (error.statusCode === 0) {
      return t("common.error.network");
    }
  }
  return t("report.common.submitError");
}

export function ReportContentModal({
  onClose,
  targetId,
  targetType,
  visible,
}: ReportContentModalProps) {
  const { locale, t } = useI18n();
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [isLoadingReasons, setIsLoadingReasons] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [reasons, setReasons] = useState<ReportReasonResponse[]>([]);
  const [selectedReason, setSelectedReason] = useState<ReportReasonResponse | null>(null);
  const [submittedReportId, setSubmittedReportId] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let isActive = true;

    if (!visible) {
      setDescription("");
      setError("");
      setSelectedReason(null);
      setSubmittedReportId(null);
      return;
    }

    setIsLoadingReasons(true);
    setError("");

    getReportReasons(targetType)
      .then((response) => {
        if (isActive) {
          setReasons(response);
        }
      })
      .catch((requestError) => {
        if (isActive) {
          setReasons([]);
          setError(
            requestError instanceof Error
              ? requestError.message
              : t("report.common.loadError"),
          );
        }
      })
      .finally(() => {
        if (isActive) {
          setIsLoadingReasons(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [reloadKey, t, targetType, visible]);

  const isDescriptionRequired = Boolean(selectedReason?.requiresDescription);
  const canSubmitReport =
    Boolean(selectedReason) &&
    !isSubmitting &&
    (!isDescriptionRequired || Boolean(description.trim()));

  function handleBack() {
    if (submittedReportId) {
      onClose();
      return;
    }

    if (selectedReason) {
      setDescription("");
      setError("");
      setSelectedReason(null);
      return;
    }

    onClose();
  }

  async function handleSubmitReport() {
    if (!selectedReason || !canSubmitReport) {
      return;
    }

    setIsSubmitting(true);
    setError("");

    try {
      const response = await createContentReport({
        description: description.trim() || undefined,
        reasonId: selectedReason.id,
        targetId,
        targetType,
      });

      setSubmittedReportId(response.id);
    } catch (requestError) {
      setError(reportRequestError(requestError, t));
    } finally {
      setIsSubmitting(false);
    }
  }

  function renderReasonList() {
    if (isLoadingReasons) {
      return (
        <View style={styles.stateBlock}>
          <ActivityIndicator color={colors.primary} />
          <Text style={styles.stateText}>{t("report.common.loading")}</Text>
        </View>
      );
    }

    if (error && !reasons.length) {
      return (
        <View style={styles.stateBlock}>
          <Text style={styles.stateTitle}>{t("report.common.loadError")}</Text>
          <Text style={styles.stateText}>{error}</Text>
          <Pressable
            accessibilityLabel={t("report.action.retry")}
            accessibilityRole="button"
            onPress={() => setReloadKey((value) => value + 1)}
            style={({ pressed }) => [styles.retryButton, pressed && styles.pressed]}
          >
            <Text style={styles.retryText}>{t("report.action.retry")}</Text>
          </Pressable>
        </View>
      );
    }

    if (!reasons.length) {
      return (
        <View style={styles.stateBlock}>
          <Text style={styles.stateTitle}>{t("report.common.emptyTitle")}</Text>
          <Text style={styles.stateText}>
            {t("report.common.emptyDescription")}
          </Text>
        </View>
      );
    }

    return (
      <ScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.hero}>
          <Text style={styles.question}>
            {t(
              targetType === "BLOG"
                ? "report.post.question"
                : "report.comment.question",
            )}
          </Text>
          <Text style={styles.description}>
            {t("report.common.anonymousNotice")}
          </Text>
        </View>

        <View style={styles.reasons}>
          {reasons.map((reason) => (
            <Pressable
              accessibilityLabel={reportReasonLabel(reason, locale, t)}
              accessibilityRole="button"
              key={reason.id}
              onPress={() => {
                setDescription("");
                setError("");
                setSelectedReason(reason);
              }}
              style={({ pressed }) => [styles.reasonRow, pressed && styles.pressed]}
            >
              <Text style={styles.reasonText}>
                {reportReasonLabel(reason, locale, t)}
              </Text>
              <ChevronRight color={colors.muted} size={28} strokeWidth={2.2} />
            </Pressable>
          ))}
        </View>
      </ScrollView>
    );
  }

  function renderReasonDetails() {
    if (!selectedReason) {
      return null;
    }

    if (submittedReportId) {
      return (
        <View style={styles.successBlock}>
          <Text style={styles.detailTitle}>{t("report.common.successTitle")}</Text>
          <Text style={styles.detailText}>
            {t("report.common.successDescription")}
          </Text>
          <Pressable
            accessibilityLabel={t("report.common.close")}
            accessibilityRole="button"
            onPress={onClose}
            style={({ pressed }) => [styles.submitButton, pressed && styles.pressed]}
          >
            <Text style={styles.submitText}>{t("report.action.close")}</Text>
          </Pressable>
        </View>
      );
    }

    return (
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        style={styles.detailContent}
      >
        <ScrollView
          contentContainerStyle={styles.detailScrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <Text style={styles.detailTitle}>
            {reportReasonLabel(selectedReason, locale, t)}
          </Text>
          <Text style={styles.detailText}>
            {reportReasonDescription(selectedReason, t)}
          </Text>

          <View style={styles.inputBlock}>
            <Text style={styles.inputLabel}>
              {t(
                isDescriptionRequired
                  ? "report.common.detailsRequired"
                  : "report.common.detailsOptional",
              )}
            </Text>
            <TextInput
              multiline
              onChangeText={setDescription}
              placeholder={t("report.common.detailsPlaceholder")}
              placeholderTextColor={colors.muted}
              style={styles.input}
              textAlignVertical="top"
              value={description}
            />
          </View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}
        </ScrollView>

        <View style={styles.footer}>
          <Pressable
            accessibilityLabel={t("report.common.submit")}
            accessibilityRole="button"
            disabled={!canSubmitReport}
            onPress={handleSubmitReport}
            style={({ pressed }) => [
              styles.submitButton,
              pressed && canSubmitReport && styles.pressed,
              !canSubmitReport && styles.disabled,
            ]}
          >
            {isSubmitting ? (
              <ActivityIndicator color={colors.white} />
            ) : (
              <Text style={styles.submitText}>{t("report.action.submit")}</Text>
            )}
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    );
  }

  return (
    <Modal animationType="slide" onRequestClose={handleBack} visible={visible}>
      <SafeAreaView style={styles.safeArea}>
        <View style={styles.header}>
          <Pressable
            accessibilityLabel={t("report.action.goBack")}
            accessibilityRole="button"
            onPress={handleBack}
            style={({ pressed }) => [styles.headerButton, pressed && styles.pressed]}
          >
            <ArrowLeft color={colors.foreground} size={30} strokeWidth={2.5} />
          </Pressable>
          <Text style={styles.title}>{t("report.common.title")}</Text>
          <Pressable
            accessibilityLabel={t("report.common.close")}
            accessibilityRole="button"
            onPress={onClose}
            style={({ pressed }) => [styles.headerButton, pressed && styles.pressed]}
          >
            <X color={colors.foreground} size={32} strokeWidth={2.5} />
          </Pressable>
        </View>

        {selectedReason ? renderReasonDetails() : renderReasonList()}
      </SafeAreaView>
    </Modal>
  );
}

export function ReportPostModal({ blogId, onClose, visible }: ReportPostModalProps) {
  return (
    <ReportContentModal
      onClose={onClose}
      targetId={blogId}
      targetType="BLOG"
      visible={visible}
    />
  );
}

const styles = StyleSheet.create({
  content: {
    paddingBottom: spacing.xl,
  },
  description: {
    color: colors.muted,
    fontSize: typography.body,
    fontWeight: "600",
    lineHeight: 26,
    marginTop: spacing.lg,
    textAlign: "center",
  },
  detailContent: {
    flex: 1,
  },
  detailScrollContent: {
    paddingBottom: spacing.xl,
    paddingHorizontal: spacing.xl,
    paddingTop: 56,
  },
  detailText: {
    color: colors.foreground,
    fontSize: typography.body,
    lineHeight: 27,
    marginTop: spacing.xl,
    textAlign: "center",
  },
  detailTitle: {
    color: colors.foreground,
    fontSize: 24,
    fontWeight: "900",
    lineHeight: 30,
    textAlign: "center",
  },
  disabled: {
    opacity: 0.45,
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.label,
    fontWeight: "700",
    lineHeight: 20,
    marginTop: spacing.md,
  },
  footer: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
    padding: spacing.xl,
  },
  header: {
    alignItems: "center",
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    minHeight: 72,
    paddingHorizontal: spacing.lg,
  },
  headerButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  hero: {
    alignItems: "center",
    paddingHorizontal: spacing.xl,
    paddingTop: 52,
  },
  input: {
    borderColor: colors.border,
    borderRadius: 14,
    borderWidth: 1,
    color: colors.foreground,
    fontSize: typography.body,
    minHeight: 132,
    padding: spacing.lg,
  },
  inputBlock: {
    gap: spacing.sm,
    marginTop: 54,
  },
  inputLabel: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.72,
  },
  question: {
    color: colors.foreground,
    fontSize: 24,
    fontWeight: "900",
    lineHeight: 30,
    textAlign: "center",
  },
  reasonRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 72,
    paddingHorizontal: spacing.xl,
  },
  reasonText: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    fontWeight: "700",
    lineHeight: 25,
  },
  retryButton: {
    alignItems: "center",
    borderColor: colors.primary,
    borderRadius: 12,
    borderWidth: 1,
    justifyContent: "center",
    marginTop: spacing.lg,
    minHeight: 44,
    paddingHorizontal: spacing.lg,
  },
  retryText: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "900",
  },
  reasons: {
    paddingTop: 62,
  },
  safeArea: {
    backgroundColor: colors.surface,
    flex: 1,
  },
  stateBlock: {
    alignItems: "center",
    flex: 1,
    justifyContent: "center",
    paddingHorizontal: spacing.xl,
  },
  stateText: {
    color: colors.muted,
    fontSize: typography.body,
    lineHeight: 24,
    marginTop: spacing.md,
    textAlign: "center",
  },
  stateTitle: {
    color: colors.foreground,
    fontSize: 22,
    fontWeight: "900",
    textAlign: "center",
  },
  submitButton: {
    alignItems: "center",
    backgroundColor: colors.link,
    borderRadius: 14,
    minHeight: 56,
    justifyContent: "center",
    paddingHorizontal: spacing.xl,
  },
  submitText: {
    color: colors.white,
    fontSize: typography.body,
    fontWeight: "900",
  },
  successBlock: {
    flex: 1,
    justifyContent: "center",
    paddingHorizontal: spacing.xl,
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: 30,
    fontWeight: "900",
    paddingLeft: spacing.lg,
  },
});
