package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogEventRequest;
import com.cafestory.dto.responseDTO.BlogEventResponse;
import com.cafestory.dto.responseDTO.BlogTrendingResponse;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.service.serviceInterface.BlogEventService;
import com.cafestory.service.serviceInterface.BlogTrendingService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/blogs")
public class BlogTrendingController {

    private final BlogTrendingService blogTrendingService;
    private final BlogEventService blogEventService;

    public BlogTrendingController(
            BlogTrendingService blogTrendingService,
            BlogEventService blogEventService) {
        this.blogTrendingService = blogTrendingService;
        this.blogEventService = blogEventService;
    }

    @GetMapping("/trending")
    public List<BlogTrendingResponse> getTrendingBlogs(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "HOUR_24") TrendWindowType windowType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return blogTrendingService.getTrendingBlogs(
                principal == null ? null : principal.userId(),
                windowType,
                page,
                size);
    }

    @PostMapping("/{blogId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public BlogEventResponse recordBlogEvent(
            @PathVariable UUID blogId,
            @Valid @RequestBody BlogEventRequest request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return blogEventService.recordEvent(
                blogId,
                requireUserId(principal),
                request.getEventType(),
                request.getWeight());
    }
}
