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
  getUserProfile,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { UserResponse } from "../../types";

export function NewChatScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user } = useAuth();
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [pendingUserId, setPendingUserId] = useState<string | null>(null);
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState<UserResponse[]>([]);

  const loadSuggestions = useCallback(async () => {
    if (!user?.userId) {
      setSuggestions([]);
      setIsLoading(false);
      return;
    }

    setError("");
    setIsLoading(true);

    try {
      const following = await getFollowingByUserId(user.userId);
      const followedProfiles = await Promise.all(
        following.map((item) => getUserProfile(item.followingUserId)),
      );

      setSuggestions(followedProfiles);
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

    return suggestions.filter(
      (item) =>
        item.userName.toLowerCase().includes(normalizedQuery) ||
        (item.userFullName ?? "").toLowerCase().includes(normalizedQuery),
    );
  }, [query, suggestions]);

  async function handleSelectUser(selectedUser: UserResponse) {
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
          ) : error ? null : query.trim() ? (
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
