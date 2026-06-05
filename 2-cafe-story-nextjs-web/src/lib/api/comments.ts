import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { CommentCreateRequest, CommentResponse } from "@/types/blog";

type ApiRequestOptions = {
  headers?: HeadersInit;
};

export function getCommentsByBlog(blogId: string, options: ApiRequestOptions = {}) {
  return apiFetch<CommentResponse[]>(apiEndpoints.comments.byBlog(blogId), {
    headers: options.headers,
    method: "GET",
  });
}

export function createComment(
  request: CommentCreateRequest,
  options: ApiRequestOptions = {},
) {
  return apiFetch<CommentResponse>(apiEndpoints.comments.list, {
    body: request,
    headers: options.headers,
    method: "POST",
  });
}
