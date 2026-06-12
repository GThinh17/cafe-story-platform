import type { BlogFeedResponse, StoryItem } from "../types";

export const mockHomeFeedStories: StoryItem[] = [
  {
    id: "story-roasters",
    initials: "RO",
    label: "Roasters",
    avatarUri:
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=180&q=80",
  },
  {
    id: "story-vibe",
    initials: "VB",
    label: "Vibe",
    avatarUri:
      "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=180&q=80",
  },
  {
    id: "story-filter",
    initials: "FI",
    label: "Filter",
    avatarUri:
      "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=180&q=80",
  },
  {
    id: "story-work",
    initials: "WK",
    label: "Work",
    avatarUri:
      "https://images.unsplash.com/photo-1442512595331-e89e73853f31?auto=format&fit=crop&w=180&q=80",
  },
  {
    id: "story-quiet",
    initials: "QU",
    label: "Quiet",
    avatarUri:
      "https://images.unsplash.com/photo-1521017432531-fbd92d768814?auto=format&fit=crop&w=180&q=80",
  },
  {
    id: "story-saved",
    initials: "SA",
    label: "Saved",
    avatarUri:
      "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=180&q=80",
  },
];

export const mockBlogFeed: BlogFeedResponse[] = [
  {
    authorAvatar: null,
    authorUserAvatar:
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=180&q=80",
    authorUserFullName: "Gia Thinh",
    authorUserId: "3f50295f-09cb-43ce-a14e-b1790aaf41f3",
    authorUserName: "gthinh_1704",
    blogId: "d8330b1c-5d4c-45cc-8988-721f43869444",
    commentCount: 28,
    contentPreview:
      "Morning stillness in our new space. The light here is pure magic...",
    createdAt: "2026-06-11T07:30:00",
    displayAuthorType: "USER",
    displayAvatarUrl:
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=180&q=80",
    displayName: "brew_n_bloom",
    imageUrls: [
      "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&w=1000&q=85",
      "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=1000&q=85",
    ],
    likeCount: 342,
    pageAddress: "Hoan Kiem, Ha Noi",
    pageAvatarUrl:
      "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=180&q=80",
    pageCoverUrl:
      "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&w=1000&q=85",
    pageId: "d1526391-e081-4ab6-b1c5-b9f23a1127b2",
    pageName: "Brew & Bloom",
    rankPosition: 3,
    regionArea: "Hoan Kiem",
    regionCity: "Ha Noi",
    regionId: "f7efa8e1-daf6-4a48-b870-f64261aa42be",
    regionProvince: "Ha Noi",
    shareCount: 18,
  },
  {
    authorAvatar: null,
    authorUserAvatar:
      "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=180&q=80",
    authorUserFullName: "Linh Nguyen",
    authorUserId: "65c2410a-8ef5-4d5f-b92b-654fe2ea4e35",
    authorUserName: "linhnotes",
    blogId: "f69ca083-040c-4553-bbc1-f8fbfd0ac01b",
    commentCount: 16,
    contentPreview:
      "A corner table, a clean filter, and enough quiet to finish the chapter.",
    createdAt: "2026-06-11T04:10:00",
    displayAuthorType: "CAFE_PAGE",
    displayAvatarUrl:
      "https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=180&q=80",
    displayName: "The Velvet Roast",
    imageUrls: [
      "https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=1000&q=85",
    ],
    likeCount: 124,
    pageAddress: "District 1, Ho Chi Minh City",
    pageAvatarUrl:
      "https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=180&q=80",
    pageCoverUrl:
      "https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=1000&q=85",
    pageId: "5d4c81b5-f7b3-4318-8d4b-ffb503d8fced",
    pageName: "The Velvet Roast",
    rankPosition: 8,
    regionArea: "District 1",
    regionCity: "Ho Chi Minh City",
    regionId: "ad0ecf51-d730-4f70-8ad6-8c85f1268b92",
    regionProvince: "Ho Chi Minh City",
    shareCount: 9,
  },
];
