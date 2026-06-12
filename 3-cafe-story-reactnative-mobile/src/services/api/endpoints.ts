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
  blogs: {
    byUser: (userId: string) => `/api/blogs/users/${pathId(userId)}`,
    feed: "/api/blogs/feed",
    likes: (blogId: string) => `/api/blogs/${pathId(blogId)}/likes`,
    likesByUser: (userId: string) => `/api/blogs/likes/users/${pathId(userId)}`,
    saves: (blogId: string) => `/api/blogs/${pathId(blogId)}/saves`,
    savesByUser: (userId: string) => `/api/blogs/saves/users/${pathId(userId)}`,
    savesMe: "/api/blogs/saves/me",
    shares: (blogId: string) => `/api/blogs/${pathId(blogId)}/shares`,
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
  },
} as const;
