export type ExploreUserSearchResult = {
  regionCity: string | null;
  userAvatar: string | null;
  userFullName: string | null;
  userId: string;
  userName: string;
};

export type ExploreCafePageSearchResult = {
  address: string | null;
  avatarUrl: string | null;
  id: string;
  name: string;
  regionCity: string | null;
};

export type ExploreBlogSearchResult = {
  authorUserAvatar: string | null;
  authorUserFullName: string | null;
  authorUserId: string | null;
  authorUserName: string | null;
  content: string;
  createdAt: string | null;
  displayAvatarUrl: string | null;
  displayName: string | null;
  id: string;
  pageAvatarUrl: string | null;
  pageId: string | null;
  pageName: string | null;
};

export type ExploreSearchResults = {
  blogs: ExploreBlogSearchResult[];
  cafePages: ExploreCafePageSearchResult[];
  users: ExploreUserSearchResult[];
};
