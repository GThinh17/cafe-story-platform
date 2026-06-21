import type {
  BlogFeedResponse,
  BlogResponse,
  BlogTrendingResponse,
  UserPostPreview,
} from "../../types";

export function isUserAuthoredBlog(blog: BlogResponse) {
  return blog.displayAuthorType !== "CAFE_PAGE" && !blog.pageId;
}

export function blogResponseToPostPreview(blog: BlogResponse): UserPostPreview {
  return {
    caption: blog.content,
    commentCount: blog.commentCount ?? 0,
    id: blog.id,
    imageUri: blog.imageUrls?.[0] ?? null,
    isPinned: blog.isPinned,
    likeCount: blog.likeCount ?? 0,
  };
}

export function blogTrendingToFeedBlog(blog: BlogTrendingResponse): BlogFeedResponse {
  return {
    authorUserAvatar: blog.authorUserAvatar,
    authorUserFullName: blog.authorUserFullName,
    authorUserId: blog.authorUserId,
    authorUserName: blog.authorUserName,
    blogId: blog.blogId,
    commentCount: blog.commentCount ?? 0,
    contentPreview: blog.contentPreview,
    createdAt: blog.createdAt,
    displayAuthorType: blog.displayAuthorType,
    displayAvatarUrl: blog.displayAvatarUrl,
    displayName: blog.displayName,
    imageUrls: blog.imageUrls ?? [],
    isLike: blog.isLike,
    isSave: blog.isSave,
    likeCount: blog.likeCount ?? 0,
    pageAvatarUrl: blog.pageAvatarUrl,
    pageCoverUrl: blog.pageCoverUrl,
    pageId: blog.pageId,
    pageName: blog.pageName,
    rankPosition: blog.rankPosition,
    shareCount: blog.shareCount ?? 0,
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
