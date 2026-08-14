import { ArrowLeft, ChevronRight, X } from "lucide-react-native";
import { Text } from "../../features/i18n/localized-native";
import { Pressable, TextInput } from "../../features/i18n/localized-native";
import { useEffect, useState } from "react";
import {
  ActivityIndicator, KeyboardAvoidingView, Modal, Platform, ScrollView, StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { createContentReport, getReportReasons } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { ReportReasonResponse } from "../../types";

type ReportPostModalProps = {
  blogId: string;
  onClose: () => void;
  visible: boolean;
};

type ReportReasonCopy = {
  description?: string;
  label: string;
};

const REPORT_REASON_COPY: Record<string, ReportReasonCopy> = {
  BULLYING_OR_UNWANTED_CONTACT: {
    label: "Bullying or unwanted contact",
  },
  DISLIKE_CONTENT: {
    label: "I just don't like this content",
  },
  FALSE_INFORMATION: {
    label: "False information",
  },
  INTELLECTUAL_PROPERTY: {
    description:
      "Tell CafeStory what rights may be affected so the team can review the report accurately.",
    label: "Intellectual property",
  },
  NUDITY_OR_SEXUAL_ACTIVITY: {
    label: "Nudity or sexual activity",
  },
  RESTRICTED_GOODS: {
    label: "Selling or promoting restricted goods",
  },
  SCAM_FRAUD_OR_SPAM: {
    label: "Scam, fraud, or spam",
  },
  SELF_HARM_OR_ABNORMAL_EATING: {
    label: "Self-harm or disordered eating",
  },
  VIOLENCE_HATE_OR_EXPLOITATION: {
    label: "Violence, hate, or exploitation",
  },
};

function reportReasonLabel(reason: ReportReasonResponse) {
  return REPORT_REASON_COPY[reason.code]?.label ?? reason.labelVi;
}

function reportReasonDescription(reason: ReportReasonResponse) {
  return (
    REPORT_REASON_COPY[reason.code]?.description ??
    "Tell CafeStory a little more so the team can review this accurately."
  );
}

export function ReportPostModal({
  blogId,
  onClose,
  visible,
}: ReportPostModalProps) {
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [isLoadingReasons, setIsLoadingReasons] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [reasons, setReasons] = useState<ReportReasonResponse[]>([]);
  const [selectedReason, setSelectedReason] = useState<ReportReasonResponse | null>(null);
  const [submittedReportId, setSubmittedReportId] = useState<string | null>(null);

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

    getReportReasons("BLOG")
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
              : "Unable to load report reasons.",
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
  }, [visible]);

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
        targetId: blogId,
        targetType: "BLOG",
      });

      setSubmittedReportId(response.id);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to submit this report.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  function renderReasonList() {
    if (isLoadingReasons) {
      return (
        <View style={styles.stateBlock}>
          <ActivityIndicator color={colors.primary} />
          <Text style={styles.stateText}>Loading report reasons...</Text>
        </View>
      );
    }

    if (error && !reasons.length) {
      return (
        <View style={styles.stateBlock}>
          <Text style={styles.stateTitle}>Unable to load report reasons</Text>
          <Text style={styles.stateText}>{error}</Text>
        </View>
      );
    }

    if (!reasons.length) {
      return (
        <View style={styles.stateBlock}>
          <Text style={styles.stateTitle}>No report reasons yet</Text>
          <Text style={styles.stateText}>
            Please try again after report reasons have been configured.
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
          <Text style={styles.question}>Why are you reporting this post?</Text>
          <Text style={styles.description}>
            Your report is anonymous. If someone is in immediate danger,
            contact your local emergency services right away.
          </Text>
        </View>

        <View style={styles.reasons}>
          {reasons.map((reason) => (
            <Pressable
              accessibilityLabel={reportReasonLabel(reason)}
              accessibilityRole="button"
              key={reason.id}
              onPress={() => {
                setDescription("");
                setError("");
                setSelectedReason(reason);
              }}
              style={({ pressed }) => [styles.reasonRow, pressed && styles.pressed]}
            >
              <Text style={styles.reasonText}>{reportReasonLabel(reason)}</Text>
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
          <Text style={styles.detailTitle}>Thanks for your report</Text>
          <Text style={styles.detailText}>
            Your report has been submitted and will be reviewed by CafeStory.
          </Text>
          <Pressable
            accessibilityLabel="Close report"
            accessibilityRole="button"
            onPress={onClose}
            style={({ pressed }) => [styles.submitButton, pressed && styles.pressed]}
          >
            <Text style={styles.submitText}>Close</Text>
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
          <Text style={styles.detailTitle}>{reportReasonLabel(selectedReason)}</Text>
          <Text style={styles.detailText}>
            {reportReasonDescription(selectedReason)}
          </Text>

          <View style={styles.inputBlock}>
            <Text style={styles.inputLabel}>
              Details {isDescriptionRequired ? "(required)" : "(optional)"}
            </Text>
            <TextInput
              multiline
              onChangeText={setDescription}
              placeholder="Add report details..."
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
            accessibilityLabel="Submit report"
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
              <Text style={styles.submitText}>Report</Text>
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
            accessibilityLabel="Go back"
            accessibilityRole="button"
            onPress={handleBack}
            style={({ pressed }) => [styles.headerButton, pressed && styles.pressed]}
          >
            <ArrowLeft color={colors.foreground} size={30} strokeWidth={2.5} />
          </Pressable>
          <Text style={styles.title}>Report</Text>
          <Pressable
            accessibilityLabel="Close report"
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
