import {
  Bookmark,
  CircleUserRound,
  Eye,
  EyeOff,
  Info,
  MessageSquareWarning,
  QrCode,
  SlidersHorizontal,
} from "lucide-react-native";
import { Modal, Pressable, StyleSheet, Text, View } from "react-native";

import { colors, spacing, typography } from "../../theme";

type PostOptionsModalProps = {
  isSavePending: boolean;
  isSaved: boolean;
  onClose: () => void;
  onReport: () => void;
  onToggleSave: () => void;
  visible: boolean;
};

type RowProps = {
  Icon: typeof Info;
  color?: string;
  label: string;
  onPress?: () => void;
};

function OptionRow({ Icon, color = colors.foreground, label, onPress }: RowProps) {
  return (
    <Pressable
      accessibilityLabel={label}
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [styles.row, pressed && styles.pressed]}
    >
      <Icon color={color} size={28} strokeWidth={2.4} />
      <Text style={[styles.rowText, { color }]}>{label}</Text>
    </Pressable>
  );
}

export function PostOptionsModal({
  isSavePending,
  isSaved,
  onClose,
  onReport,
  onToggleSave,
  visible,
}: PostOptionsModalProps) {
  return (
    <Modal
      animationType="slide"
      onRequestClose={onClose}
      transparent
      visible={visible}
    >
      <View style={styles.overlay}>
        <Pressable
          accessibilityLabel="Close post options"
          onPress={onClose}
          style={styles.backdrop}
        />
        <View style={styles.sheet}>
          <View style={styles.handle} />

          <View style={styles.quickActions}>
            <Pressable
              accessibilityLabel={isSaved ? "Unsave post" : "Save post"}
              accessibilityRole="button"
              disabled={isSavePending}
              onPress={onToggleSave}
              style={({ pressed }) => [
                styles.quickAction,
                pressed && !isSavePending && styles.pressed,
                isSavePending && styles.disabled,
              ]}
            >
              <View style={styles.quickIcon}>
                <Bookmark
                  color={colors.foreground}
                  fill={isSaved ? colors.foreground : "none"}
                  size={34}
                  strokeWidth={2.4}
                />
              </View>
              <Text style={styles.quickLabel}>{isSaved ? "Đã lưu" : "Lưu"}</Text>
            </Pressable>

            <Pressable
              accessibilityLabel="Open QR code"
              accessibilityRole="button"
              style={({ pressed }) => [styles.quickAction, pressed && styles.pressed]}
            >
              <View style={styles.quickIcon}>
                <QrCode color={colors.foreground} size={34} strokeWidth={2.4} />
              </View>
              <Text style={styles.quickLabel}>Mã QR</Text>
            </Pressable>
          </View>

          <View style={styles.divider} />

          <View style={styles.rows}>
            <OptionRow Icon={Info} label="Tại sao bạn nhìn thấy bài viết này" />
            <OptionRow Icon={EyeOff} label="Không quan tâm" />
            <OptionRow Icon={Eye} label="Quan tâm" />
            <OptionRow Icon={CircleUserRound} label="Giới thiệu về tài khoản này" />
            <OptionRow
              Icon={MessageSquareWarning}
              color={colors.danger}
              label="Báo cáo"
              onPress={onReport}
            />
          </View>

          <View style={styles.divider} />

          <OptionRow Icon={SlidersHorizontal} label="Quản lý tùy chọn về nội dung" />
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    width: "100%",
  },
  disabled: {
    opacity: 0.5,
  },
  divider: {
    backgroundColor: colors.border,
    height: 1,
    width: "100%",
  },
  handle: {
    alignSelf: "center",
    backgroundColor: colors.border,
    borderRadius: 3,
    height: 5,
    marginTop: spacing.sm,
    width: 44,
  },
  overlay: {
    backgroundColor: "rgba(33, 29, 28, 0.42)",
    flex: 1,
    justifyContent: "flex-end",
  },
  pressed: {
    opacity: 0.72,
  },
  quickAction: {
    alignItems: "center",
    gap: spacing.sm,
    minWidth: 120,
  },
  quickActions: {
    flexDirection: "row",
    justifyContent: "space-around",
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.xl,
  },
  quickIcon: {
    alignItems: "center",
    borderColor: colors.muted,
    borderRadius: 46,
    borderWidth: 1.5,
    height: 92,
    justifyContent: "center",
    width: 92,
  },
  quickLabel: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "700",
  },
  row: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.lg,
    minHeight: 68,
    paddingHorizontal: spacing.xl,
  },
  rowText: {
    flex: 1,
    fontSize: typography.body,
    fontWeight: "700",
    lineHeight: 24,
  },
  rows: {
    paddingVertical: spacing.md,
  },
  sheet: {
    backgroundColor: colors.surface,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    overflow: "hidden",
    paddingBottom: spacing.lg,
  },
});
