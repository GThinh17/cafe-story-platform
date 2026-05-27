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
    feed: "/api/blogs/feed",
    trending: "/api/blogs/trending",
    byId: (blogId: string) => `/api/blogs/${pathId(blogId)}`,
    byUser: (userId: string) => `/api/blogs/users/${pathId(userId)}`,
    events: (blogId: string) => `/api/blogs/${pathId(blogId)}/events`,
    likes: (blogId: string) => `/api/blogs/${pathId(blogId)}/likes`,
    likesByUser: (userId: string) => `/api/blogs/likes/users/${pathId(userId)}`,
    shares: (blogId: string) => `/api/blogs/${pathId(blogId)}/shares`,
    sharesByUser: (userId: string) => `/api/blogs/shares/users/${pathId(userId)}`,
  },
  cafes: {
    list: "/api/cafe-pages",
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
    me: "/api/users/me",
    meRegion: "/api/users/me/region",
    followers: (userId: string) => `/api/users/${pathId(userId)}/followers`,
    following: (userId: string) => `/api/users/${pathId(userId)}/following`,
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
