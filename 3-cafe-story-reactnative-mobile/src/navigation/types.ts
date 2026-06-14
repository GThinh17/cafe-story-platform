import type { NavigatorScreenParams } from "@react-navigation/native";
import type { routes } from "./routes";
import type { ProfileContentTab } from "../types";

export type AuthStackParamList = {
  [routes.login]: undefined;
  [routes.region]: undefined;
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
  [routes.conversations]: undefined;
  [routes.settings]: undefined;
  [routes.chatDetail]: {
    chatAvatar?: string | null;
    chatName?: string | null;
    conversationId: string;
    userName?: string | null;
  };
  [routes.newChat]: undefined;
  [routes.userPosts]: {
    contentTab?: ProfileContentTab;
    initialBlogId?: string;
    userId: string;
    userName?: string | null;
  };
};
