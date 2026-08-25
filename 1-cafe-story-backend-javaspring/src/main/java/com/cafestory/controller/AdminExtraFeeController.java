package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminExtraFeeStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.service.serviceInterface.AdminExtraFeeService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/extra-fees")
public class AdminExtraFeeController {

    private final AdminExtraFeeService adminExtraFeeService;

    public AdminExtraFeeController(AdminExtraFeeService adminExtraFeeService) {
        this.adminExtraFeeService = adminExtraFeeService;
    }

    @GetMapping
    public Page<ExtraFeeResponseDTO> getExtraFees(
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminExtraFeeService.getExtraFees(status, pageable(page, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExtraFeeResponseDTO createExtraFee(@Valid @RequestBody ExtraFeeRequestDTO request) {
        return adminExtraFeeService.createExtraFee(request);
    }

    @PutMapping("/{extraFeeId}")
    public ExtraFeeResponseDTO updateExtraFee(
            @PathVariable UUID extraFeeId,
            @Valid @RequestBody ExtraFeeRequestDTO request) {
        return adminExtraFeeService.updateExtraFee(extraFeeId, request);
    }

    @PatchMapping("/{extraFeeId}/status")
    public ExtraFeeResponseDTO updateExtraFeeStatus(
            @PathVariable UUID extraFeeId,
            @Valid @RequestBody AdminExtraFeeStatusUpdateRequestDTO request) {
        return adminExtraFeeService.updateExtraFeeStatus(extraFeeId, request);
    }

    @DeleteMapping("/{extraFeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExtraFee(@PathVariable UUID extraFeeId) {
        adminExtraFeeService.deleteExtraFee(extraFeeId);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
