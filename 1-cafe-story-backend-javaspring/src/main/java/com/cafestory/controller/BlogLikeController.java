package com.cafestory.controller;

import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.service.serviceInterface.BlogLikeService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/blogs")
public class BlogLikeController {

    private final BlogLikeService blogLikeService;

    public BlogLikeController(BlogLikeService blogLikeService) {
        this.blogLikeService = blogLikeService;
    }

    @PostMapping("/{blogId}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public BlogLikeResponseDTO likeBlog(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogLikeService.likeBlog(blogId, requireUserId(principal));
    }

    @DeleteMapping("/{blogId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeBlog(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        blogLikeService.unlikeBlog(blogId, requireUserId(principal));
    }

    @GetMapping("/{blogId}/likes")
    public List<BlogLikeResponseDTO> getLikesByBlogId(@PathVariable UUID blogId) {
        return blogLikeService.getLikesByBlogId(blogId);
    }

    @GetMapping("/likes/users/{userId}")
    public List<BlogLikeResponseDTO> getLikesByUserId(@PathVariable UUID userId) {
        return blogLikeService.getLikesByUserId(userId);
    }
}
