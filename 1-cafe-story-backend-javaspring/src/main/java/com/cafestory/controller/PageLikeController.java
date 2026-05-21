package com.cafestory.controller;

import com.cafestory.dto.responseDTO.PageLikeResponseDTO;
import com.cafestory.service.serviceInterface.PageLikeService;
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
@RequestMapping("/api/cafe-pages")
public class PageLikeController {

    private final PageLikeService pageLikeService;

    public PageLikeController(PageLikeService pageLikeService) {
        this.pageLikeService = pageLikeService;
    }

    @PostMapping("/{cafePageId}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public PageLikeResponseDTO likePage(
            @PathVariable UUID cafePageId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return pageLikeService.likePage(cafePageId, requireUserId(principal));
    }

    @DeleteMapping("/{cafePageId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikePage(
            @PathVariable UUID cafePageId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        pageLikeService.unlikePage(cafePageId, requireUserId(principal));
    }

    @GetMapping("/{cafePageId}/likes")
    public List<PageLikeResponseDTO> getLikesByCafePageId(@PathVariable UUID cafePageId) {
        return pageLikeService.getLikesByCafePageId(cafePageId);
    }

    @GetMapping("/likes/users/{userId}")
    public List<PageLikeResponseDTO> getLikesByUserId(@PathVariable UUID userId) {
        return pageLikeService.getLikesByUserId(userId);
    }
}
