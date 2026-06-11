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

import { createContentReport, getReportReasons } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { ReportReasonResponse } from "../../types";

type ReportPostModalProps = {
  blogId: string;
  onClose: () => void;
  visible: boolean;
};

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
              : "Không thể tải lý do báo cáo.",
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
          : "Không thể gửi báo cáo.",
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
          <Text style={styles.stateText}>Đang tải lý do báo cáo...</Text>
        </View>
      );
    }

    if (error && !reasons.length) {
      return (
        <View style={styles.stateBlock}>
          <Text style={styles.stateTitle}>Không tải được lý do báo cáo</Text>
          <Text style={styles.stateText}>{error}</Text>
        </View>
      );
    }

    if (!reasons.length) {
      return (
        <View style={styles.stateBlock}>
          <Text style={styles.stateTitle}>Chưa có lý do báo cáo</Text>
          <Text style={styles.stateText}>
            Vui lòng thử lại sau khi hệ thống đã cấu hình report_reason.
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
          <Text style={styles.question}>Tại sao bạn báo cáo bài viết này?</Text>
          <Text style={styles.description}>
            Báo cáo của bạn sẽ được ẩn danh. Nếu ai đó đang gặp nguy hiểm,
            đừng chần chừ mà hãy báo ngay cho dịch vụ khẩn cấp tại địa phương.
          </Text>
        </View>

        <View style={styles.reasons}>
          {reasons.map((reason) => (
            <Pressable
              accessibilityLabel={reason.labelVi}
              accessibilityRole="button"
              key={reason.id}
              onPress={() => {
                setDescription("");
                setError("");
                setSelectedReason(reason);
              }}
              style={({ pressed }) => [styles.reasonRow, pressed && styles.pressed]}
            >
              <Text style={styles.reasonText}>{reason.labelVi}</Text>
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
          <Text style={styles.detailTitle}>Cảm ơn bạn đã báo cáo</Text>
          <Text style={styles.detailText}>
            Báo cáo của bạn đã được gửi và sẽ được CafeStory xem xét.
          </Text>
          <Pressable
            accessibilityLabel="Close report"
            accessibilityRole="button"
            onPress={onClose}
            style={({ pressed }) => [styles.submitButton, pressed && styles.pressed]}
          >
            <Text style={styles.submitText}>Đóng</Text>
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
          <Text style={styles.detailTitle}>{selectedReason.labelVi}</Text>
          <Text style={styles.detailText}>
            {selectedReason.descriptionVi ||
              "Hãy cho CafeStory biết thêm chi tiết để đội ngũ có thể xem xét chính xác hơn."}
          </Text>

          <View style={styles.inputBlock}>
            <Text style={styles.inputLabel}>
              Lý do cụ thể {isDescriptionRequired ? "(bắt buộc)" : "(không bắt buộc)"}
            </Text>
            <TextInput
              multiline
              onChangeText={setDescription}
              placeholder="Nhập chi tiết báo cáo..."
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
              <Text style={styles.submitText}>Báo cáo</Text>
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
          <Text style={styles.title}>Báo cáo</Text>
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
