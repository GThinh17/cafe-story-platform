package com.cafestory.controller;

import com.cafestory.dto.requestDTO.RagUserBlogModerationRequestDTO;
import com.cafestory.dto.responseDTO.RagBlogModerationItemResponseDTO;
import com.cafestory.service.serviceInterface.RagUserContextService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/user-context")
public class RagUserContextController {

    private final RagUserContextService ragUserContextService;

    public RagUserContextController(RagUserContextService ragUserContextService) {
        this.ragUserContextService = ragUserContextService;
    }

    @PostMapping("/blog-moderation")
    public List<RagBlogModerationItemResponseDTO> getBlogModeration(
            @Valid @RequestBody RagUserBlogModerationRequestDTO request) {
        return ragUserContextService.getBlogModerationForUser(request.getUserJwt(), request.getLimit());
    }
}
