package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.service.serviceInterface.BlogService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlogResponseDTO createBlog(
            @Valid @RequestBody BlogCreateDTO blogCreateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.createBlog(blogCreateDTO, requireUserId(principal));
    }

    @PostMapping("/moderated")
    @ResponseStatus(HttpStatus.CREATED)
    public BlogResponseDTO createModeratedBlog(
            @Valid @RequestBody BlogCreateDTO blogCreateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.createModeratedBlog(blogCreateDTO, requireUserId(principal));
    }

    @GetMapping
    public List<BlogResponseDTO> getBlogs(
            @RequestParam(required = false) UUID authorUserId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        if (authorUserId != null) {
            return blogService.getAllBlogsByUserId(authorUserId, optionalUserId(principal));
        }
        return blogService.getAllBlogs(optionalUserId(principal));
    }

    @GetMapping("/users/{userId}")
    public List<BlogResponseDTO> getAllBlogsByUserId(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.getAllBlogsByUserId(userId, optionalUserId(principal));
    }

    @GetMapping("/users/{userId}/saved")
    public List<BlogResponseDTO> getSavedBlogsByUserId(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.getSavedBlogsByUserId(userId, optionalUserId(principal));
    }

    @GetMapping("/users/{userId}/shared")
    public List<BlogResponseDTO> getSharedBlogsByUserId(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.getSharedBlogsByUserId(userId, optionalUserId(principal));
    }

    @GetMapping("/users/{userId}/tagged")
    public List<BlogResponseDTO> getTaggedBlogsByUserId(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.getTaggedBlogsByUserId(userId, optionalUserId(principal));
    }

    @GetMapping("/tag-suggestions")
    public List<BlogTaggedUserResponseDTO> getTagSuggestions(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.getTagSuggestions(requireUserId(principal), keyword);
    }

    @GetMapping("/{blogId}")
    public BlogResponseDTO getBlogById(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.getBlogById(blogId, optionalUserId(principal));
    }

    @PatchMapping("/{blogId}")
    public BlogResponseDTO updateBlog(
            @PathVariable UUID blogId,
            @Valid @RequestBody BlogUpdateDTO blogUpdateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogService.updateBlog(blogId, requireUserId(principal), blogUpdateDTO);
    }

    @DeleteMapping("/{blogId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlog(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        blogService.deleteBlog(blogId, requireUserId(principal));
    }

    private UUID optionalUserId(AuthenticatedUserPrincipal principal) {
        return principal == null ? null : principal.userId();
    }
}
