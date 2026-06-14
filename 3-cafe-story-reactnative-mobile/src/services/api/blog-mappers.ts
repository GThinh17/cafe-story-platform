import type { BlogFeedResponse, BlogResponse, UserPostPreview } from "../../types";

export function blogResponseToPostPreview(blog: BlogResponse): UserPostPreview {
  return {
    caption: blog.content,
    commentCount: blog.commentCount ?? 0,
    id: blog.id,
    imageUri: blog.imageUrls?.[0] ?? null,
    likeCount: blog.likeCount ?? 0,
  };
}

export function blogResponseToFeedBlog(blog: BlogResponse): BlogFeedResponse {
  return {
    authorUserAvatar: blog.authorUserAvatar ?? null,
    authorUserFullName: blog.authorUserFullName ?? null,
    authorUserId: blog.authorUserId,
    authorUserName: blog.authorUserName ?? null,
    blogId: blog.id,
    commentCount: blog.commentCount ?? 0,
    contentPreview: blog.content,
    createdAt: blog.createdAt,
    displayAuthorType: blog.displayAuthorType ?? null,
    displayAvatarUrl: blog.displayAvatarUrl ?? null,
    displayName: blog.displayName ?? null,
    imageUrls: blog.imageUrls ?? [],
    isFollow: null,
    isLike: blog.isLike ?? false,
    isSave: blog.isSave ?? false,
    likeCount: blog.likeCount ?? 0,
    pageAvatarUrl: blog.pageAvatarUrl ?? null,
    pageId: blog.pageId ?? null,
    pageName: blog.pageName ?? null,
    regionId: blog.regionId ?? null,
    shareCount: blog.shareCount ?? 0,
  };
}
