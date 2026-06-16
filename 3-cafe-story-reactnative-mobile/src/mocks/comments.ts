import type { CommentResponse } from "../types";

const fallbackComments: CommentResponse[] = [
  {
    authorUserName: "linhnotes",
    blogId: "mock-blog",
    content: "Goc nay nhin yeu qua, nhat la anh sang buoi sang.",
    createdAt: "2026-06-11T07:42:00",
    id: "comment-fallback-1",
    imageUrls: null,
    parentCommentId: null,
    status: "PUBLISHED",
    updatedAt: null,
    userId: "mock-user-1",
  },
  {
    authorUserName: "minhbrew",
    blogId: "mock-blog",
    content: "Quan nay co on de ngoi lam viec lau khong?",
    createdAt: "2026-06-11T07:55:00",
    id: "comment-fallback-2",
    imageUrls: null,
    parentCommentId: null,
    status: "PUBLISHED",
    updatedAt: null,
    userId: "mock-user-2",
  },
  {
    authorUserName: "gthinh_1704",
    blogId: "mock-blog",
    content: "Minh thich filter o day, vi ca phe rat sach va hau vi diu.",
    createdAt: "2026-06-11T08:05:00",
    id: "comment-fallback-3",
    imageUrls: null,
    parentCommentId: null,
    status: "PUBLISHED",
    updatedAt: null,
    userId: "mock-user-3",
  },
];

export const mockCommentsByBlogId: Record<string, CommentResponse[]> = {
  "d8330b1c-5d4c-45cc-8988-721f43869444": fallbackComments,
  "f69ca083-040c-4553-bbc1-f8fbfd0ac01b": [
    {
      authorUserName: "quietcorner",
      blogId: "f69ca083-040c-4553-bbc1-f8fbfd0ac01b",
      content: "Minh can nhung quan yen tinh nhu the nay.",
      createdAt: "2026-06-11T05:20:00",
      id: "comment-velvet-1",
      imageUrls: null,
      parentCommentId: null,
      status: "PUBLISHED",
      updatedAt: null,
      userId: "mock-user-4",
    },
    {
      authorUserName: "an.cafe",
      blogId: "f69ca083-040c-4553-bbc1-f8fbfd0ac01b",
      content: "Ban gan cua so co can dat truoc khong ban?",
      createdAt: "2026-06-11T05:44:00",
      id: "comment-velvet-2",
      imageUrls: null,
      parentCommentId: null,
      status: "PUBLISHED",
      updatedAt: null,
      userId: "mock-user-5",
    },
  ],
};

export async function getMockCommentsByBlogId(blogId: string) {
  await new Promise((resolve) => setTimeout(resolve, 220));

  return mockCommentsByBlogId[blogId] ?? fallbackComments;
}
