package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogInteractionRequestDTO;
import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.service.serviceInterface.BlogLikeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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
            @Valid @RequestBody BlogInteractionRequestDTO request) {
        return blogLikeService.likeBlog(blogId, request.getUserId());
    }

    @DeleteMapping("/{blogId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeBlog(
            @PathVariable UUID blogId,
            @RequestParam UUID userId) {
        blogLikeService.unlikeBlog(blogId, userId);
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
