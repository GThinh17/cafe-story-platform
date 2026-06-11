import { Send, X } from "lucide-react-native";
import { useEffect, useMemo, useState } from "react";
import {
  FlatList,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";

import { Avatar } from "../ui/avatar";
import { EmptyState } from "../ui/empty-state";
import { LoadingState } from "../ui/loading-state";
import { getMockCommentsByBlogId } from "../../mocks";
import { colors, spacing, typography } from "../../theme";
import type { BlogFeedComment } from "../../types";

type CommentModalProps = {
  blogId: string;
  onClose: () => void;
  postAuthorName: string;
  visible: boolean;
};

function formatCommentTime(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "now";
  }

  const diffMinutes = Math.max(0, Math.floor((Date.now() - date.getTime()) / 60000));

  if (diffMinutes < 1) {
    return "now";
  }

  if (diffMinutes < 60) {
    return `${diffMinutes}m`;
  }

  const diffHours = Math.floor(diffMinutes / 60);

  if (diffHours < 24) {
    return `${diffHours}h`;
  }

  return `${Math.floor(diffHours / 24)}d`;
}

function getInitials(name: string) {
  return name
    .split(/\s|_/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

function CommentItem({ comment }: { comment: BlogFeedComment }) {
  return (
    <View style={styles.commentItem}>
      <Avatar
        initials={getInitials(comment.authorName)}
        size={38}
        uri={comment.authorAvatar}
      />
      <View style={styles.commentBody}>
        <View style={styles.commentBubble}>
          <Text numberOfLines={1} style={styles.commentAuthor}>
            {comment.authorName}
          </Text>
          <Text style={styles.commentText}>{comment.content}</Text>
        </View>
        <View style={styles.commentMetaRow}>
          <Text style={styles.commentMeta}>{formatCommentTime(comment.createdAt)}</Text>
          <Text style={styles.commentMeta}>Like</Text>
          <Text style={styles.commentMeta}>Reply</Text>
          {comment.likeCount ? (
            <Text style={styles.commentMeta}>{comment.likeCount} likes</Text>
          ) : null}
          {comment.replyCount ? (
            <Text style={styles.commentMeta}>{comment.replyCount} replies</Text>
          ) : null}
        </View>
      </View>
    </View>
  );
}

export function CommentModal({
  blogId,
  onClose,
  postAuthorName,
  visible,
}: CommentModalProps) {
  const [comments, setComments] = useState<BlogFeedComment[]>([]);
  const [draft, setDraft] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    let isActive = true;

    if (!visible) {
      setDraft("");
      return;
    }

    setIsLoading(true);

    getMockCommentsByBlogId(blogId)
      .then((response) => {
        if (isActive) {
          setComments(response);
        }
      })
      .finally(() => {
        if (isActive) {
          setIsLoading(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [blogId, visible]);

  const title = useMemo(
    () => `${postAuthorName}'s comments`,
    [postAuthorName],
  );

  function handlePostComment() {
    const content = draft.trim();

    if (!content) {
      return;
    }

    setComments((currentComments) => [
      {
        authorAvatar: null,
        authorName: "you",
        content,
        createdAt: new Date().toISOString(),
        id: `local-comment-${Date.now()}`,
        likeCount: 0,
      },
      ...currentComments,
    ]);
    setDraft("");
  }

  return (
    <Modal
      animationType="slide"
      onRequestClose={onClose}
      transparent
      visible={visible}
    >
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        style={styles.overlay}
      >
        <Pressable
          accessibilityLabel="Close comments"
          onPress={onClose}
          style={styles.backdrop}
        />
        <View style={styles.sheet}>
          <View style={styles.handle} />
          <View style={styles.header}>
            <Text numberOfLines={1} style={styles.title}>
              {title}
            </Text>
            <Pressable
              accessibilityLabel="Close comments"
              accessibilityRole="button"
              onPress={onClose}
              style={({ pressed }) => [
                styles.closeButton,
                pressed && styles.pressed,
              ]}
            >
              <X color={colors.foreground} size={20} strokeWidth={2.5} />
            </Pressable>
          </View>

          {isLoading ? (
            <LoadingState label="Loading comments..." />
          ) : comments.length ? (
            <FlatList
              contentContainerStyle={styles.commentsList}
              data={comments}
              keyExtractor={(comment) => comment.id}
              keyboardShouldPersistTaps="handled"
              renderItem={({ item }) => <CommentItem comment={item} />}
              showsVerticalScrollIndicator={false}
            />
          ) : (
            <EmptyState
              description="Start the conversation with your first thought."
              title="No comments yet"
            />
          )}

          <View style={styles.composer}>
            <Avatar size={34} uri={null} />
            <View style={styles.inputWrap}>
              <TextInput
                multiline
                onChangeText={setDraft}
                placeholder="Add a comment..."
                placeholderTextColor={colors.muted}
                style={styles.input}
                value={draft}
              />
            </View>
            <Pressable
              accessibilityLabel="Post comment"
              accessibilityRole="button"
              disabled={!draft.trim()}
              onPress={handlePostComment}
              style={({ pressed }) => [
                styles.sendButton,
                !draft.trim() && styles.sendButtonDisabled,
                pressed && draft.trim() && styles.pressed,
              ]}
            >
              <Send color={colors.white} size={18} strokeWidth={2.4} />
            </Pressable>
          </View>
        </View>
      </KeyboardAvoidingView>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    width: "100%",
  },
  closeButton: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 18,
    height: 36,
    justifyContent: "center",
    width: 36,
  },
  commentAuthor: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  commentBody: {
    flex: 1,
    gap: spacing.xs,
  },
  commentBubble: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 16,
    gap: spacing.xs,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  commentItem: {
    flexDirection: "row",
    gap: spacing.md,
  },
  commentMeta: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  commentMetaRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.md,
    paddingLeft: spacing.sm,
  },
  commentText: {
    color: colors.secondaryStrong,
    fontSize: typography.label,
    lineHeight: 20,
  },
  commentsList: {
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.lg,
  },
  composer: {
    alignItems: "flex-end",
    borderTopColor: colors.border,
    borderTopWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.lg,
  },
  handle: {
    alignSelf: "center",
    backgroundColor: colors.border,
    borderRadius: 3,
    height: 5,
    marginTop: spacing.sm,
    width: 44,
  },
  header: {
    alignItems: "center",
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
  },
  input: {
    color: colors.foreground,
    fontSize: typography.label,
    maxHeight: 92,
    minHeight: 38,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  inputWrap: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 20,
    flex: 1,
  },
  overlay: {
    backgroundColor: "rgba(33, 29, 28, 0.42)",
    flex: 1,
    justifyContent: "flex-end",
  },
  pressed: {
    opacity: 0.72,
  },
  sendButton: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 19,
    height: 38,
    justifyContent: "center",
    width: 38,
  },
  sendButtonDisabled: {
    opacity: 0.45,
  },
  sheet: {
    backgroundColor: colors.background,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    maxHeight: "86%",
    minHeight: "62%",
    overflow: "hidden",
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    fontWeight: "900",
  },
});
