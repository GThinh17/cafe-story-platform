function pathId(id: string) {
  return encodeURIComponent(id);
}

export const apiEndpoints = {
  adFees: {
    list: "/api/ad-fees",
  },
  auth: {
    login: "/api/auth/login",
    register: "/api/auth/register",
    me: "/api/auth/me",
    logout: "/api/auth/logout",
    refresh: "/api/auth/refresh",
  },
  chat: {
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
    me: "/api/users/me",
    meAvatar: "/api/users/me/avatar",
    meRegion: "/api/users/me/region",
  },
} as const;
