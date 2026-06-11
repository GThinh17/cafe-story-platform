import { ArrowLeft, ChevronRight, X } from "lucide-react-native";
import { Modal, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { colors, spacing, typography } from "../../theme";

type ReportPostModalProps = {
  onClose: () => void;
  visible: boolean;
};

const reportReasons = [
  "Chỉ là tôi không thích nội dung này",
  "Bắt nạt hoặc liên hệ theo cách không mong muốn",
  "Tự tử, tự gây thương tích hoặc ăn uống thất thường",
  "Bạo lực, thù ghét hoặc bóc lột",
  "Bán hoặc quảng bá mặt hàng bị hạn chế",
  "Ảnh khỏa thân hoặc hoạt động tình dục",
  "Lừa đảo, gian lận hoặc spam",
  "Thông tin sai sự thật",
  "Quyền sở hữu trí tuệ",
];

export function ReportPostModal({ onClose, visible }: ReportPostModalProps) {
  return (
    <Modal animationType="slide" onRequestClose={onClose} visible={visible}>
      <SafeAreaView style={styles.safeArea}>
        <View style={styles.header}>
          <Pressable
            accessibilityLabel="Go back"
            accessibilityRole="button"
            onPress={onClose}
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
            {reportReasons.map((reason) => (
              <Pressable
                accessibilityLabel={reason}
                accessibilityRole="button"
                key={reason}
                style={({ pressed }) => [styles.reasonRow, pressed && styles.pressed]}
              >
                <Text style={styles.reasonText}>{reason}</Text>
                <ChevronRight color={colors.muted} size={28} strokeWidth={2.2} />
              </Pressable>
            ))}
          </View>
        </ScrollView>
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
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: 30,
    fontWeight: "900",
    paddingLeft: spacing.lg,
  },
});
