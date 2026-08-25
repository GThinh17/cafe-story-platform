package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.service.serviceInterface.AdminBlogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/blogs")
public class AdminBlogController {

    private final AdminBlogService adminBlogService;

    public AdminBlogController(AdminBlogService adminBlogService) {
        this.adminBlogService = adminBlogService;
    }

    @GetMapping
    public Page<BlogResponseDTO> getBlogs(
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) UUID authorUserId,
            @RequestParam(required = false) UUID pageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminBlogService.getBlogs(status, authorUserId, pageId, pageable(page, size));
    }

    @GetMapping("/{blogId}")
    public BlogResponseDTO getBlog(@PathVariable UUID blogId) {
        return adminBlogService.getBlog(blogId);
    }

    @PatchMapping("/{blogId}/status")
    public BlogResponseDTO updateBlogStatus(
            @PathVariable UUID blogId,
            @Valid @RequestBody AdminPostStatusUpdateRequestDTO request) {
        return adminBlogService.updateBlogStatus(blogId, request);
    }

    @DeleteMapping("/{blogId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlog(@PathVariable UUID blogId) {
        adminBlogService.deleteBlog(blogId);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
