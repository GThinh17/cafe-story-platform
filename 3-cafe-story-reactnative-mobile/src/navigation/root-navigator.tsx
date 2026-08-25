import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { LoadingState, Screen } from "../components";
import { useAuth } from "../features/auth";
import { t } from "../features/i18n";
import {
  AdsManagerScreen,
  AiAssistantScreen,
  BlogDetailScreen,
  ChatDetailScreen,
  CafePageScreen,
  ConversationScreen,
  NewChatScreen,
  OtherUserProfileScreen,
  PaymentOptionsScreen,
  ProfileFollowsScreen,
  ReviewerDashboardScreen,
  SettingsScreen,
  UserPostsScreen,
} from "../screens";
import { AuthNavigator } from "./auth-navigator";
import { MainTabs } from "./main-tabs";
import { routes } from "./routes";
import type { RootStackParamList } from "./types";

const Stack = createNativeStackNavigator<RootStackParamList>();

export function RootNavigator() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <Screen>
        <LoadingState label={t("Preparing CafeStory...")} />
      </Screen>
    );
  }

  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
      }}
    >
      {isAuthenticated ? (
        <>
          <Stack.Screen component={MainTabs} name={routes.main} />
          <Stack.Screen component={AdsManagerScreen} name={routes.adsManager} />
          <Stack.Screen
            component={AiAssistantScreen}
            name={routes.aiAssistant}
          />
          <Stack.Screen
            component={ConversationScreen}
            name={routes.conversations}
          />
          <Stack.Screen component={NewChatScreen} name={routes.newChat} />
          <Stack.Screen
            component={OtherUserProfileScreen}
            name={routes.otherUserProfile}
          />
          <Stack.Screen
            component={ProfileFollowsScreen}
            name={routes.profileFollows}
          />
          <Stack.Screen
            component={PaymentOptionsScreen}
            name={routes.paymentOptions}
          />
          <Stack.Screen
            component={ReviewerDashboardScreen}
            name={routes.reviewerDashboard}
          />
          <Stack.Screen component={SettingsScreen} name={routes.settings} />
          <Stack.Screen component={BlogDetailScreen} name={routes.blogDetail} />
          <Stack.Screen component={ChatDetailScreen} name={routes.chatDetail} />
          <Stack.Screen component={CafePageScreen} name={routes.cafeDetail} />
          <Stack.Screen component={UserPostsScreen} name={routes.userPosts} />
        </>
      ) : (
        <Stack.Screen component={AuthNavigator} name={routes.auth} />
      )}
    </Stack.Navigator>
  );
}
