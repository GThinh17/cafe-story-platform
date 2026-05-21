package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogRankingOverrideRequest;
import com.cafestory.dto.responseDTO.BlogRankingOverrideResponse;
import com.cafestory.service.serviceInterface.BlogRankingService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/admin/blogs")
public class AdminBlogRankingController {

    private final BlogRankingService blogRankingService;

    public AdminBlogRankingController(BlogRankingService blogRankingService) {
        this.blogRankingService = blogRankingService;
    }

    @PostMapping("/{blogId}/ranking-override")
    @ResponseStatus(HttpStatus.CREATED)
    public BlogRankingOverrideResponse createRankingOverride(
            @PathVariable UUID blogId,
            @Valid @RequestBody BlogRankingOverrideRequest request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        request.setCreatedBy(requireUserId(principal));
        return blogRankingService.createOverride(blogId, request);
    }
}
