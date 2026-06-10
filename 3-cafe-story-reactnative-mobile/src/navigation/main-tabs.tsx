import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { Text } from "react-native";
import {
  CreateScreen,
  ExploreScreen,
  HomeScreen,
  NotificationsScreen,
  ProfileScreen,
} from "../screens";
import { colors } from "../theme";
import { routes } from "./routes";
import type { MainTabParamList } from "./types";

const Tab = createBottomTabNavigator<MainTabParamList>();

const tabIcons: Record<keyof MainTabParamList, string> = {
  [routes.create]: "+",
  [routes.explore]: "S",
  [routes.home]: "H",
  [routes.notifications]: "N",
  [routes.profile]: "P",
};

export function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.muted,
        tabBarIcon: ({ color }) => (
          <Text style={{ color, fontWeight: "900" }}>
            {tabIcons[route.name]}
          </Text>
        ),
      })}
    >
      <Tab.Screen component={HomeScreen} name={routes.home} />
      <Tab.Screen component={ExploreScreen} name={routes.explore} />
      <Tab.Screen component={CreateScreen} name={routes.create} />
      <Tab.Screen component={NotificationsScreen} name={routes.notifications} />
      <Tab.Screen component={ProfileScreen} name={routes.profile} />
    </Tab.Navigator>
  );
}
