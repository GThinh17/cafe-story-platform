package com.cafestory.controller;

import com.cafestory.dto.requestDTO.PageMemberAddRequestDTO;
import com.cafestory.dto.requestDTO.PageMemberStatusUpdateDTO;
import com.cafestory.dto.responseDTO.PageMemberResponseDTO;
import com.cafestory.service.serviceInterface.PageMemberService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/cafe-pages")
public class PageMemberController {

    private final PageMemberService pageMemberService;

    public PageMemberController(PageMemberService pageMemberService) {
        this.pageMemberService = pageMemberService;
    }

    @PostMapping("/{cafePageId}/members/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public PageMemberResponseDTO requestToJoinPage(
            @PathVariable UUID cafePageId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return pageMemberService.requestToJoinPage(cafePageId, requireUserId(principal));
    }

    @PostMapping("/{cafePageId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public PageMemberResponseDTO addPageMember(
            @PathVariable UUID cafePageId,
            @Valid @RequestBody PageMemberAddRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return pageMemberService.addPageMember(
                cafePageId,
                requireUserId(principal),
                request.getUserId(),
                request.getRoleName());
    }

    @PatchMapping("/{cafePageId}/members/{userId}/status")
    public PageMemberResponseDTO updatePageMemberStatus(
            @PathVariable UUID cafePageId,
            @PathVariable UUID userId,
            @Valid @RequestBody PageMemberStatusUpdateDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return pageMemberService.updatePageMemberStatus(
                cafePageId,
                requireUserId(principal),
                userId,
                request.getStatus());
    }

    @GetMapping("/{cafePageId}/members")
    public List<PageMemberResponseDTO> getPageMembers(@PathVariable UUID cafePageId) {
        return pageMemberService.getPageMembers(cafePageId);
    }

    @GetMapping("/{cafePageId}/members/pending")
    public List<PageMemberResponseDTO> getPendingPageMembers(@PathVariable UUID cafePageId) {
        return pageMemberService.getPendingPageMembers(cafePageId);
    }
}
