package com.cafestory.controller;

import com.cafestory.dto.responseDTO.BlogSaveResponseDTO;
import com.cafestory.service.serviceInterface.BlogSaveService;
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
public class BlogSaveController {

    private final BlogSaveService blogSaveService;

    public BlogSaveController(BlogSaveService blogSaveService) {
        this.blogSaveService = blogSaveService;
    }

    @PostMapping("/{blogId}/saves")
    public BlogSaveResponseDTO saveBlog(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogSaveService.saveBlog(blogId, requireUserId(principal));
    }

    @DeleteMapping("/{blogId}/saves")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsaveBlog(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        blogSaveService.unsaveBlog(blogId, requireUserId(principal));
    }

    @GetMapping("/{blogId}/saves")
    public List<BlogSaveResponseDTO> getSavesByBlogId(@PathVariable UUID blogId) {
        return blogSaveService.getSavesByBlogId(blogId);
    }

    @GetMapping("/saves/users/{userId}")
    public List<BlogSaveResponseDTO> getSavesByUserId(@PathVariable UUID userId) {
        return blogSaveService.getSavesByUserId(userId);
    }

    @GetMapping("/saves/me")
    public List<BlogSaveResponseDTO> getMySaves(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogSaveService.getSavesByUserId(requireUserId(principal));
    }
}
