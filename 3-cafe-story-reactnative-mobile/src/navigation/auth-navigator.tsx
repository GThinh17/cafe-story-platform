import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { LoginScreen, RegionScreen, RegisterScreen } from "../screens";
import { routes } from "./routes";
import type { AuthStackParamList } from "./types";

const Stack = createNativeStackNavigator<AuthStackParamList>();

export function AuthNavigator() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
      }}
    >
      <Stack.Screen component={LoginScreen} name={routes.login} />
      <Stack.Screen component={RegionScreen} name={routes.region} />
      <Stack.Screen component={RegisterScreen} name={routes.register} />
    </Stack.Navigator>
  );
}
