import { useCallback, useEffect, useMemo, useState } from "react";
import { FlatList, StyleSheet, Text, TextInput, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  ConversationTopBar,
  EmptyState,
  LoadingState,
  NewChatSuggestionRow,
  Screen,
} from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  createDirectConversation,
  getFollowingByUserId,
  getUserRecommendations,
  getUserProfile,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { RecommendationCardResponse } from "../../types";
import type { NewChatSuggestionUser } from "../../components";

function isMatchingSuggestion(item: NewChatSuggestionUser, query: string) {
  return (
    item.userName.toLowerCase().includes(query) ||
    (item.userFullName ?? "").toLowerCase().includes(query)
  );
}

function recommendationToChatUser(
  recommendation: RecommendationCardResponse,
): NewChatSuggestionUser | null {
  if (recommendation.targetType !== "USER") {
    return null;
  }

  const userName = recommendation.username || recommendation.fullName;

  if (!userName) {
    return null;
  }

  return {
    userAvatar: recommendation.avatar,
    userFullName: recommendation.fullName,
    userId: recommendation.targetId,
    userName,
  };
}

export function NewChatScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user } = useAuth();
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [pendingUserId, setPendingUserId] = useState<string | null>(null);
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState<NewChatSuggestionUser[]>([]);
  const [discoverSuggestions, setDiscoverSuggestions] = useState<NewChatSuggestionUser[]>([]);

  const loadSuggestions = useCallback(async () => {
    if (!user?.userId) {
      setSuggestions([]);
      setDiscoverSuggestions([]);
      setIsLoading(false);
      return;
    }

    setError("");
    setIsLoading(true);

    try {
      const [following, recommendations] = await Promise.all([
        getFollowingByUserId(user.userId),
        getUserRecommendations(0, 12),
      ]);
      const followedProfiles = await Promise.all(
        following.map((item) => getUserProfile(item.followingUserId)),
      );
      const followedIds = new Set(followedProfiles.map((profile) => profile.userId));
      const recommendationProfiles = recommendations
        .map(recommendationToChatUser)
        .filter((item): item is NewChatSuggestionUser => Boolean(item))
        .filter(
          (item) =>
            item.userId !== user.userId &&
            !followedIds.has(item.userId),
        );

      setSuggestions(followedProfiles);
      setDiscoverSuggestions(recommendationProfiles);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load suggestions.",
      );
    } finally {
      setIsLoading(false);
    }
  }, [user?.userId]);

  useEffect(() => {
    void loadSuggestions();
  }, [loadSuggestions]);

  const filteredSuggestions = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) {
      return suggestions;
    }

    return suggestions.filter((item) =>
      isMatchingSuggestion(item, normalizedQuery),
    );
  }, [query, suggestions]);

  const filteredDiscoverSuggestions = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) {
      return discoverSuggestions;
    }

    return discoverSuggestions.filter((item) =>
      isMatchingSuggestion(item, normalizedQuery),
    );
  }, [discoverSuggestions, query]);

  async function handleSelectUser(selectedUser: NewChatSuggestionUser) {
    if (pendingUserId) {
      return;
    }

    setPendingUserId(selectedUser.userId);
    setError("");

    try {
      const conversation = await createDirectConversation(selectedUser.userId);

      navigation.replace(routes.chatDetail, {
        chatAvatar: conversation.chatAvatar || selectedUser.userAvatar,
        chatName:
          conversation.chatName ||
          selectedUser.userFullName ||
          selectedUser.userName,
        conversationId: conversation.id,
        targetUserId: selectedUser.userId,
        userName: conversation.userName || selectedUser.userName,
      });
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to start chat.",
      );
    } finally {
      setPendingUserId(null);
    }
  }

  return (
    <Screen padded={false}>
      <ConversationTopBar
        onBackPress={() => navigation.goBack()}
        title="New message"
      />

      <FlatList
        ListHeaderComponent={
          <View style={styles.headerContent}>
            <View style={styles.searchRow}>
              <Text style={styles.toLabel}>To:</Text>
              <TextInput
                autoCapitalize="none"
                onChangeText={setQuery}
                placeholder="Search"
                placeholderTextColor={colors.muted}
                style={styles.searchInput}
                value={query}
              />
            </View>
            <Text style={styles.sectionTitle}>Suggestions</Text>
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
          </View>
        }
        ListFooterComponent={
          filteredDiscoverSuggestions.length > 0 ? (
            <View style={styles.discoverSection}>
              <Text style={styles.sectionTitle}>Discover people</Text>
              {filteredDiscoverSuggestions.map((item) => (
                <NewChatSuggestionRow
                  disabled={Boolean(pendingUserId)}
                  key={item.userId}
                  onPress={handleSelectUser}
                  user={item}
                />
              ))}
            </View>
          ) : null
        }
        contentContainerStyle={styles.content}
        data={filteredSuggestions}
        keyExtractor={(item) => item.userId}
        renderItem={({ item }) => (
          <NewChatSuggestionRow
            disabled={Boolean(pendingUserId)}
            onPress={handleSelectUser}
            user={item}
          />
        )}
        ListEmptyComponent={
          isLoading ? (
            <LoadingState label="Loading suggestions..." />
          ) : error || filteredDiscoverSuggestions.length > 0 ? null : query.trim() ? (
            <EmptyState
              description="Try another name or username."
              title="No people found"
            />
          ) : (
            <EmptyState
              description="Follow people first to start a direct chat."
              title="No suggestions yet"
            />
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
  discoverSection: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
    gap: spacing.sm,
    marginTop: spacing.lg,
    paddingTop: spacing.lg,
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.label,
    fontWeight: "600",
  },
  headerContent: {
    gap: spacing.lg,
    paddingTop: spacing.md,
  },
  searchInput: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "600",
    paddingVertical: spacing.sm,
  },
  searchRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xl,
    minHeight: 58,
  },
  sectionTitle: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "800",
  },
  toLabel: {
    color: colors.muted,
    fontSize: typography.title,
    fontWeight: "800",
  },
});
