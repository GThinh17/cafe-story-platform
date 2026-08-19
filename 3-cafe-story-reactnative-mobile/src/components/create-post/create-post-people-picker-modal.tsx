import { ArrowLeft, Check } from "lucide-react-native";
import { Pressable, Text } from "react-native";
import { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator, Modal, ScrollView, StyleSheet, View } from "react-native";

import { getFollowingByUserId, getUserProfile } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { UserResponse } from "../../types";
import { Avatar } from "../ui/avatar";
import { EmptyState } from "../ui/empty-state";
import { LoadingState } from "../ui/loading-state";
import { t } from "../../features/i18n";

type CreatePostPeoplePickerModalProps = {
  currentUserId?: string | null;
  onApply: (users: UserResponse[]) => void;
  onClose: () => void;
  selectedUserIds: string[];
  visible: boolean;
};

export function CreatePostPeoplePickerModal({
  currentUserId,
  onApply,
  onClose,
  selectedUserIds,
  visible,
}: CreatePostPeoplePickerModalProps) {
  const [followingUsers, setFollowingUsers] = useState<UserResponse[]>([]);
  const [selectedIds, setSelectedIds] = useState<string[]>(selectedUserIds);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (visible) {
      setSelectedIds(selectedUserIds);
    }
  }, [selectedUserIds, visible]);

  useEffect(() => {
    if (!visible || !currentUserId) {
      return;
    }

    const userId = currentUserId;
    let isMounted = true;

    async function loadFollowingUsers() {
      setIsLoading(true);
      setError(null);

      try {
        const following = await getFollowingByUserId(userId);
        const followingUserIds = following
          .map((item) => item.followingUserId)
          .filter((userId): userId is string => Boolean(userId));
        const profiles = await Promise.all(
          followingUserIds.map((followingUserId) => getUserProfile(followingUserId)),
        );

        if (isMounted) {
          setFollowingUsers(profiles);
        }
      } catch (requestError) {
        if (isMounted) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : "Unable to load following users.",
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    void loadFollowingUsers();

    return () => {
      isMounted = false;
    };
  }, [currentUserId, visible]);

  const selectedUsers = useMemo(
    () => followingUsers.filter((profile) => selectedIds.includes(profile.userId)),
    [followingUsers, selectedIds],
  );

  function toggleUser(userId: string) {
    setSelectedIds((currentIds) =>
      currentIds.includes(userId)
        ? currentIds.filter((id) => id !== userId)
        : [...currentIds, userId],
    );
  }

  function handleApply() {
    onApply(selectedUsers);
    onClose();
  }

  return (
    <Modal animationType="slide" onRequestClose={onClose} visible={visible}>
      <View style={styles.modal}>
        <View style={styles.header}>
          <Pressable
            accessibilityLabel={t("Close people picker")}
            accessibilityRole="button"
            onPress={onClose}
            style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
          >
            <ArrowLeft color={colors.foreground} size={30} strokeWidth={2.5} />
          </Pressable>

          <Text numberOfLines={1} style={styles.headerTitle}>{t("Tag people")}</Text>

          <Pressable
            accessibilityLabel={t("Apply tagged people")}
            accessibilityRole="button"
            onPress={handleApply}
            style={({ pressed }) => [styles.doneButton, pressed && styles.pressed]}
          >
            <Text style={styles.doneText}>{t("Done")}</Text>
          </Pressable>
        </View>

        {isLoading ? (
          <LoadingState label={t("Loading following...")} />
        ) : error ? (
          <EmptyState description={t("Try closing this screen and opening it again.")} title={error} />
        ) : !currentUserId ? (
          <EmptyState description={t("Sign in again to tag people.")} title={t("Unable to find your account")} />
        ) : followingUsers.length === 0 ? (
          <EmptyState description={t("Follow people first, then tag them in posts.")} title={t("No following users yet")} />
        ) : (
          <ScrollView
            contentContainerStyle={styles.content}
            showsVerticalScrollIndicator={false}
          >
            {followingUsers.map((profile) => {
              const isSelected = selectedIds.includes(profile.userId);
              const displayName =
                profile.userFullName || profile.userName || "CafeStory user";

              return (
                <Pressable
                  accessibilityLabel={`Select ${displayName}`}
                  accessibilityRole="checkbox"
                  accessibilityState={{ checked: isSelected }}
                  key={profile.userId}
                  onPress={() => toggleUser(profile.userId)}
                  style={({ pressed }) => [
                    styles.userRow,
                    pressed && styles.pressed,
                  ]}
                >
                  <Avatar
                    initials={profile.userName.slice(0, 2).toUpperCase()}
                    size={54}
                    uri={profile.userAvatar}
                  />
                  <View style={styles.userCopy}>
                    <Text numberOfLines={1} style={styles.userName}>
                      {displayName}
                    </Text>
                    <Text numberOfLines={1} style={styles.userHandle}>
                      @{profile.userName}
                    </Text>
                  </View>
                  <View style={[styles.checkCircle, isSelected && styles.checkCircleActive]}>
                    {isSelected ? (
                      <Check color={colors.white} size={17} strokeWidth={3} />
                    ) : null}
                  </View>
                </Pressable>
              );
            })}
          </ScrollView>
        )}

        {isLoading ? (
          <View style={styles.loadingFooter}>
            <ActivityIndicator color={colors.primary} />
          </View>
        ) : null}
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  checkCircle: {
    alignItems: "center",
    borderColor: colors.border,
    borderRadius: 13,
    borderWidth: 1,
    height: 26,
    justifyContent: "center",
    width: 26,
  },
  checkCircleActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  content: {
    paddingBottom: 36,
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
  loadingFooter: {
    alignItems: "center",
    paddingBottom: spacing.lg,
  },
  modal: {
    backgroundColor: colors.white,
    flex: 1,
  },
  pressed: {
    opacity: 0.72,
  },
  userCopy: {
    flex: 1,
    gap: 3,
  },
  userHandle: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "600",
  },
  userName: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  userRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 78,
    paddingHorizontal: spacing.xl,
  },
});
