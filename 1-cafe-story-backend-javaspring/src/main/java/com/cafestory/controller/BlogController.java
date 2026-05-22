package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
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
        blogCreateDTO.setAuthorUserId(requireUserId(principal));
        return blogService.createBlog(blogCreateDTO);
    }

    @GetMapping
    public List<BlogResponseDTO> getBlogs(@RequestParam(required = false) UUID authorUserId) {
        if (authorUserId != null) {
            return blogService.getAllBlogsByUserId(authorUserId);
        }
        return blogService.getAllBlogs();
    }

    @GetMapping("/users/{userId}")
    public List<BlogResponseDTO> getAllBlogsByUserId(@PathVariable UUID userId) {
        return blogService.getAllBlogsByUserId(userId);
    }

    @GetMapping("/{blogId}")
    public BlogResponseDTO getBlogById(@PathVariable UUID blogId) {
        return blogService.getBlogById(blogId);
    }

    @PatchMapping("/{blogId}")
    public BlogResponseDTO updateBlog(
            @PathVariable UUID blogId,
            @Valid @RequestBody BlogUpdateDTO blogUpdateDTO) {
        return blogService.updateBlog(blogId, blogUpdateDTO);
    }

    @DeleteMapping("/{blogId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlog(@PathVariable UUID blogId) {
        blogService.deleteBlog(blogId);
    }
}
