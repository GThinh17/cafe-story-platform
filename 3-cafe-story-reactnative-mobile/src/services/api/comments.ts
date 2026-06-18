import type { CommentCreateRequest, CommentResponse } from "../../types";
import { apiCacheTtl, cachedApiCall, invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getCommentsByBlog(blogId: string) {
  return cachedApiCall(`comments:blog:${blogId}`, apiCacheTtl.comments, () =>
    apiFetch<CommentResponse[]>(apiEndpoints.comments.byBlog(blogId), {
      method: "GET",
    }),
  );
}

export function getRepliesByComment(commentId: string) {
  return cachedApiCall(`comments:replies:${commentId}`, apiCacheTtl.comments, () =>
    apiFetch<CommentResponse[]>(apiEndpoints.comments.replies(commentId), {
      method: "GET",
    }),
  );
}

export async function createComment(request: CommentCreateRequest) {
  const response = await apiFetch<CommentResponse>(apiEndpoints.comments.list, {
    body: request,
    method: "POST",
  });
  invalidateApiCache(`comments:blog:${request.blogId}`);
  if (request.parentCommentId) {
    invalidateApiCache(`comments:replies:${request.parentCommentId}`);
  }
  invalidateApiCache(`blogs:detail:${request.blogId}`);
  invalidateApiCache("feed:");
  return response;
}
