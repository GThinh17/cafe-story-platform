package com.cafestory.controller;

import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.service.serviceInterface.ExtraFeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/extra-fees")
public class ExtraFeeController {

    private final ExtraFeeService extraFeeService;

    public ExtraFeeController(ExtraFeeService extraFeeService) {
        this.extraFeeService = extraFeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExtraFeeResponseDTO createExtraFee(@Valid @RequestBody ExtraFeeRequestDTO request) {
        return extraFeeService.createExtraFee(request);
    }

    @PutMapping("/{extraFeeId}")
    public ExtraFeeResponseDTO updateExtraFee(
            @PathVariable UUID extraFeeId,
            @Valid @RequestBody ExtraFeeRequestDTO request) {
        return extraFeeService.updateExtraFee(extraFeeId, request);
    }

    @GetMapping
    public List<ExtraFeeResponseDTO> getAllExtraFees() {
        return extraFeeService.getAllExtraFees();
    }
}
