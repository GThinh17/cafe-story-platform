import { useCallback, useMemo, useState } from "react";
import { FlatList, StyleSheet, Text, View } from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
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
import { getConversations } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { ConversationListItem, ConversationResponse } from "../../types";

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
): ConversationListItem {
  const targetMember = conversation.members?.find(
    (member) => member.userId !== currentUserId,
  );

  return {
    avatarUri: conversation.chatAvatar || targetMember?.userAvatar || null,
    id: conversation.id,
    lastMessage:
      conversation.lastMessage ||
      conversation.latestMessagePreview ||
      "No messages yet",
    name:
      conversation.chatName ||
      targetMember?.userFullName ||
      targetMember?.userName ||
      conversation.userName ||
      "CafeStory user",
    targetUserId: targetMember?.userId ?? null,
    time: formatConversationTime(
      conversation.lastMessageAt || conversation.updatedAt || conversation.createdAt,
    ),
    userName: conversation.userName || targetMember?.userName || "",
  };
}

export function ConversationScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user } = useAuth();
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
      const response = await getConversations();
      setConversationItems(
        response.map((conversation) =>
          mapConversationToListItem(conversation, user?.userId),
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
  }, [user?.userId]);

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
      conversationId: conversation.id,
      targetUserId: conversation.targetUserId,
      userName: conversation.userName,
    });
  }

  return (
    <Screen padded={false}>
      <ConversationTopBar
        onBackPress={() => navigation.goBack()}
        onEditPress={() => navigation.navigate(routes.newChat)}
        title="Messages"
      />

      <FlatList
        ListHeaderComponent={
          <View style={styles.headerContent}>
            <MessageSearch onChangeText={setQuery} value={query} />
            <OnlineUserRail users={mockOnlineUsers} />
            <Text style={styles.sectionTitle}>Conversations</Text>
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
                Start a new chat from the message button above.
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
