package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogRatingRequestDTO;
import com.cafestory.dto.responseDTO.BlogRatingResponseDTO;
import com.cafestory.service.serviceInterface.BlogRatingService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/blogs")
public class BlogRatingController {

    private final BlogRatingService blogRatingService;

    public BlogRatingController(BlogRatingService blogRatingService) {
        this.blogRatingService = blogRatingService;
    }

    @PutMapping("/{blogId}/rating")
    public BlogRatingResponseDTO rateBlog(
            @PathVariable UUID blogId,
            @Valid @RequestBody BlogRatingRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogRatingService.rateBlog(blogId, requireUserId(principal), request.getRating());
    }

    @DeleteMapping("/{blogId}/rating")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        blogRatingService.deleteRating(blogId, requireUserId(principal));
    }

    @GetMapping("/{blogId}/ratings")
    public List<BlogRatingResponseDTO> getRatingsByBlogId(@PathVariable UUID blogId) {
        return blogRatingService.getRatingsByBlogId(blogId);
    }

    @GetMapping("/ratings/users/{userId}")
    public List<BlogRatingResponseDTO> getRatingsByUserId(@PathVariable UUID userId) {
        return blogRatingService.getRatingsByUserId(userId);
    }
}
