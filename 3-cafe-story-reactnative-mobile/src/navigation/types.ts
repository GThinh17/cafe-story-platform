import type { NavigatorScreenParams } from "@react-navigation/native";
import type { routes } from "./routes";

export type AuthStackParamList = {
  [routes.login]: undefined;
  [routes.register]: undefined;
};

export type MainTabParamList = {
  [routes.home]: undefined;
  [routes.explore]: undefined;
  [routes.create]: undefined;
  [routes.notifications]: undefined;
  [routes.profile]: undefined;
};

export type RootStackParamList = {
  [routes.auth]: NavigatorScreenParams<AuthStackParamList>;
  [routes.main]: NavigatorScreenParams<MainTabParamList>;
  [routes.createPostModal]: undefined;
  [routes.cafeDetail]: { cafeId: string };
  [routes.blogDetail]: { blogId: string };
};
