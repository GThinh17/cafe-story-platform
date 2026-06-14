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
  },
  chat: {
    conversations: "/api/chat/conversations",
    directConversation: "/api/chat/conversations/direct",
    messages: (conversationId: string) =>
      `/api/chat/conversations/${pathId(conversationId)}/messages`,
  },
  blogs: {
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
  },
  comments: {
    list: "/api/comments",
    byBlog: (blogId: string) => `/api/comments/blogs/${pathId(blogId)}`,
    replies: (commentId: string) => `/api/comments/${pathId(commentId)}/replies`,
  },
  reportReasons: {
    list: (targetType?: string) =>
      targetType
        ? `/api/report-reasons?targetType=${pathId(targetType)}`
        : "/api/report-reasons",
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
  },
  reports: {
    list: "/api/reports",
  },
  users: {
    byId: (userId: string) => `/api/users/${pathId(userId)}`,
    byUsername: (username: string) => `/api/users/by-username/${pathId(username)}`,
    follow: (followingUserId: string) =>
      `/api/users/${pathId(followingUserId)}/followers`,
    following: (userId: string) => `/api/users/${pathId(userId)}/following`,
    me: "/api/users/me",
    meAvatar: "/api/users/me/avatar",
    meRegion: "/api/users/me/region",
  },
} as const;
