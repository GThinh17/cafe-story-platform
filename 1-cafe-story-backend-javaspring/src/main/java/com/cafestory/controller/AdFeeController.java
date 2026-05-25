package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdFeeRequestDTO;
import com.cafestory.dto.responseDTO.AdFeeResponseDTO;
import com.cafestory.service.serviceInterface.AdFeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ad-fees")
public class AdFeeController {

    private final AdFeeService adFeeService;

    public AdFeeController(AdFeeService adFeeService) {
        this.adFeeService = adFeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdFeeResponseDTO createAdFee(@Valid @RequestBody AdFeeRequestDTO request) {
        return adFeeService.createAdFee(request);
    }

    @GetMapping
    public List<AdFeeResponseDTO> getAllAdFees() {
        return adFeeService.getAllAdFees();
    }

    @DeleteMapping("/{adFeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdFee(@PathVariable UUID adFeeId) {
        adFeeService.deleteAdFee(adFeeId);
    }
}
