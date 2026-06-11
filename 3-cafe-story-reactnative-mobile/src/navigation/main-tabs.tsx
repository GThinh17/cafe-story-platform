import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { BottomBar } from "../components";
import {
  CreateScreen,
  ExploreScreen,
  HomeScreen,
  NotificationsScreen,
  ProfileScreen,
} from "../screens";
import { routes } from "./routes";
import type { MainTabParamList } from "./types";

const Tab = createBottomTabNavigator<MainTabParamList>();

export function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
      }}
      tabBar={(props) => <BottomBar {...props} />}
    >
      <Tab.Screen component={HomeScreen} name={routes.home} />
      <Tab.Screen component={ExploreScreen} name={routes.explore} />
      <Tab.Screen component={CreateScreen} name={routes.create} />
      <Tab.Screen component={NotificationsScreen} name={routes.notifications} />
      <Tab.Screen component={ProfileScreen} name={routes.profile} />
    </Tab.Navigator>
  );
}
