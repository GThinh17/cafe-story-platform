package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminCafePageStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.service.serviceInterface.AdminCafePageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cafe-pages")
public class AdminCafePageController {

    private final AdminCafePageService adminCafePageService;

    public AdminCafePageController(AdminCafePageService adminCafePageService) {
        this.adminCafePageService = adminCafePageService;
    }

    @GetMapping
    public Page<CafePageResponseDTO> getCafePages(
            @RequestParam(required = false) PageStatus status,
            @RequestParam(required = false) UUID ownerUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminCafePageService.getCafePages(status, ownerUserId, pageable(page, size));
    }

    @GetMapping("/{pageId}")
    public CafePageResponseDTO getCafePage(@PathVariable UUID pageId) {
        return adminCafePageService.getCafePage(pageId);
    }

    @PatchMapping("/{pageId}/status")
    public CafePageResponseDTO updateCafePageStatus(
            @PathVariable UUID pageId,
            @Valid @RequestBody AdminCafePageStatusUpdateRequestDTO request) {
        return adminCafePageService.updateCafePageStatus(pageId, request);
    }

    @DeleteMapping("/{pageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCafePage(@PathVariable UUID pageId) {
        adminCafePageService.deleteCafePage(pageId);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
