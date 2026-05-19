package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CafePageInteractionRequestDTO;
import com.cafestory.dto.responseDTO.PageFollowResponseDTO;
import com.cafestory.service.serviceInterface.PageFollowService;
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
@RequestMapping("/api/cafe-pages")
public class PageFollowController {

    private final PageFollowService pageFollowService;

    public PageFollowController(PageFollowService pageFollowService) {
        this.pageFollowService = pageFollowService;
    }

    @PostMapping("/{cafePageId}/follows")
    @ResponseStatus(HttpStatus.CREATED)
    public PageFollowResponseDTO followPage(
            @PathVariable UUID cafePageId,
            @Valid @RequestBody CafePageInteractionRequestDTO request) {
        return pageFollowService.followPage(cafePageId, request.getUserId());
    }

    @DeleteMapping("/{cafePageId}/follows")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowPage(
            @PathVariable UUID cafePageId,
            @RequestParam UUID userId) {
        pageFollowService.unfollowPage(cafePageId, userId);
    }

    @GetMapping("/{cafePageId}/follows")
    public List<PageFollowResponseDTO> getFollowersByCafePageId(@PathVariable UUID cafePageId) {
        return pageFollowService.getFollowersByCafePageId(cafePageId);
    }

    @GetMapping("/follows/users/{userId}")
    public List<PageFollowResponseDTO> getFollowedPagesByUserId(@PathVariable UUID userId) {
        return pageFollowService.getFollowedPagesByUserId(userId);
    }
}
