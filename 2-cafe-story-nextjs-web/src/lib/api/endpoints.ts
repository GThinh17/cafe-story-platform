function pathId(id: string) {
  return encodeURIComponent(id);
}

export const apiEndpoints = {
  auth: {
    login: "/api/auth/login",
    register: "/api/auth/register",
    me: "/api/auth/me",
    logout: "/api/auth/logout",
    refresh: "/api/auth/refresh",
    usernameSuggestions: (fullName: string) =>
      `/api/auth/usernames/suggestions?fullName=${encodeURIComponent(fullName)}`,
  },
  blogs: {
    list: "/api/blogs",
    moderated: "/api/blogs/moderated",
    feed: "/api/blogs/feed",
    trending: "/api/blogs/trending",
    byId: (blogId: string) => `/api/blogs/${pathId(blogId)}`,
    byUser: (userId: string) => `/api/blogs/users/${pathId(userId)}`,
    events: (blogId: string) => `/api/blogs/${pathId(blogId)}/events`,
    likes: (blogId: string) => `/api/blogs/${pathId(blogId)}/likes`,
    likesByUser: (userId: string) => `/api/blogs/likes/users/${pathId(userId)}`,
    shares: (blogId: string) => `/api/blogs/${pathId(blogId)}/shares`,
    sharesByUser: (userId: string) => `/api/blogs/shares/users/${pathId(userId)}`,
    sharedByUser: (userId: string) => `/api/blogs/users/${pathId(userId)}/shared`,
    saves: (blogId: string) => `/api/blogs/${pathId(blogId)}/saves`,
    savesByUser: (userId: string) => `/api/blogs/saves/users/${pathId(userId)}`,
    savedByUser: (userId: string) => `/api/blogs/users/${pathId(userId)}/saved`,
  },
  cafes: {
    list: "/api/cafe-pages",
    top: "/api/cafe-pages/top",
    byId: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}`,
    blogs: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}/blogs`,
    likes: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}/likes`,
    likesByUser: (userId: string) => `/api/cafe-pages/likes/users/${pathId(userId)}`,
    follows: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}/follows`,
    followsByUser: (userId: string) => `/api/cafe-pages/follows/users/${pathId(userId)}`,
    memberRequests: (cafePageId: string) =>
      `/api/cafe-pages/${pathId(cafePageId)}/members/requests`,
    members: (cafePageId: string) => `/api/cafe-pages/${pathId(cafePageId)}/members`,
    memberStatus: (cafePageId: string, userId: string) =>
      `/api/cafe-pages/${pathId(cafePageId)}/members/${pathId(userId)}/status`,
    pendingMembers: (cafePageId: string) =>
      `/api/cafe-pages/${pathId(cafePageId)}/members/pending`,
  },
  reportReasons: {
    list: (targetType?: string) =>
      targetType
        ? `/api/report-reasons?targetType=${encodeURIComponent(targetType)}`
        : "/api/report-reasons",
  },
  reports: {
    list: "/api/reports",
  },
  payments: {
    list: "/api/payments",
    mine: "/api/payments/me",
    byId: (paymentId: string) => `/api/payments/${pathId(paymentId)}`,
    stripeSync: (paymentId: string) => `/api/payments/${pathId(paymentId)}/stripe/sync`,
    vnpayReturn: "/api/payments/vnpay/return",
  },
  reviewers: {
    list: "/api/reviewers",
    region: "/api/reviewers/region",
    byUserId: (userId: string) => `/api/reviewers/${pathId(userId)}`,
    payouts: (reviewerId: string) => `/api/reviewers/${pathId(reviewerId)}/payouts`,
    stats: (reviewerId: string) => `/api/reviewers/${pathId(reviewerId)}/stats`,
    badges: (reviewerId: string) => `/api/reviewers/${pathId(reviewerId)}/badges`,
    ranking: "/api/reviewers/ranking",
    top: "/api/reviewers/top",
    connect: {
      onboard: "/api/reviewers/connect/onboard",
      status: "/api/reviewers/connect/status",
      sync: "/api/reviewers/connect/sync",
    },
  },
  extraFees: {
    list: "/api/extra-fees",
  },
  adFees: {
    list: "/api/ad-fees",
  },
  adCampaigns: {
    list: "/api/ad-campaigns",
    byId: (adCampaignId: string) => `/api/ad-campaigns/${pathId(adCampaignId)}`,
    pause: (adCampaignId: string) =>
      `/api/ad-campaigns/${pathId(adCampaignId)}/pause`,
    activate: (adCampaignId: string) =>
      `/api/ad-campaigns/${pathId(adCampaignId)}/activate`,
    clicks: (adCampaignId: string) =>
      `/api/ad-campaigns/${pathId(adCampaignId)}/clicks`,
  },
  comments: {
    list: "/api/comments",
    byId: (commentId: string) => `/api/comments/${pathId(commentId)}`,
    byBlog: (blogId: string) => `/api/comments/blogs/${pathId(blogId)}`,
    byUser: (userId: string) => `/api/comments/users/${pathId(userId)}`,
    replies: (commentId: string) => `/api/comments/${pathId(commentId)}/replies`,
  },
  users: {
    list: "/api/users",
    byId: (userId: string) => `/api/users/${pathId(userId)}`,
    byUsername: (username: string) => `/api/users/by-username/${pathId(username)}`,
    me: "/api/users/me",
    meRegion: "/api/users/me/region",
    followers: (userId: string) => `/api/users/${pathId(userId)}/followers`,
    following: (userId: string) => `/api/users/${pathId(userId)}/following`,
    followingTargets: (userId: string, type = "ALL") =>
      `/api/users/${pathId(userId)}/following-targets?type=${pathId(type)}`,
    follow: (followingUserId: string) =>
      `/api/users/${pathId(followingUserId)}/followers`,
  },
  notifications: {
    list: "/notifications",
    unreadCount: "/notifications/unread-count",
    byId: (notificationId: string) => `/notifications/${pathId(notificationId)}`,
    markRead: (notificationId: string) => `/notifications/${pathId(notificationId)}/read`,
    markAllRead: "/notifications/read-all",
  },
  regions: {
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
    create: (requirement?: string) =>
      requirement
        ? `/api/regions?requirement=${pathId(requirement)}`
        : "/api/regions",
  },
  recommendations: {
    mixed: (page = 0, size = 30) =>
      `/api/recommendations/mixed?page=${page}&size=${size}`,
    cafePages: (page = 0, size = 20) =>
      `/api/recommendations/cafe-pages?page=${page}&size=${size}`,
    reviewers: (page = 0, size = 20) =>
      `/api/recommendations/reviewers?page=${page}&size=${size}`,
    users: (page = 0, size = 20) =>
      `/api/recommendations/users?page=${page}&size=${size}`,
  },
  aiChat: {
    ask: "/api/ai/chat/ask",
  },
  chat: {
    conversations: "/api/chat/conversations",
    directConversation: "/api/chat/conversations/direct",
    groupConversation: "/api/chat/conversations/group",
    messages: (conversationId: string) =>
      `/api/chat/conversations/${pathId(conversationId)}/messages`,
    members: (conversationId: string) =>
      `/api/chat/conversations/${pathId(conversationId)}/members`,
    member: (conversationId: string, memberUserId: string) =>
      `/api/chat/conversations/${pathId(conversationId)}/members/${pathId(memberUserId)}`,
    leave: (conversationId: string) =>
      `/api/chat/conversations/${pathId(conversationId)}/leave`,
    group: (conversationId: string) =>
      `/api/chat/conversations/${pathId(conversationId)}/group`,
  },
} as const;
