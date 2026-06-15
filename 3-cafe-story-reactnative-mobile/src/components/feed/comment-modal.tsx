import { Heart, Send, X } from "lucide-react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { useEffect, useMemo, useState } from "react";
import {
  FlatList,
  Keyboard,
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
import { LoadingState } from "../ui/loading-state";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import { createComment, getCommentsByBlog } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { CommentResponse } from "../../types";

type CommentModalProps = {
  blogId: string;
  onClose: () => void;
  onCommentCreated?: () => void;
  postAuthorName: string;
  visible: boolean;
};

type CommentItemProps = {
  comment: CommentResponse;
  isLiked: boolean;
  likeCount: number;
  level?: number;
  onOpenProfile: (userId: string, userName?: string | null) => void;
  onReply: (comment: CommentResponse) => void;
  onToggleLike: (commentId: string) => void;
};

const reactions = ["❤️", "🙌", "🔥", "👏", "🥲", "😍", "😮", "😂"];

function formatCommentTime(value: string | null) {
  if (!value) {
    return "now";
  }

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

function getCommentAuthor(comment: CommentResponse) {
  return comment.authorUserName || "CafeStory user";
}

function getInitials(name: string) {
  return name
    .split(/\s|_/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

function renderCommentContent(content: string) {
  const parts = content.split(/(@[\w._-]+)/g);

  return parts.map((part, index) => {
    if (part.startsWith("@")) {
      return (
        <Text key={`${part}-${index}`} style={styles.mention}>
          {part}
        </Text>
      );
    }

    return <Text key={`${part}-${index}`}>{part}</Text>;
  });
}

function CommentItem({
  comment,
  isLiked,
  likeCount,
  level = 0,
  onOpenProfile,
  onReply,
  onToggleLike,
}: CommentItemProps) {
  const authorName = getCommentAuthor(comment);
  const isReply = level > 0;

  return (
    <View style={[styles.commentItem, isReply && styles.replyItem]}>
      <Pressable
        accessibilityLabel={`Open ${authorName} profile`}
        accessibilityRole="button"
        onPress={() => onOpenProfile(comment.userId, comment.authorUserName)}
        style={({ pressed }) => pressed && styles.pressed}
      >
        <Avatar initials={getInitials(authorName)} size={isReply ? 30 : 38} uri={null} />
      </Pressable>
      <View style={styles.commentBody}>
        <View style={styles.commentContentRow}>
          <View style={styles.commentTextBlock}>
            <Text style={styles.commentLine}>
              <Text style={styles.commentAuthor}>{authorName} </Text>
              {renderCommentContent(comment.content)}
            </Text>
            <View style={styles.commentMetaRow}>
              <Text style={styles.commentMeta}>{formatCommentTime(comment.createdAt)}</Text>
              <Pressable
                accessibilityLabel={`Reply to ${authorName}`}
                accessibilityRole="button"
                onPress={() => onReply(comment)}
              >
                <Text style={styles.commentMeta}>Reply</Text>
              </Pressable>
            </View>
          </View>
          <Pressable
            accessibilityLabel={isLiked ? "Unlike comment" : "Like comment"}
            accessibilityRole="button"
            onPress={() => onToggleLike(comment.id)}
            style={({ pressed }) => [
              styles.commentHeartButton,
              pressed && styles.pressed,
            ]}
          >
            <Heart
              color={isLiked ? colors.danger : colors.muted}
              fill={isLiked ? colors.danger : "none"}
              size={20}
              strokeWidth={2.2}
            />
            {likeCount ? <Text style={styles.commentLikeCount}>{likeCount}</Text> : null}
          </Pressable>
        </View>
      </View>
    </View>
  );
}

export function CommentModal({
  blogId,
  onClose,
  onCommentCreated,
  postAuthorName,
  visible,
}: CommentModalProps) {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user } = useAuth();
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [draft, setDraft] = useState("");
  const [error, setError] = useState("");
  const [expandedReplyIds, setExpandedReplyIds] = useState<Set<string>>(new Set());
  const [likedCommentIds, setLikedCommentIds] = useState<Set<string>>(new Set());
  const [localLikeCounts, setLocalLikeCounts] = useState<Record<string, number>>({});
  const [replyTarget, setReplyTarget] = useState<CommentResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isPosting, setIsPosting] = useState(false);

  useEffect(() => {
    let isActive = true;

    if (!visible) {
      setDraft("");
      setError("");
      setReplyTarget(null);
      return;
    }

    setIsLoading(true);
    setError("");

    getCommentsByBlog(blogId)
      .then((response) => {
        if (isActive) {
          setComments(response);
        }
      })
      .catch((requestError) => {
        if (isActive) {
          setComments([]);
          setError(
            requestError instanceof Error
              ? requestError.message
              : "Unable to load comments.",
          );
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

  const parentComments = useMemo(
    () => comments.filter((comment) => !comment.parentCommentId),
    [comments],
  );

  const repliesByParentId = useMemo(() => {
    const groupedReplies: Record<string, CommentResponse[]> = {};

    for (const comment of comments) {
      if (!comment.parentCommentId) {
        continue;
      }

      groupedReplies[comment.parentCommentId] = [
        ...(groupedReplies[comment.parentCommentId] ?? []),
        comment,
      ];
    }

    return groupedReplies;
  }, [comments]);

  const placeholder = replyTarget
    ? `Reply to ${getCommentAuthor(replyTarget)}...`
    : `Comment for ${postAuthorName}...`;

  function handleToggleCommentLike(commentId: string) {
    setLikedCommentIds((currentIds) => {
      const nextIds = new Set(currentIds);
      const nextIsLiked = !nextIds.has(commentId);

      if (nextIsLiked) {
        nextIds.add(commentId);
      } else {
        nextIds.delete(commentId);
      }

      setLocalLikeCounts((currentCounts) => ({
        ...currentCounts,
        [commentId]: Math.max(
          0,
          (currentCounts[commentId] ?? 0) + (nextIsLiked ? 1 : -1),
        ),
      }));

      return nextIds;
    });
  }

  function handleToggleReplies(commentId: string) {
    setExpandedReplyIds((currentIds) => {
      const nextIds = new Set(currentIds);

      if (nextIds.has(commentId)) {
        nextIds.delete(commentId);
      } else {
        nextIds.add(commentId);
      }

      return nextIds;
    });
  }

  async function handlePostComment() {
    const content = draft.trim();

    if (!content || isPosting) {
      return;
    }

    setIsPosting(true);
    setError("");

    try {
      const createdComment = await createComment({
        blogId,
        content,
        parentCommentId: replyTarget?.id,
      });

      setComments((currentComments) =>
        replyTarget ? [...currentComments, createdComment] : [createdComment, ...currentComments],
      );

      if (replyTarget) {
        setExpandedReplyIds((currentIds) => {
          const nextIds = new Set(currentIds);
          nextIds.add(replyTarget.id);
          return nextIds;
        });
      }

      setDraft("");
      setReplyTarget(null);
      Keyboard.dismiss();
      onCommentCreated?.();
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to post comment.",
      );
    } finally {
      setIsPosting(false);
    }
  }

  function handleOpenProfile(userId: string, userName?: string | null) {
    onClose();
    setTimeout(() => {
      navigation.navigate(routes.otherUserProfile, {
        userId,
        userName,
      });
    }, 120);
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
              Comments
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

          <View style={styles.content}>
            {isLoading ? (
              <LoadingState label="Loading comments..." />
            ) : error ? (
              <View style={styles.emptyBlock}>
                <Text style={styles.emptyTitle}>Comments unavailable</Text>
                <Text style={styles.emptyDescription}>{error}</Text>
              </View>
            ) : comments.length ? (
              <FlatList
                contentContainerStyle={styles.commentsList}
                data={parentComments}
                keyExtractor={(comment) => comment.id}
                keyboardShouldPersistTaps="handled"
                renderItem={({ item }) => {
                  const replies = repliesByParentId[item.id] ?? [];
                  const isExpanded = expandedReplyIds.has(item.id);
                  const visibleReplies = isExpanded ? replies : [];

                  return (
                    <View style={styles.thread}>
                      <CommentItem
                        comment={item}
                        isLiked={likedCommentIds.has(item.id)}
                        likeCount={localLikeCounts[item.id] ?? 0}
                        onOpenProfile={handleOpenProfile}
                        onReply={setReplyTarget}
                        onToggleLike={handleToggleCommentLike}
                      />
                      {visibleReplies.map((reply) => (
                        <CommentItem
                          comment={reply}
                          isLiked={likedCommentIds.has(reply.id)}
                          key={reply.id}
                          level={1}
                          likeCount={localLikeCounts[reply.id] ?? 0}
                          onOpenProfile={handleOpenProfile}
                          onReply={setReplyTarget}
                          onToggleLike={handleToggleCommentLike}
                        />
                      ))}
                      {replies.length ? (
                        <Pressable
                          accessibilityLabel={
                            isExpanded ? "Hide replies" : `View ${replies.length} replies`
                          }
                          accessibilityRole="button"
                          onPress={() => handleToggleReplies(item.id)}
                          style={({ pressed }) => [
                            styles.viewRepliesButton,
                            pressed && styles.pressed,
                          ]}
                        >
                          <View style={styles.replyLine} />
                          <Text style={styles.viewRepliesText}>
                            {isExpanded
                              ? "Hide replies"
                              : `View ${replies.length} ${
                                  replies.length === 1 ? "reply" : "replies"
                                }`}
                          </Text>
                        </Pressable>
                      ) : null}
                    </View>
                  );
                }}
                showsVerticalScrollIndicator={false}
              />
            ) : (
              <View style={styles.emptyBlock}>
                <Text style={styles.emptyTitle}>No comments yet</Text>
                <Text style={styles.emptyDescription}>
                  Be the first to start the conversation.
                </Text>
              </View>
            )}
          </View>

          <View style={styles.composerBlock}>
            <View style={styles.reactionRail}>
              {reactions.map((reaction) => (
                <Pressable
                  accessibilityLabel={`Add ${reaction} reaction`}
                  accessibilityRole="button"
                  key={reaction}
                  onPress={() => setDraft((currentDraft) => `${currentDraft}${reaction}`)}
                  style={({ pressed }) => [
                    styles.reactionButton,
                    pressed && styles.pressed,
                  ]}
                >
                  <Text style={styles.reactionText}>{reaction}</Text>
                </Pressable>
              ))}
            </View>

            {replyTarget ? (
              <View style={styles.replyTargetRow}>
                <Text numberOfLines={1} style={styles.replyTargetText}>
                  Replying to {getCommentAuthor(replyTarget)}
                </Text>
                <Pressable
                  accessibilityLabel="Cancel reply"
                  accessibilityRole="button"
                  onPress={() => setReplyTarget(null)}
                >
                  <Text style={styles.cancelReplyText}>Cancel</Text>
                </Pressable>
              </View>
            ) : null}

            <View style={styles.composer}>
              <Avatar
                initials={getInitials(user?.userName || "Me")}
                size={34}
                uri={user?.userAvatar}
              />
              <View style={styles.inputWrap}>
                <TextInput
                  multiline
                  onChangeText={setDraft}
                  placeholder={placeholder}
                  placeholderTextColor={colors.muted}
                  style={styles.input}
                  value={draft}
                />
              </View>
              <Pressable
                accessibilityLabel="Post comment"
                accessibilityRole="button"
                disabled={!draft.trim() || isPosting}
                onPress={handlePostComment}
                style={({ pressed }) => [
                  styles.sendButton,
                  (!draft.trim() || isPosting) && styles.sendButtonDisabled,
                  pressed && draft.trim() && !isPosting && styles.pressed,
                ]}
              >
                <Send color={colors.white} size={18} strokeWidth={2.4} />
              </Pressable>
            </View>
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
  cancelReplyText: {
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "900",
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
    fontWeight: "900",
  },
  commentBody: {
    flex: 1,
  },
  commentContentRow: {
    alignItems: "flex-start",
    flexDirection: "row",
    gap: spacing.sm,
  },
  commentHeartButton: {
    alignItems: "center",
    minHeight: 34,
    paddingTop: 4,
    width: 36,
  },
  commentItem: {
    flexDirection: "row",
    gap: spacing.md,
  },
  commentLikeCount: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
    marginTop: 2,
  },
  commentLine: {
    color: colors.foreground,
    fontSize: typography.body,
    lineHeight: 22,
  },
  commentMeta: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  commentMetaRow: {
    flexDirection: "row",
    gap: spacing.md,
    marginTop: spacing.xs,
  },
  commentTextBlock: {
    flex: 1,
  },
  commentsList: {
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.lg,
  },
  composer: {
    alignItems: "flex-end",
    flexDirection: "row",
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },
  composerBlock: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
    paddingBottom: spacing.lg,
  },
  content: {
    flex: 1,
  },
  emptyBlock: {
    alignItems: "center",
    flex: 1,
    justifyContent: "center",
    paddingHorizontal: spacing.xl,
  },
  emptyDescription: {
    color: colors.muted,
    fontSize: typography.body,
    marginTop: spacing.md,
    textAlign: "center",
  },
  emptyTitle: {
    color: colors.foreground,
    fontSize: 24,
    fontWeight: "900",
    textAlign: "center",
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
    fontSize: typography.body,
    maxHeight: 92,
    minHeight: 40,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  inputWrap: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 22,
    borderWidth: 1,
    flex: 1,
  },
  mention: {
    color: colors.link,
  },
  overlay: {
    backgroundColor: "rgba(33, 29, 28, 0.42)",
    flex: 1,
    justifyContent: "flex-end",
  },
  pressed: {
    opacity: 0.72,
  },
  reactionButton: {
    alignItems: "center",
    height: 38,
    justifyContent: "center",
    width: 42,
  },
  reactionRail: {
    flexDirection: "row",
    justifyContent: "space-between",
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.sm,
  },
  reactionText: {
    fontSize: 28,
  },
  replyItem: {
    marginLeft: 52,
    marginTop: spacing.md,
  },
  replyLine: {
    backgroundColor: colors.border,
    height: 1,
    width: 42,
  },
  replyTargetRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    justifyContent: "space-between",
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.sm,
  },
  replyTargetText: {
    color: colors.muted,
    flex: 1,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  sendButton: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 20,
    height: 40,
    justifyContent: "center",
    width: 40,
  },
  sendButtonDisabled: {
    opacity: 0.45,
  },
  sheet: {
    backgroundColor: colors.background,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    maxHeight: "86%",
    minHeight: "64%",
    overflow: "hidden",
  },
  thread: {
    gap: spacing.xs,
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    fontWeight: "900",
    textAlign: "center",
  },
  viewRepliesButton: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    marginLeft: 52,
    marginTop: spacing.sm,
    minHeight: 28,
  },
  viewRepliesText: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "900",
  },
});
