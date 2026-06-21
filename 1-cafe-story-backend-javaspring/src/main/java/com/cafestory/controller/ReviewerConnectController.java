package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ReviewerConnectOnboardResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStripeAccountResponseDTO;
import com.cafestory.service.serviceInterface.ReviewerConnectService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/reviewers/connect")
public class ReviewerConnectController {

    private final ReviewerConnectService connectService;

    public ReviewerConnectController(ReviewerConnectService connectService) {
        this.connectService = connectService;
    }

    @PostMapping("/onboard")
    public ReviewerConnectOnboardResponseDTO onboard(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return connectService.createOnboardingLink(requireUserId(principal));
    }

    @PostMapping("/refresh")
    public ReviewerConnectOnboardResponseDTO refresh(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return connectService.refreshOnboardingLink(requireUserId(principal));
    }

    @GetMapping("/status")
    public ReviewerStripeAccountResponseDTO status(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return connectService.getStatus(requireUserId(principal));
    }

    @PostMapping("/sync")
    public ReviewerStripeAccountResponseDTO sync(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return connectService.sync(requireUserId(principal));
    }
}
