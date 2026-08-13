package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogShareRequestDTO;
import com.cafestory.dto.responseDTO.BlogShareResponseDTO;
import com.cafestory.service.serviceInterface.BlogShareService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/blogs")
public class BlogShareController {

    private final BlogShareService blogShareService;

    public BlogShareController(BlogShareService blogShareService) {
        this.blogShareService = blogShareService;
    }

    @PostMapping("/{blogId}/shares")
    @ResponseStatus(HttpStatus.CREATED)
    public BlogShareResponseDTO shareBlog(
            @PathVariable UUID blogId,
            @Valid @RequestBody BlogShareRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogShareService.shareBlog(
                blogId,
                requireUserId(principal),
                request.getShareType(),
                request.getActorContextType(),
                request.getActorCafePageId());
    }

    @DeleteMapping("/{blogId}/shares")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShare(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        blogShareService.deleteShare(blogId, requireUserId(principal));
    }

    @GetMapping("/{blogId}/shares")
    public List<BlogShareResponseDTO> getSharesByBlogId(@PathVariable UUID blogId) {
        return blogShareService.getSharesByBlogId(blogId);
    }

    @GetMapping("/shares/users/{userId}")
    public List<BlogShareResponseDTO> getSharesByUserId(@PathVariable UUID userId) {
        return blogShareService.getSharesByUserId(userId);
    }
}
