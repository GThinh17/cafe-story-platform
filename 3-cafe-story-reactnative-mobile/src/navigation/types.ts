import type { NavigatorScreenParams } from "@react-navigation/native";
import type { routes } from "./routes";
import type { ChatTargetType, PaymentPlanTab, ProfileContentTab } from "../types";

export type AuthStackParamList = {
  [routes.login]: undefined;
  [routes.region]: undefined;
  [routes.register]: undefined;
};

export type MainTabParamList = {
  [routes.home]: undefined;
  [routes.explore]: undefined;
  [routes.create]: {
    cafeAvatarUrl?: string | null;
    cafePageId?: string;
    cafePageName?: string | null;
    locationName?: string | null;
    regionId?: string | null;
  } | undefined;
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
    canReplyAsCafePage?: boolean;
    targetCafePageId?: string | null;
    targetType?: ChatTargetType | null;
    targetUserId?: string | null;
    userName?: string | null;
  };
  [routes.newChat]: undefined;
  [routes.otherUserProfile]: {
    userId: string;
    userName?: string | null;
  };
  [routes.paymentOptions]: {
    initialTab?: PaymentPlanTab;
  } | undefined;
  [routes.profileFollows]: {
    initialTab?: "followers" | "following";
    userId: string;
    userName?: string | null;
  };
  [routes.reviewerDashboard]: undefined;
  [routes.userPosts]: {
    contentTab?: ProfileContentTab;
    initialBlogId?: string;
    pageId?: string;
    pageName?: string | null;
    userId?: string;
    userName?: string | null;
  };
};
