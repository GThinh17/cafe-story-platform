package com.cafestory.controller;

import com.cafestory.dto.requestDTO.ContentReportRequestDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/reports")
public class ContentReportController {

    private final ContentReportService contentReportService;

    public ContentReportController(ContentReportService contentReportService) {
        this.contentReportService = contentReportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentReportResponseDTO createReport(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody ContentReportRequestDTO request) {
        return contentReportService.createReport(requireUserId(principal), request);
    }
}
