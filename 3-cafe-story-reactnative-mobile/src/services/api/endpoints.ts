function pathId(id: string) {
  return encodeURIComponent(id);
}

export const apiEndpoints = {
  adFees: {
    list: "/api/ad-fees",
  },
  adCampaigns: {
    byId: (adCampaignId: string) => `/api/ad-campaigns/${pathId(adCampaignId)}`,
    clicks: (adCampaignId: string) => `/api/ad-campaigns/${pathId(adCampaignId)}/clicks`,
    list: "/api/ad-campaigns",
  },
  auth: {
    login: "/api/auth/login",
    register: "/api/auth/register",
    me: "/api/auth/me",
    logout: "/api/auth/logout",
    refresh: "/api/auth/refresh",
    usernameSuggestions: (fullName: string) =>
      `/api/auth/usernames/suggestions?fullName=${pathId(fullName)}`,
  },
  chat: {
    cafePageConversations: (cafePageId: string) =>
      `/api/chat/cafe-pages/${pathId(cafePageId)}/conversations`,
    conversations: "/api/chat/conversations",
    directConversation: "/api/chat/conversations/direct",
    cafePageConversation: "/api/chat/conversations/cafe-page",
    messages: (conversationId: string) =>
      `/api/chat/conversations/${pathId(conversationId)}/messages`,
  },
  blogs: {
    byId: (blogId: string) => `/api/blogs/${pathId(blogId)}`,
    byUser: (userId: string) => `/api/blogs/users/${pathId(userId)}`,
    feed: "/api/blogs/feed",
    list: "/api/blogs",
    moderated: "/api/blogs/moderated",
    likes: (blogId: string) => `/api/blogs/${pathId(blogId)}/likes`,
    likesByUser: (userId: string) => `/api/blogs/likes/users/${pathId(userId)}`,
    saves: (blogId: string) => `/api/blogs/${pathId(blogId)}/saves`,
    savedByUser: (userId: string) => `/api/blogs/users/${pathId(userId)}/saved`,
    savesByUser: (userId: string) => `/api/blogs/saves/users/${pathId(userId)}`,
    savesMe: "/api/blogs/saves/me",
    shares: (blogId: string) => `/api/blogs/${pathId(blogId)}/shares`,
    sharedByUser: (userId: string) => `/api/blogs/users/${pathId(userId)}/shared`,
    taggedByUser: (userId: string) => `/api/blogs/users/${pathId(userId)}/tagged`,
    trending: "/api/blogs/trending",
  },
  cafePages: {
    blogs: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}/blogs`,
    byId: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}`,
    follows: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}/follows`,
    likes: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}/likes`,
    list: "/api/cafe-pages",
  },
  comments: {
    list: "/api/comments",
    byBlog: (blogId: string) => `/api/comments/blogs/${pathId(blogId)}`,
    replies: (commentId: string) => `/api/comments/${pathId(commentId)}/replies`,
  },
  extraFees: {
    list: "/api/extra-fees",
  },
  feed: {
    impressions: "/api/feed/impressions",
    list: "/api/feed",
    organic: "/api/feed/organic",
  },
  notifications: {
    byId: (notificationId: string) => `/notifications/${pathId(notificationId)}`,
    list: "/notifications",
    markAllRead: "/notifications/read-all",
    markRead: (notificationId: string) => `/notifications/${pathId(notificationId)}/read`,
    unreadCount: "/notifications/unread-count",
  },
  payments: {
    byId: (paymentId: string) => `/api/payments/${pathId(paymentId)}`,
    list: "/api/payments",
    vnpayReturn: "/api/payments/vnpay/return",
  },
  reportReasons: {
    list: (targetType?: string) =>
      targetType
        ? `/api/report-reasons?targetType=${pathId(targetType)}`
        : "/api/report-reasons",
  },
  regions: {
    create: (requirement?: string) =>
      requirement
        ? `/api/regions?requirement=${pathId(requirement)}`
        : "/api/regions",
    provinces: "/api/regions/provinces",
    cities: (provinceCode?: string) =>
      provinceCode
        ? `/api/regions/cities?provinceCode=${pathId(provinceCode)}`
        : "/api/regions/cities",
    wards: (params: { cityCode?: string; provinceCode?: string } = {}) => {
      const query = new URLSearchParams();
      if (params.provinceCode) {
        query.set("provinceCode", params.provinceCode);
      }
      if (params.cityCode) {
        query.set("cityCode", params.cityCode);
      }
      const queryString = query.toString();
      return queryString ? `/api/regions/wards?${queryString}` : "/api/regions/wards";
    },
  },
  recommendations: {
    cafePages: (page = 0, size = 20) =>
      `/api/recommendations/cafe-pages?page=${page}&size=${size}`,
    mixed: (page = 0, size = 30) =>
      `/api/recommendations/mixed?page=${page}&size=${size}`,
    reviewers: (page = 0, size = 20) =>
      `/api/recommendations/reviewers?page=${page}&size=${size}`,
    users: (page = 0, size = 20) =>
      `/api/recommendations/users?page=${page}&size=${size}`,
  },
  reports: {
    list: "/api/reports",
  },
  reviewers: {
    badges: (reviewerId: string) => `/api/reviewers/${pathId(reviewerId)}/badges`,
    byUser: (userId: string) => `/api/reviewers/${pathId(userId)}`,
    payouts: (reviewerId: string) => `/api/reviewers/${pathId(reviewerId)}/payouts`,
    ranking: "/api/reviewers/ranking",
    stats: (reviewerId: string) => `/api/reviewers/${pathId(reviewerId)}/stats`,
  },
  users: {
    byId: (userId: string) => `/api/users/${pathId(userId)}`,
    byUsername: (username: string) => `/api/users/by-username/${pathId(username)}`,
    follow: (followingUserId: string) =>
      `/api/users/${pathId(followingUserId)}/followers`,
    followers: (userId: string) => `/api/users/${pathId(userId)}/followers`,
    following: (userId: string) => `/api/users/${pathId(userId)}/following`,
    followingTargets: (userId: string, type = "ALL") =>
      `/api/users/${pathId(userId)}/following-targets?type=${pathId(type)}`,
    list: "/api/users",
    me: "/api/users/me",
    meAvatar: "/api/users/me/avatar",
    meRegion: "/api/users/me/region",
  },
} as const;
