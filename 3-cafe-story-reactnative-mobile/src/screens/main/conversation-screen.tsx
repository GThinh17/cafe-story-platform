import { useCallback, useMemo, useState } from "react";
import { FlatList, StyleSheet, Text, View } from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import type { RouteProp } from "@react-navigation/native";
import {
  ConversationRow,
  ConversationTopBar,
  EmptyState,
  ListRowSkeletonList,
  MessageSearch,
  OnlineUserRail,
  Screen,
} from "../../components";
import { useAuth } from "../../features/auth";
import { mockOnlineUsers } from "../../mocks";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import { getCafePageConversations, getConversations } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { ChatTargetType, ConversationListItem, ConversationResponse } from "../../types";

function formatConversationTime(value: string | null) {
  if (!value) {
    return "";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const diffMs = Date.now() - date.getTime();
  const diffMinutes = Math.max(1, Math.floor(diffMs / 60000));

  if (diffMinutes < 60) {
    return `${diffMinutes}m`;
  }

  const diffHours = Math.floor(diffMinutes / 60);

  if (diffHours < 24) {
    return `${diffHours}h`;
  }

  return `${Math.floor(diffHours / 24)}d`;
}

function mapConversationToListItem(
  conversation: ConversationResponse,
  currentUserId?: string,
  isCafePageInbox = false,
): ConversationListItem {
  const targetType: ChatTargetType =
    conversation.targetType ??
    (conversation.type === "CAFE_PAGE"
      ? "CAFE_PAGE"
      : conversation.type === "GROUP"
        ? "GROUP"
        : "USER");
  const targetMember = conversation.members?.find(
    (member) => member.userId !== currentUserId,
  );
  const customerMember =
    conversation.members?.find((member) => member.role === "MEMBER") ??
    targetMember;
  const displayMember = isCafePageInbox && targetType === "CAFE_PAGE"
    ? customerMember
    : targetMember;
  const targetUserId =
    targetType === "USER" || isCafePageInbox
      ? conversation.targetUserId ?? displayMember?.userId ?? null
      : null;
  const targetCafePageId =
    targetType === "CAFE_PAGE"
      ? conversation.targetCafePageId ?? conversation.targetId ?? null
      : null;
  const fallbackName =
    targetType === "CAFE_PAGE"
      ? "Cafe page"
      : targetType === "GROUP"
        ? "Group chat"
        : "CafeStory user";
  const pageDisplayName = conversation.chatName || conversation.userName || fallbackName;
  const memberDisplayName =
    displayMember?.userFullName ||
    displayMember?.userName ||
    conversation.userName ||
    fallbackName;
  const displayName =
    isCafePageInbox && targetType === "CAFE_PAGE"
      ? memberDisplayName
      : pageDisplayName;

  return {
    avatarUri:
      targetType === "CAFE_PAGE"
        ? isCafePageInbox
          ? displayMember?.userAvatar || null
          : conversation.chatAvatar || null
        : conversation.chatAvatar || displayMember?.userAvatar || null,
    canReplyAsCafePage: conversation.canReplyAsCafePage,
    id: conversation.id,
    lastMessage:
      conversation.lastMessage ||
      conversation.latestMessagePreview ||
      "No messages yet",
    name:
      targetType === "CAFE_PAGE"
        ? displayName
        : conversation.chatName ||
          displayMember?.userFullName ||
          displayMember?.userName ||
          conversation.userName ||
          fallbackName,
    targetCafePageId,
    targetType,
    targetUserId,
    time: formatConversationTime(
      conversation.lastMessageAt || conversation.updatedAt || conversation.createdAt,
    ),
    userName:
      targetType === "CAFE_PAGE"
        ? isCafePageInbox
          ? displayMember?.userName || ""
          : pageDisplayName
        : conversation.userName || displayMember?.userName || "",
  };
}

export function ConversationScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route =
    useRoute<RouteProp<RootStackParamList, typeof routes.conversations>>();
  const { user } = useAuth();
  const cafePageId = route.params?.cafePageId;
  const cafePageName = route.params?.cafePageName;
  const [conversationItems, setConversationItems] = useState<
    ConversationListItem[]
  >([]);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [query, setQuery] = useState("");

  const loadConversations = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    setError("");

    try {
      const response = cafePageId
        ? await getCafePageConversations(cafePageId)
        : await getConversations();
      setConversationItems(
        response.map((conversation) =>
          mapConversationToListItem(conversation, user?.userId, Boolean(cafePageId)),
        ),
      );
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load conversations.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [cafePageId, user?.userId]);

  useFocusEffect(
    useCallback(() => {
      void loadConversations();
    }, [loadConversations]),
  );

  const conversations = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) {
      return conversationItems;
    }

    return conversationItems.filter(
      (conversation) =>
        conversation.name.toLowerCase().includes(normalizedQuery) ||
        conversation.userName.toLowerCase().includes(normalizedQuery) ||
        conversation.lastMessage.toLowerCase().includes(normalizedQuery),
    );
  }, [conversationItems, query]);

  function handleConversationPress(conversation: ConversationListItem) {
    navigation.navigate(routes.chatDetail, {
      chatAvatar: conversation.avatarUri,
      chatName: conversation.name,
      canReplyAsCafePage: conversation.canReplyAsCafePage,
      conversationId: conversation.id,
      targetCafePageId: conversation.targetCafePageId,
      targetType: conversation.targetType,
      targetUserId: conversation.targetUserId,
      userName: conversation.userName,
    });
  }

  return (
    <Screen padded={false}>
      <ConversationTopBar
        onBackPress={() => navigation.goBack()}
        onEditPress={cafePageId ? undefined : () => navigation.navigate(routes.newChat)}
        title={cafePageId ? `${cafePageName || "Cafe Page"} messages` : "Messages"}
      />

      <FlatList
        ListHeaderComponent={
          <View style={styles.headerContent}>
            <MessageSearch onChangeText={setQuery} value={query} />
            {cafePageId ? null : <OnlineUserRail users={mockOnlineUsers} />}
            <Text style={styles.sectionTitle}>
              {cafePageId ? "Cafe page conversations" : "Conversations"}
            </Text>
          </View>
        }
        contentContainerStyle={styles.content}
        data={conversations}
        keyExtractor={(item) => item.id}
        onRefresh={() => void loadConversations(true)}
        refreshing={isRefreshing}
        renderItem={({ item }) => (
          <ConversationRow
            conversation={item}
            onPress={handleConversationPress}
          />
        )}
        ListEmptyComponent={
          isLoading ? (
            <ListRowSkeletonList padded={false} />
          ) : error ? (
            <EmptyState description={error} title="Conversations unavailable" />
          ) : query.trim() ? (
            <View style={styles.emptyState}>
              <Text style={styles.emptyTitle}>No conversations found</Text>
              <Text style={styles.emptyDescription}>
                Try another name or message.
              </Text>
            </View>
          ) : (
            <View style={styles.emptyState}>
              <Text style={styles.emptyTitle}>No conversations yet</Text>
              <Text style={styles.emptyDescription}>
                {cafePageId
                  ? "Customer conversations for this cafe page will appear here."
                  : "Start a new chat from the message button above."}
              </Text>
            </View>
          )
        }
        showsVerticalScrollIndicator={false}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingBottom: 96,
    paddingHorizontal: spacing.lg,
  },
  emptyDescription: {
    color: colors.muted,
    fontSize: typography.label,
    textAlign: "center",
  },
  emptyState: {
    alignItems: "center",
    gap: spacing.xs,
    paddingTop: spacing.xxl,
  },
  emptyTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "800",
    textAlign: "center",
  },
  headerContent: {
    gap: spacing.sm,
    paddingTop: spacing.sm,
  },
  sectionTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "800",
    paddingTop: spacing.xs,
  },
});
