import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { LoadingState, Screen } from "../components";
import { useAuth } from "../features/auth";
import {
  ChatDetailScreen,
  ConversationScreen,
  NewChatScreen,
  SettingsScreen,
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
        <LoadingState label="Preparing CafeStory..." />
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
          <Stack.Screen
            component={ConversationScreen}
            name={routes.conversations}
          />
          <Stack.Screen component={NewChatScreen} name={routes.newChat} />
          <Stack.Screen component={SettingsScreen} name={routes.settings} />
          <Stack.Screen component={ChatDetailScreen} name={routes.chatDetail} />
        </>
      ) : (
        <Stack.Screen component={AuthNavigator} name={routes.auth} />
      )}
    </Stack.Navigator>
  );
}
