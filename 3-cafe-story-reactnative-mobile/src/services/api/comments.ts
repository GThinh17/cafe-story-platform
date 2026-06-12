import type { CommentCreateRequest, CommentResponse } from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getCommentsByBlog(blogId: string) {
  return apiFetch<CommentResponse[]>(apiEndpoints.comments.byBlog(blogId), {
    method: "GET",
  });
}

export function getRepliesByComment(commentId: string) {
  return apiFetch<CommentResponse[]>(apiEndpoints.comments.replies(commentId), {
    method: "GET",
  });
}

export function createComment(request: CommentCreateRequest) {
  return apiFetch<CommentResponse>(apiEndpoints.comments.list, {
    body: request,
    method: "POST",
  });
}
