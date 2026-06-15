package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminModerationResolveRequestDTO;
import com.cafestory.dto.responseDTO.AdminModerationResultResponseDTO;
import com.cafestory.service.serviceInterface.AdminModerationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/moderation")
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    public AdminModerationController(AdminModerationService adminModerationService) {
        this.adminModerationService = adminModerationService;
    }

    @GetMapping("/results")
    public Page<AdminModerationResultResponseDTO> getAllResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminModerationService.getAllResults(pageable(page, size));
    }

    @GetMapping("/queue")
    public Page<AdminModerationResultResponseDTO> getQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminModerationService.getQueue(pageable(page, size));
    }

    @GetMapping("/results/{resultId}")
    public AdminModerationResultResponseDTO getResult(@PathVariable UUID resultId) {
        return adminModerationService.getResult(resultId);
    }

    @PostMapping("/results/{resultId}/resolve")
    public AdminModerationResultResponseDTO resolveResult(
            @PathVariable UUID resultId,
            @Valid @RequestBody AdminModerationResolveRequestDTO request) {
        return adminModerationService.resolveResult(resultId, request);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
