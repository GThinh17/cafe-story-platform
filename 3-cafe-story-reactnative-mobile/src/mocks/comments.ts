import type { BlogFeedComment } from "../types";

const fallbackComments: BlogFeedComment[] = [
  {
    authorAvatar:
      "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=180&q=80",
    authorName: "linhnotes",
    content: "Goc nay nhin yeu qua, nhat la anh sang buoi sang.",
    createdAt: "2026-06-11T07:42:00",
    id: "comment-fallback-1",
    likeCount: 12,
    replyCount: 2,
  },
  {
    authorAvatar: null,
    authorName: "minhbrew",
    content: "Quan nay co on de ngoi lam viec lau khong?",
    createdAt: "2026-06-11T07:55:00",
    id: "comment-fallback-2",
    likeCount: 4,
  },
  {
    authorAvatar:
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=180&q=80",
    authorName: "gthinh_1704",
    content: "Minh thich filter o day, vi ca phe rat sach va hau vi diu.",
    createdAt: "2026-06-11T08:05:00",
    id: "comment-fallback-3",
    likeCount: 8,
  },
];

export const mockCommentsByBlogId: Record<string, BlogFeedComment[]> = {
  "d8330b1c-5d4c-45cc-8988-721f43869444": fallbackComments,
  "f69ca083-040c-4553-bbc1-f8fbfd0ac01b": [
    {
      authorAvatar: null,
      authorName: "quietcorner",
      content: "Minh can nhung quan yen tinh nhu the nay.",
      createdAt: "2026-06-11T05:20:00",
      id: "comment-velvet-1",
      likeCount: 6,
    },
    {
      authorAvatar:
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=180&q=80",
      authorName: "an.cafe",
      content: "Ban gan cua so co can dat truoc khong ban?",
      createdAt: "2026-06-11T05:44:00",
      id: "comment-velvet-2",
      likeCount: 3,
      replyCount: 1,
    },
  ],
};

export async function getMockCommentsByBlogId(blogId: string) {
  await new Promise((resolve) => setTimeout(resolve, 220));

  return mockCommentsByBlogId[blogId] ?? fallbackComments;
}
