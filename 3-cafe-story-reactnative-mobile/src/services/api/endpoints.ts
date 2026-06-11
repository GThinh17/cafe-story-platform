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
    feed: "/api/blogs/feed",
    likes: (blogId: string) => `/api/blogs/${pathId(blogId)}/likes`,
    likesByUser: (userId: string) => `/api/blogs/likes/users/${pathId(userId)}`,
    saves: (blogId: string) => `/api/blogs/${pathId(blogId)}/saves`,
    savesByUser: (userId: string) => `/api/blogs/saves/users/${pathId(userId)}`,
    savesMe: "/api/blogs/saves/me",
    shares: (blogId: string) => `/api/blogs/${pathId(blogId)}/shares`,
  },
  users: {
    follow: (followingUserId: string) =>
      `/api/users/${pathId(followingUserId)}/followers`,
    following: (userId: string) => `/api/users/${pathId(userId)}/following`,
  },
} as const;
